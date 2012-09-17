(defproject hits "0.0.1"
  :main hits.core
  :dependencies
  [[org.clojure/clojure "1.4.0"]
   [compojure "0.6.4"]
   [expectations "1.3.3"]
   [hiccup "0.3.6"]
   [noir "1.2.2" :exclusions [org.clojure/clojure]]
   [org.clojure/java.jdbc "0.1.1"]
   [ring/ring-jetty-adapter "0.3.10"]
   [clj-time "0.4.3"]
   [com.datomic/datomic-free "0.8.3479"]]
  :dev-dependencies [[lein-autoexpect "0.1.1"]])
