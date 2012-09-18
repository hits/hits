(ns hits.web.middleware
  (:require [ring.util.response :as resp]))

(defn wrap-slash [handler]
  ;; From http://stackoverflow.com/questions/12469914/
  ;; how-to-do-http-302-redirects-with-noir-web-framework"
  (fn [{:keys [uri] :as req}]
    (if (.endsWith uri "/")
      (handler req)
      (resp/redirect (str uri "/")))))

