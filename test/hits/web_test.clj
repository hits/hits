(ns hits.web-test
  (:require [noir.core :as noir])
  (:require [datomic.api :only [q db] :as d])
  (:use hits.models.datomic)
  (:require [hiccup.core :as hicc])
  (:require [hits.models.git-schema :only [schema] :as git] )
  (:require [hiccup.page-helpers :as page])
  (:require [noir.server :as server]))

(def repos
  [["eigenhombre" "namejen"]
   ["hits" "hits"]])

(defn do-repos! [conn repos]
  (apply concat (map (fn [[name proj]] (add-repo-to-db conn name proj)) repos)))

(defn setup_datomic!
  [repos]
  (let [uri "datomic:mem://hits-test"]
    (d/create-database uri)
    (let [conn (d/connect uri)]
      @(d/transact conn git/schema)
      (datomic.common/await-derefs (do-repos! conn repos))
      conn)))

(def conn (setup_datomic! repos))

(author-activity "eigenhombre" "namejen" "src/namejen" conn)

(noir/defpage "/" []
  (hicc/html [:h1 "Welcome to HITS (Hands in the Soup)"]
             [:p [:b "Available repos:"]]
             (map (fn [[name repo]] 
                    [:p (page/link-to "/" (str name "/" repo))]) repos)))

;; run (web-main) at REPL to launch test server:
(defn web-main []
  (let [port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port)))
