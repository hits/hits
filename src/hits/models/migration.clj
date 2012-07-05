(ns hits.models.migration
  (:require [clojure.java.jdbc :as sql]))

(defn db-get-tables []
  (into []
        (sql/resultset-seq
         (-> (sql/connection)
             (.getMetaData)
             (.getTables nil nil nil (into-array ["TABLE" "VIEW"]))))))

(defn table-names []
  (map :table_name (db-get-tables)))

(defn create-actions []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (when-not (some #{'actions} (table-names)))
    (sql/create-table :actions
                      [:id "varchar(40)"]
                      [:action "varchar(4)"]
                      [:path "varchar(256)"])))  ;; should be textfield?

(defn -main []
  (print "Migrating database...") (flush)
  (create-actions)
    (println " done"))
