(ns hits.web-test
  (:require [noir.core :as noir])
  (:require [datomic.api :only [q db] :as d])
  (:use hits.models.datomic)
  (:require [hiccup.core :as hicc])
  (:require [hits.models.git-schema :only [schema] :as git] )
  (:require [hiccup.page-helpers :as page])
  (:require [noir.server :as server]))

(def start-repos
  [["eigenhombre" "namejen"]
   ["hits" "hits"]])

(defn do-repos! [conn repos]
  (apply concat (map (fn [[name proj]] (add-repo-to-db conn name proj)) repos)))

(defn setup-datomic! [uri]
    (d/create-database uri)
    (let [conn (d/connect uri)]
      @(d/transact conn git/schema)
      conn))

(def conn (let [c (setup-datomic! "datomic:mem://hits-live")]
            (do-repos! c start-repos)
            c))

(defn link-for [name repo] (format "/%s/%s/" name repo))

(noir/defpage "/" []
  (hicc/html [:h1 "Welcome to HITS (Hands in the Soup)"]
             [:p [:b "Available repos:"]]
             (map (fn [[name repo]]
                    [:p (page/link-to (link-for name repo)
                                      (str name "/" repo))]) (current-repos conn))
             [:p "Or visit /owner/repo of your choice"]))

(noir/defpage "/:name/:repo" {:keys [name repo]}
  (when (not (contains? (current-repos conn) [name repo]))
    (datomic.common/await-derefs (add-repo-to-db conn name repo)))
  (hicc/html [:h1 (format "%s/%s" name repo)]
             (map (fn [[author counts]] [:pre author " " counts])
                  (reverse (sort-by second (author-activity name repo "" conn))))))

;; run (web-main) at REPL to launch test server:
(defn web-main []
  (let [port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port)))
(defn -main []
  (web-main))
