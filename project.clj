(defproject hits "0.0.1"
  :main hits.core
  :dependencies
  [[org.clojure/clojure "1.2.1"]
   [compojure "0.6.4"]
   [expectations "1.3.3"]
   [hiccup "0.3.6"]
   [org.clojure/java.jdbc "0.1.1"]
   [postgresql/postgresql "8.4-702.jdbc4"]
   [ring/ring-jetty-adapter "0.3.10"]
   [clj-time "0.4.3"]])
