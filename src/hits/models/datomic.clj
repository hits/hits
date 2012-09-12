(ns hits.models.datomic
  (:require [datomic.api :only [q db] :as d])
  (:require [hits.models.git-schema :only [schema] :as git] )
  (:require [hits.models.gitparse :only [parse-log parse-whatchanged] :as parse])
  (:require [clj-time.format :as timef])
  (:require [clj-time.coerce :as timec]))

(def key-translate 
  {"author_name"  :git/author-name
   "author_email" :git/author-email
   "id"           :git/id
   "subject"      :git/subject
   "body"         :git/body
   "date"         :git/date
   "timestamp"    :git/timestamp
   "action"       :git.change/action
   "file"         :git.change/file
   "commit-id"    :git.change/commit-id}
   )

(defn to-timestamp [s]
  (->> s
    (timef/parse (timef/formatter "EEE MMM dd HH:mm:ss yyyy Z"))
    timec/to-timestamp))

(defn parse-int [s]
  (Integer. (re-find #"[0-9]*" s)))

(def transformations-log 
  [(defn timestamp [m]
     (if (contains? m "timestamp")
       (update-in m ["timestamp"] parse-int)
        m))
   (defn date [m]
     (if (contains? m "date")
       (update-in m ["date"] to-timestamp)
        m))])


(defn translate-log [logmap]
  (let [oldkeys (keys logmap)
        newkeys (map key-translate oldkeys)
        newmap  (reduce (fn [m trns] (trns m)) logmap transformations-log)]
    (zipmap newkeys (map newmap oldkeys))))

(def transformations-wc
  [])
(defn translate-wc [wc-map]
  (let [oldkeys (keys wc-map)
        newkeys (map key-translate oldkeys)
        newmap  (reduce (fn [m trns] (trns m)) wc-map transformations-wc)]
    (zipmap newkeys (map newmap oldkeys))))


(defn add-new-id [m]
  (assoc m :db/id (datomic.api/tempid :db.part/user)))

(defn add-repo-to-db [conn user repo]
  (parse/clone-repo user repo) ; idempotent
  (let [log-data (parse/parse-log user repo)
        dtm-data (map add-new-id (map translate-log log-data))
        wc-data  (parse/parse-whatchanged user repo)
        wc-flat  (parse/unpack-whatchanged wc-data)
        dwc-data (map add-new-id (map translate-log wc-flat))]
    (map (fn [dat] (d/transact conn [dat])) (concat dtm-data dwc-data))))