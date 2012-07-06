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
                        [:path "text"]))))

(defn test-action-insert []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/insert-record :actions {:id "0987135", :action "TEST", :path "/dev/null"})))

(defn all-actions []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/with-query-results results ["select * from actions"] (into [] results))))

(defn create-log []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (when-not (some #{"log"} (table-names))
      (sql/do-commands
        "CREATE TABLE log
        (
          author_email Varchar(128),
          author_name  Varchar(128),
          date         timestamp,
          id           Varchar(40) PRIMARY KEY,
          subject      text,
          timestamp    bigint,
          body         text
        );"))))

(defn test-log-insert []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/insert-record :log {:author_email "email@gmail.com"
                             :author_name  "Joe Schmoe"
                             :date "Fri Jul 6 11:59:00 2012 -0500"
                             :id "49937c8d5c9592bf94e4026d02f2206fe643228d"
                             :subject "added some functionality"
                             :timestamp "1341594990"
                             :body "added function fn to file.clj"})))
                           
(defn all-log []
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/with-query-results results ["select * from log"] (into [] results))))

(defn -main []
  (println "Migrating database...") (flush)
  (println "Creating actions table...")(flush)
  (create-actions)
  (test-action-insert)
  (println "Creating log table...")(flush)
  (create-log)
  (test-log-insert)
  (println " done.  Table contents:")
  (println (all-actions))
  (println (all-log)))
