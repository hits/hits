(defproject hits "0.0.1"
  :main hits.core
  :dependencies
  [[org.clojure/clojure "1.2.1"]
   [postgresql/postgresql "8.4-702.jdbc4"]
   [noir "1.2.2"]
   [org.clojure/java.jdbc "0.1.1"]
   [ring/ring-jetty-adapter "0.3.10"]
   [compojure "0.6.4"]
   [hiccup "0.3.6"]
   [clj-time "0.4.3"]]
  :dev-dependencies [[expectations "1.3.3"]
                     [lein-autoexpect "0.1.1"]])
