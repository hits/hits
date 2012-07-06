(ns hits.core
  (:use noir.core)
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:require [noir.server :as server]))

(def
  ^{:doc "FIXME: this should be a database model"}
  repos
  [["mrocklin" "sypy"]
   ["eigenhombre" "namejen"]
   ["clojure" "clojure"]
   ["django" "django"]])

(defpage "/" []
  (html [:h1 "Welcome to HITS (Hands in the Soup)"]
        [:p [:b "Available repos:"]]
        (map (fn [[name repo]] [:p (link-to "/" (str name "/" repo))]) repos)))


(defn -main []
  (let [port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port)))
