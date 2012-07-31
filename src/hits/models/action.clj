(ns hits.models.action
  (:use expectations)
  (:require [clojure.java.jdbc :as sql]))

(defn hits-query [query]
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/with-query-results results [query]
      (into [] results))))



(defn files-touched-by-user [author-name]
  (hits-query 
    (format
       "SELECT path, count(path) as cnt
        FROM log INNER JOIN actions
        ON log.id = actions.id
        WHERE author_name = '%s'
        GROUP BY path
        ORDER BY cnt DESC" 
       author-name)))

(defn users-active-in-path [path]
  (hits-query 
    (format
       "SELECT author_name, count(author_name) as cnt 
        FROM log INNER JOIN actions
        ON log.id = actions.id
        WHERE path like '%s'
        GROUP BY author_name 
        ORDER BY cnt DESC"
       path)))

;; (defn create [action]
;;   (sql/with-connection (System/getenv "DATABASE_URL")
;;         (sql/insert-values :actions [:body] [shout])))
