(ns hits.models.migration
  (:require [clojure.java.jdbc :as sql]))

(defn create-actions []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/create-table :actions
                      [:id "varchar(40)"]
                      [:action "varchar(4)"]
                      [:path "varchar(256)"])))  ;; should be textfield?

(defn -main []
  (print "Migrating database...") (flush)
  (create-actions)
    (println " done"))
