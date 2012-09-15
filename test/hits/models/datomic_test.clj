(ns hits.models.datomic-test
  (:use clojure.test
        hits.models.datomic)
  (:require [datomic.api :only [q db] :as d])
  (:require [hits.models.git-schema :only [schema] :as git] )
  (:require [hits.models.gitparse :only [parse-log parse-whatchanged] :as parse]))

(deftest test-parse-int
  (is (= (parse-int "123") 123)))

(deftest test-timestamp
  (is (= (type (to-timestamp "Fri Jul 6 11:59:00 2012 -0500")) 
         java.sql.Timestamp)))

(defn set= [a b]
  (= (set a) (set b)))

(deftest test-translate-log
  (let [logmap          {"id" "ABCD"
                         "subject" "a subject" 
                         "body" "a body" 
                         "author_name" "Joe Schmoe" 
                         "date" "Fri Jul 6 11:59:00 2012 -0500"
                         "timestamp" "10000000"}
        dtmmap          (translate-log logmap)
        expected-dtmmap {:git.log/id "ABCD" 
                         :git.log/subject "a subject" 
                         :git.log/body "a body" 
                         :git.log/author-name "Joe Schmoe" 
                         :git.log/date (to-timestamp "Fri Jul 6 11:59:00 2012 -0500")
                         :git.log/timestamp 10000000}]
    (is (set= (keys expected-dtmmap) (keys dtmmap)))
    (is (set= (vals expected-dtmmap) (vals dtmmap)))
    (is (= (type (:git.log/date dtmmap)) java.sql.Timestamp))))

(deftest test-add-new-id
  (let [oldmap {:git.log/id "ABCD" :git.log/subject "a subject"}
        newmap (add-new-id oldmap)]
    (is (contains? newmap :db/id))))

(def uri "datomic:mem://hits-test")
(d/create-database uri)
(def conn (d/connect uri))
@(d/transact conn git/schema)
(datomic.common/await-derefs (add-repo-to-db conn "hits" "hits-test"))


(deftest test-query-subjects
  (is (contains? (d/q '[:find ?sub :where [?c :git.log/subject ?sub]] (d/db conn)) ["First commit"])))

(deftest test-author-of-first-commit
  (is (= (d/q '[:find ?name :where [?c :git.log/subject "First commit"]
                                   [?c :git.log/author-name ?name]] (d/db conn))
         #{["Matthew Rocklin"]})))

(deftest test-changes-of-first-commit
  (is (= (d/q '[:find ?action ?file :where [?c :git.change/action     ?action]
                                           [?c :git.change/file       ?file]
                                           [?c :git.change/commit-id "fcd206b7ba561ed12641328bc6da8b3a494deabb"]]
                     (d/db conn))
          #{["A" "README"]})))

(deftest test-join
  (is (= (d/q '[:find ?action ?file :where [?commit :git.log/subject  "First commit"]
                                           [?commit :git.log/id           ?id]
                                           [?change :git.change/commit-id ?id]
                                           [?change :git.change/action    ?action]
                                           [?change :git.change/file      ?file]]
              (d/db conn))
         #{["A" "README"]})))

(deftest test-count-groups
  (is (= (count-groups [[1 2 3] [2 2 2], [2 1 2]] 2)
         {2 2, 3 1})))

(deftest test-activity
  (is (= (activity conn "activity-dir")
         #{["activity-dir/a-file" "Matthew Rocklin" "29702acc15d7f5acd884cbc2d70db1ed881cab0c"]})))

(deftest test-author-activity
  (is (= (author-activity conn "activity-dir")
         {"Matthew Rocklin" 1})))

(deftest test-file-activity
  (is (= (file-activity conn "README")
         {"README.md" 1 "README" 3})))

(comment (clojure.pprint/pprint (seq (d/q 
                      '[:find ?file  
                        :where [?c :git.log/id ?id] 
                               [?c :git.log/author-name "Matthew Rocklin"] 
                               [?change :git.change/commit-id ?id]
                               [?change :git.change/file      ?file]
                               [?change :git.change/action    "A"]]
                            (d/db conn)))))
