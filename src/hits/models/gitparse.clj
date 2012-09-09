(ns hits.models.gitparse
  (:require [clojure.string :as string])
  (:require [clojure.java.shell :as shell]))

;; git log 
(def git-fields {"id" "%H"
                 "author_name" "%an"
                 "author_email" "%ae"
                 "date" "%ad"
                 "timestamp" "%at"
                 "subject" "%s"
                 "body" "%B"})

(def line-terminator "\nline-terminator525724924752\n")
(def msg-terminator  "\n\nmessage-terminator525925825722825\n\n")

(def log-format (str (string/join line-terminator (vals git-fields))
                     msg-terminator))

(defn log-msg-to-map [msg]
  (zipmap (keys git-fields)
          (map string/trim (string/split msg (re-pattern line-terminator)))))

(def tempdir "./tmp/")
(defn dir-of-repo [user repo]
  (str tempdir user "--" repo))
(defn clone-repo [user repo]
  (shell/sh "git" "clone" 
            (str "git@github.com:" user "/" repo ".git") 
            (dir-of-repo user repo)))

(defn parse-log [user repo]
  (let [text (:out (shell/sh "git" "log" (format "--format=%s" log-format) 
                             :dir (dir-of-repo user repo)))
        msgs (string/split text (re-pattern msg-terminator))
        maps (map log-msg-to-map msgs)
        non-empty-maps (filter (fn [m] (contains? m "id")) maps)]
    non-empty-maps))
    
;; git whatchanged
(def whatchanged-format (str msg-terminator "%H"))

(defn parse-action-line [line]
  (let [words  (string/split line #"\s+")
        action (first words)
        path   (string/join " " (rest words))]
    [action path]))

(defn non-trivial-line [line]
  (not (= (string/trim line) "")))

(defn index-by-id [map-coll]
  (zipmap (map (fn [m] (m "id")) map-coll)
          map-coll))

(defn wc-msg-to-map [msg]
  (let [lines (string/split msg #"\n")
        id (first lines)
        action-lines (filter non-trivial-line (rest lines))
        actions-and-paths (map parse-action-line action-lines)]
    {id actions-and-paths}))

(defn parse-whatchanged [user repo]
  (let [text (:out (shell/sh "git" "whatchanged" 
                             (format "--format=%s" whatchanged-format)
                             "--name-status"
                             :dir (dir-of-repo user repo)))
        msgs (string/split text (re-pattern msg-terminator))
        good-msgs (filter non-trivial-line msgs)]
    (apply merge (map wc-msg-to-map good-msgs))))
        
(defn flatten-whatchanged-map [[id actions-and-paths]]
  (map (fn [[action path]] {"id" id "action" action "path" path})
       actions-and-paths))

;; whatchanged testing
(clone-repo "hits" "hits")
(def id-appair (first (parse-whatchanged "hits" "hits" )))
(def flat-map (flatten-whatchanged-map id-appair))
(def final-result (map flatten-whatchanged-map (parse-whatchanged "hits" "hits")))

;; log testing
(count (filter (fn [repo] (= (repo "author_email") "mrocklin@gmail.com")) 
               (parse-log "hits" "hits")))
