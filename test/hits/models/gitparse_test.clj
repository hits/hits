(ns hits.models.gitparse-test
  (:use clojure.test
        hits.models.gitparse))

(defn setup []
  (clone-repo "hits" "hits-test"))
  
(setup)
 
(def log (parse-log "hits" "hits-test"))
(def log-map (index-by-id log))


(deftest test-unpack-whatchanged
  (is (= (unpack-whatchanged {"abcd" [["A" "file1"] ["M" "file2"]]
                              "dcba" [["M" "file1"]]})
         #{{"commit-id" "abcd" "action" "A" "file" "file1"}
           {"commit-id" "abcd" "action" "M" "file" "file2"}
           {"commit-id" "dcba" "action" "M" "file" "file1"}})))
(test-unpack-whatchanged)

(def whatchanged (parse-whatchanged "hits" "hits-test"))

(def c1 (log-map "fcd206b7ba561ed12641328bc6da8b3a494deabb"))
(deftest first-commit-log
  (is (= (c1 "id") "fcd206b7ba561ed12641328bc6da8b3a494deabb"))
  (is (= (c1 "subject") "First commit"))
  (is (= (c1 "author_name" "Matthew Rocklin"))))
(deftest first-commit-wc
  (is (= (whatchanged (c1 "id")) [["A" "README"]])))
(deftest first-commit
  (first-commit-log)
  (first-commit-wc))

(def c2 (log-map "cf55be357e7fe6837faa530d53fe5d516627c6a7"))
(deftest second-commit-log
  (is (= (c2 "id") "cf55be357e7fe6837faa530d53fe5d516627c6a7"))
  (is (= (c2 "subject") "Second commit - contribute text to readme"))
  (is (= (c2 "author_name" "Matthew Rocklin"))))
(deftest second-commit-wc
  (is (= (whatchanged (c2 "id")) [["M" "README"]])))
(deftest second-commit
  (second-commit-log)
  (second-commit-wc))

(deftest testall
  (first-commit)
  (second-commit))
(testall)