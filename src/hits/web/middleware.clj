(ns hits.web.middleware
  (:require [ring.util.response :as resp]))

(defn wrap-slash [handler]
  ;; From http://stackoverflow.com/questions/12469914/
  ;; how-to-do-http-302-redirects-with-noir-web-framework"
  ;; Note caveat: "Please be careful with the first solution as I
  ;; think it breaks on URLs with parameters. – Ivan Koblik"
  (fn [{:keys [uri] :as req}]
    (if (.endsWith uri "/")
      (handler req)
      (resp/redirect (str uri "/")))))

;(defn wrap-slash [handler]
;  (fn [{:keys [uri] :as req}]
;    (if (.endsWith uri "/")
;      (handler (assoc req 
;                      :uri (.substring uri 
;                                       0 (dec (count uri)))))
;      (handler req))))