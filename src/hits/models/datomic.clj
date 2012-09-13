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


; The data produced by gitparse.clj needs to be massaged a bit
; For example git produces dates as strings "Fri 24 June ..." 
; But datomic needs java.sql.TimeStamp objects
; This is one transformation that needs to happen. There might be more.
; Lets create a list of all the transformations we need. We'll compose them
; later.
(def transformations-log 
  [(fn timestamp [m] 
     "Git timestamp is the number of seconds past some time
      It is returned as a string. We just need to parse it" 
     (if (contains? m "timestamp") 
       (update-in m ["timestamp"] parse-int)
        m))
   (fn date [m]
     "Git dates are strings. We need to turn them into java.sql.TimeStamps
      Note that this timestamp and the previous timestamp are very different"
     (if (contains? m "date")
       (update-in m ["date"] to-timestamp)
        m))])


(defn translate-log [logmap]
  (let [oldkeys (keys logmap)
        newkeys (map key-translate oldkeys)
        newmap  ((apply comp transformations-log) logmap)]
    (zipmap newkeys (map newmap oldkeys))))

(def transformations-wc
  [])

(defn translate-wc [wc-map]
  (let [oldkeys (keys wc-map)
        newkeys (map key-translate oldkeys)
        newmap  ((apply comp transformations-wc) wc-map)]
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
