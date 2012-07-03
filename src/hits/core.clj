(ns hits.core
  (:use [compojure.core :only [defroutes GET]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.adapter.jetty :as ring]))

(defn app [req]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello from Clojure!\n"})

(defroutes routes
  (GET  "/" [] "it still works"))
  ; (route/resources "/"))

(def application (handler/site routes))

(defn start [port]
  (ring/run-jetty #'application {:port (or port 8080) :join? false}))

(defn -main []
  (let [port (Integer. (System/getenv "PORT"))]
        (start port)))
