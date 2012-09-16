(ns hits.models.gitparse
  (:require [clojure.string :as string])
  (:require [clojure.java.shell :as shell]))

;-------------------------
; git log
;-------------------------

; The fields we want to pull out of git log and their format strings
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
  "Parse a single commit returned by git log"
  (zipmap (keys git-fields)
          (map string/trim (string/split msg (re-pattern line-terminator)))))

(defn str-identifier [user repo]
  "Identification string for a particular user and repo"
  (str user "--" repo))

(def tempdir "./tmp/")
(defn dir-of-repo [user repo]
  "The directory in which we clone a repo"
  (str tempdir (str-identifier user repo)))

(defn clone-repo [user repo]
  "Clone a complete git repository. Store in temporary directory. 
  This function is idempotent (feel free to use it many times at no added cost)"
  (shell/sh "mkdir" tempdir) ; idempotent
  (shell/sh "git" "clone" 
            (str "git@github.com:" user "/" repo ".git") 
            (dir-of-repo user repo)))

(defn parse-log [user repo]
  "Collect information about a users repository by running git log.
  Returns a seq of maps."
  (let [text (:out (shell/sh "git" "log" (format "--format=%s" log-format) 
                             :dir (dir-of-repo user repo)))
        msgs (string/split text (re-pattern msg-terminator))
        maps (map log-msg-to-map msgs)
        non-empty-maps (filter (fn [m] (contains? m "id")) maps)]
    non-empty-maps))

;-------------------------
; git whatchanged 
;-------------------------

(def whatchanged-format (str msg-terminator "%H"))

(defn parse-action-line [line]
  "Return the action and path from a particular line in git whatchanged"
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
  "Convert a single whatchanged message into a map"
  (let [lines (string/split msg #"\n")
        id (first lines)
        action-lines (filter non-trivial-line (rest lines))
        actions-and-paths (map parse-action-line action-lines)]
    {id actions-and-paths}))

(defn parse-whatchanged [user repo]
  "Collect information about a users repository by running git whatchanged.
  Returns a seq of maps."
  (let [text (:out (shell/sh "git" "whatchanged" 
                             (format "--format=%s" whatchanged-format)
                             "--name-status"
                             :dir (dir-of-repo user repo)))
        msgs (string/split text (re-pattern msg-terminator))
        good-msgs (filter non-trivial-line msgs)]
    (apply merge (map wc-msg-to-map good-msgs))))
        
(defn flatten-whatchanged-map [[id actions-and-paths]]
  "Convert a map like {id: [[action path] [action path]]} 
  into a set of named maps holding id, action, and path"
  (set (map (fn [[action path]] {"commit-id" id "action" action "file" path})
       actions-and-paths)))

(defn unpack-whatchanged [wc-map]
  "Turn whatchanged hierarchical map into a flattened set of maps"
  (apply clojure.set/union (map flatten-whatchanged-map wc-map)))
