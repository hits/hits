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
    (when-not (some #{"actions"} (table-names))
      (sql/create-table :actions
                        [:id "varchar(40)"]
                        [:action "varchar(4)"]
                        [:path "varchar(256)"]))))  ;; should be textfield?

(defn test-action-insert []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/insert-record :actions {:id "0987135", :action "TEST", :path "/dev/null"})))

(defn all-actions []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/with-query-results results ["select * from actions"] (into [] results))))

(defn -main []
  (print "Migrating database...") (flush)
  (create-actions)
  (test-action-insert)
  (println " done.  Table contents:")
  (println (all-actions)))
