(ns hits.models.datomic
  (:require [datomic.api :only [q db] :as d])
  (:require [hits.models.git-schema :only [schema] :as git] )
  (:require [hits.models.gitparse :only [parse-log parse-whatchanged] :as parse])
  (:require [clj-time.format :as timef])
  (:require [clj-time.coerce :as timec]))


; ------------------------------
;  Inserts
; ------------------------------

; keys used in git parse and keys used in datomic are different
; Here is a mapping between them
(def key-translate 
  {"author_name"  :git.log/author-name
   "author_email" :git.log/author-email
   "id"           :git.log/id
   "subject"      :git.log/subject
   "body"         :git.log/body
   "date"         :git.log/date
   "timestamp"    :git.log/timestamp
   "action"       :git.change/action
   "file"         :git.change/file
   "commit-id"    :git.change/commit-id}
   )

(defn to-timestamp [s]
  "Convert a time string to a java.sql.Timestamp"
  (->> s
    (timef/parse (timef/formatter "EEE MMM dd HH:mm:ss yyyy Z"))
    timec/to-timestamp))

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
       (update-in m ["timestamp"] #(Integer/parseInt %))
        m))
   (fn date [m]
     "Git dates are strings. We need to turn them into java.sql.TimeStamps
      Note that this timestamp and the previous timestamp are very different"
     (if (contains? m "date")
       (update-in m ["date"] to-timestamp)
        m))])


(defn translate-log [logmap]
  "Convert maps from gitparse/parse-log into something for datomic"
  (let [oldkeys (keys logmap)
        newkeys (map key-translate oldkeys)
        newmap  ((apply comp transformations-log) logmap)]
    (zipmap newkeys (map newmap oldkeys))))

(def transformations-wc
  [])

(defn translate-wc [wc-map]
  "Convert maps from gitparse/parse-whatchanged into something for datomic"
  (let [oldkeys (keys wc-map)
        newkeys (map key-translate oldkeys)
        newmap  ((apply comp transformations-wc) wc-map)]
    (zipmap newkeys (map newmap oldkeys))))

(defn add-new-id [m]
  "Add a generated id to an insert map
  This should be called on every *new* entry before sending it to the database"
  (assoc m :db/id (datomic.api/tempid :db.part/user)))

(defn repo-partition [user repo]
  "Define a new partition for a user/repo pair"
  {:db/id (datomic.api/tempid :db.part/db),
   :db/ident (parse/str-identifier user repo),
   :db.install/_partition :db.part/db})

(defn add-repo-to-db [conn user repo] ; this function is not idempotent
  "Add a new repository to database. This function should only be called once."
  (parse/clone-repo user repo) ; idempotent
  (let [log-data (parse/parse-log user repo)
        dtm-data (map add-new-id (map translate-log log-data))
        wc-data  (parse/parse-whatchanged user repo)
        wc-flat  (parse/unpack-whatchanged wc-data)
        dwc-data (map add-new-id (map translate-log wc-flat))]
    (map (fn [dat] (d/transact conn [dat])) (concat dtm-data dwc-data))))

; ------------------------------
; Queries 
; ------------------------------
(defn count-groups [vecs idx]
  "Group a seq of vectors by an index and return the counts of each bin"
  (let [groups (group-by #(nth % idx) vecs)]
       (zipmap (keys groups) (map count (vals groups)))))

(defn activity [user repo path conn]
  "File activity in a directory of a repository 
  Returns a vector of [File, Author, ID] triples "
  (d/q 
    '[:find ?file ?author ?id 
      :in $ ?path
      :where [?c      :git.log/author-name  ?author]
             [?c      :git.log/id           ?id]
             [?change :git.change/commit-id ?id] 
             [?change :git.change/file      ?file]
             [(.startsWith ^String ?file ?path)]]
     (d/db conn) path))

(defn file-activity [user repo path conn]
  "The number of times each file within a path has been modified"
  (count-groups (activity user repo path conn) 0))

(defn author-activity [user repo path conn]
  "The number of times each user has modified a file within a path"
  (count-groups (activity user repo path conn) 1))

