(ns hits.models.datomic-test
  (:use clojure.test
        hits.models.datomic)
  (:require [datomic.api :only [q db] :as d])
  (:require [hits.models.git-schema :only [schema] :as git] )
  (:require [hits.models.gitparse :only [parse-log parse-whatchanged] :as parse]))

(deftest test-timestamp
  (is (= (type (to-timestamp "Fri Jul 6 11:59:00 2012 -0500")) 
         java.sql.Timestamp)))

(defn set= [a b]
  (= (set a) (set b)))

(deftest test-translate
  (let [logmap          {"id" "ABCD"
                         "subject" "a subject" 
                         "body" "a body" 
                         "author_name" "Joe Schmoe" 
                         "date" "Fri Jul 6 11:59:00 2012 -0500"
                         "timestamp" "10000000"}
        dtmmap          (translate logmap)
        expected-dtmmap {:git/id "ABCD" 
                         :git/subject "a subject" 
                         :git/body "a body" 
                         :git/author-name "Joe Schmoe" 
                         :git/date (to-timestamp "Fri Jul 6 11:59:00 2012 -0500")
                         :git/timestamp 10000000}]
    (is (set= (keys expected-dtmmap) (keys dtmmap)))
    (is (set= (vals expected-dtmmap) (vals dtmmap)))
    (is (= (type (:git/date dtmmap)) java.sql.Timestamp))))

(deftest test-add-new-id
  (let [oldmap {:git/id "ABCD" :git/subject "a subject"}
        newmap (add-new-id oldmap)]
    (is (contains? newmap :db/id))))

(def uri "datomic:mem://hits-test")
(d/create-database uri)
(def conn (d/connect uri))
@(d/transact conn git/schema)
(datomic.common/await-derefs (add-repo-to-db conn "hits" "hits-test"))


(deftest test-query-subjects
  (is (contains? (d/q '[:find ?sub :where [?c :git/subject ?sub]] (d/db conn)) ["First commit"])))

(deftest test-author-of-first-commit
  (is (= (ffirst (d/q '[:find ?name :where [?c :git/subject "First commit"]
                                            [?c :git/author-name ?name]] (d/db conn)))
         "Matthew Rocklin")))
           
    
;(clojure.pprint/pprint (d/q '[:find ?name ?email :where [?c :git/author-email ?email] [?c :git/author-name ?name]] (d/db conn)))
;(clojure.pprint/pprint (seq (d/q '[:find ?c ?name ?id :where [?c :git/id "804d5c62d429e313a542630e58efd3de6a0a1d02"] [?c :git/author-name ?name] [?c :git/id ?id]] (d/db conn))))

(test-timestamp)
(test-translate)
(test-add-new-id)
(test-query-subjects)
(test-author-of-first-commit)