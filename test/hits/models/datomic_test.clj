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

(def uri-mem "datomic:mem://hits-test")
(def uri-disk "datomic:free://localhost:4334/hits-test")
(def uri uri-mem)
(d/create-database uri)
(def conn (d/connect uri))
@(d/transact conn git/schema)
(datomic.common/await-derefs (add-repo-to-db! conn "hits" "hits-test"))
(datomic.common/await-derefs (add-repo-to-db! conn "hits" "hits-test2"))

(deftest test-add-repo-to-db!-idempotent
  (datomic.common/await-derefs (add-repo-to-db! conn "hits" "hits-test"))
  (datomic.common/await-derefs (add-repo-to-db! conn "hits" "hits-test"))
  (is (= nil
         (add-repo-to-db! conn "hits" "hits-test"))))

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

(deftest test-userrepo
  (is (= (d/q '[:find ?repo :where [?c :git.log/id ?id]
                                   [?c :git.log/repo ?repo]
                                   [?change :git.change/commit-id ?id]
                                   [?change :git.change/file "README.md"]] (d/db conn))
         #{["hits-test"], ["hits-test2"]})))

(deftest test-count-groups
  (is (= (count-groups [[1 2 3] [2 2 2], [2 1 2]] #(nth % 2))
         {2 2, 3 1})))

(deftest test-activity
  (is (= (activity "hits" "hits-test" "activity-dir" (d/db conn))
         #{["activity-dir/a-file" "Matthew Rocklin" "29702acc15d7f5acd884cbc2d70db1ed881cab0c"]})))

(deftest test-author-activity
  (is (= (author-activity "hits" "hits-test" "activity-dir" (d/db conn))
         {"Matthew Rocklin" 1})))

(deftest test-file-activity
  (is (= (file-activity "hits" "hits-test" "README" (d/db conn))
         {"README.md" 1 "README" 3})))

(deftest test-current-repos
  (is (= (current-repos (d/db conn))
         #{["hits" "hits-test"] ["hits" "hits-test2"]})))
(comment
(deftest test-file-activity-tree
  (is (= (file-activity-tree [["dir/file.txt" "bob" "1234"]
                              ["dir/file.txt" "alice" "1235"]
                              ["readme" "alice" "4852"]
                              ["dir/src.clj" "bob" "9876"]])
  {"file" [] :authors {"alice" 2 "bob" 2}
     :children [{"file" ["readme"] :authors {"alice" 1} :children []}
        {"file" ["dir"]    :authors {"alice" 1 "bob" 2}
           :children [{"file" ["dir" "file.txt"] :authors {"alice" 1 "bob" 1}
                         :children []}
                      {"file" ["dir" "src.clj"]  :authors {"bob" 1}
                         :children []}]}]}))))

(defn abs [x]
  (if (> x 0) x (- x)))
(deftest test-repeat-inf
  (is (= (repeat-inf (fn [x] (-> x dec abs)) #{5, 8})
         #{0, 1, 2, 3, 4, 5, 6, 7, 8})))

(deftest test-author-counts
  (is (= (author-counts [{:name "A" :id 1} {:name "A" :id 2} {:name "B" :id 3}])
         {"A" 2 "B" 1})))

(deftest test-split-path
  (is (= (split-path "A/B.py")
         ["" "A" "B.py"])))

(deftest test-activity-maps
  (is (=  (activity-maps ["dir/file.clj" :joe 1234])
          {:path ["" "dir" "file.clj"] :name :joe :id 1234})))

(def files [["A" "B.clj"] ["A" "C.clj"] 
            ["A" "D"] ["A" "D" "E.clj"]
            ["F" "F.clj"]])
(deftest test-tree-of
  (is (= (tree-of ["A"] (fmap set (group-by drop-last files)) )
         {:path ["A"] :children #{{:path ["A" "B.clj"] :children #{}}
                                  {:path ["A" "C.clj"] :children #{}}
                                  {:path ["A" "D"    ] :children #{
                                     {:path ["A" "D" "E.clj"] :children #{}}}}}})))

(deftest test-contributions
  (is (= (contributions [{:path :a :name :joe} {:path :a :name :joe} 
                         {:path :a :name :bob} {:path :b :name :bob}])
         {:a {:joe 2 :bob 1}
          :b {:bob 1}})))
  
(deftest test-tree-contributions-simple
  (is (= (tree-contributions {:path :a :children #{}} {:a {:joe 1 :bob 2}})
         {:path :a :contributions {:joe 1 :bob 2} :children #{}})))

(deftest test-tree-contributions-moderate
  (let [tree {:path :a :children #{{:path :b :children #{}}
                                {:path :c :children #{{:path :e :children #{}}
                                                      {:path :f :children #{}}}}}}
       conts {:f {:joe 1 :bob 2} :e {:joe 1} :b {:alice 3}}
       expected {:path :a :contributions {:joe 2 :bob 2 :alice 3}
             :children #{{:path :b :contributions {:alice 3} :children #{}}
                         {:path :c :contributions {:joe 2 :bob 2} 
                          :children #{{:path :e :contributions {:joe 1}        :children #{}}
                                      {:path :f :contributions {:joe 1 :bob 2} :children #{}}}}}}]

  (is (= (tree-contributions tree conts)
         expected))))