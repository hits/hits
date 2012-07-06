(ns hits.models.migration
  (:require [clojure.java.jdbc :as sql])
  (:require [clj-time.format :as timef])
  (:require [clj-time.coerce :as timec]))

(def conn (System/getenv "DATABASE_URL"))

(defn db-get-tables []
  (into []
        (sql/resultset-seq
         (-> (sql/connection)
             (.getMetaData)
             (.getTables nil nil nil (into-array ["TABLE" "VIEW"]))))))

(defn table-names []
  (map :table_name (db-get-tables)))

(defn create-actions []
  (sql/with-connection conn
    (when-not (some #{"actions"} (table-names))
      (sql/create-table :actions
                        [:id "varchar(40)"]
                        [:action "varchar(4)"]
                        [:path "text"]))))

(defn drop-table [name]
  (sql/with-connection conn
    (sql/drop-table name)))

(defn test-action-insert []
  (sql/with-connection conn
    (sql/insert-record :actions {:id "0987135", :action "TEST", :path "/dev/null"})))

(defn select-all [table]
  (sql/with-connection conn
    (sql/with-query-results results [(format "select * from %s" table)] 
                            (into [] results))))
(defn create-log []
  (sql/with-connection conn
    (when-not (some #{"log"} (table-names))
      (sql/do-commands
        "CREATE TABLE log
        (
          author_email Varchar(128),
          author_name  Varchar(128),
          date         timestamp with time zone,
          id           Varchar(40) PRIMARY KEY,
          subject      text,
          timestamp    bigint,
          body         text
        );"))))

(defn test-log-insert []
  (sql/with-connection conn
    (sql/do-commands
      "INSERT INTO log (author_email, author_name, date, id, subject, timestamp, body)
      VALUES
      ('email@gmail.com',
       'Joe Schmoe',
       'Fri Jul 6 11:59:00 2012 -0500',
       '49937c8d5c9592bf94e4026d02f2206fe643228d',
       'added some functionality',
       '1341594990',
       'added function fn to file.clj');")))

(defn to-timestamp [s]
  (->> s
    (timef/parse (timef/formatter "EEE MMM dd HH:mm:ss yyyy Z"))
    timec/to-timestamp))
    
(defn test-log-insert-clj []
  (sql/with-connection conn
    (sql/insert-record :log {:author_email "email@gmail.com"
                             :author_name  "Joe Schmoe"
                             :date (to-timestamp
                                     "Fri Jul 6 11:59:00 2012 -0500")
                             :id "49937c8d5c9592bf94e4026d02f2206fe645555d"
                             :subject "added some functionality"
                             :timestamp 1341594990
                             :body "added function fn to file.clj"})))
                           
(defn -main []
  (println "Migrating database...") (flush)
  (println "Creating actions table...")(flush)
  (create-actions)
  (test-action-insert)
  (println "Creating log table...")(flush)
  (drop-table :log)
  (create-log)
  (test-log-insert)
  (test-log-insert-clj)
  (println " done.  Table contents:")
  (println (select-all "actions"))
  (println (select-all "log")))
