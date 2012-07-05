(ns hits.models.action
  (:require [clojure.java.jdbc :as sql]))

;; CRUD stuff here

;; (defn all []
;;   (sql/with-connection (System/getenv "DATABASE_URL")
;;     (sql/with-query-results results
;;       ["select * from shouts order by id desc"]
;;       (into [] results))))

;; (defn create [action]
;;   (sql/with-connection (System/getenv "DATABASE_URL")
;;         (sql/insert-values :actions [:body] [shout])))
