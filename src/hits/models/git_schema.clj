(ns hits.models.git-schema
  (:require [datomic.api :only [db] :as d]))

(def schema 
[
 {:db/id #db/id[:db.part/db]
  :db/ident :git.log/id
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/unique :db.unique/identity
  :db/index true
  :db/doc "The Hash of a commit - Unique 40 character string"
  :db.install/_attribute :db.part/db}

 {:db/id #db/id[:db.part/db]
  :db/ident :git.log/subject
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/doc "The subject line of a commit"
  :db.install/_attribute :db.part/db}
 
 {:db/id #db/id[:db.part/db]
  :db/ident :git.log/body
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/doc "The body of a commit"
  :db.install/_attribute :db.part/db}
 
 {:db/id #db/id[:db.part/db]
  :db/ident :git.log/author-name
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/doc "The name of the author of a commit"
  :db.install/_attribute :db.part/db}
 
 {:db/id #db/id[:db.part/db]
  :db/ident :git.log/author-email
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/doc "The email of the author of a commit"
  :db.install/_attribute :db.part/db}
 
 {:db/id #db/id[:db.part/db]
  :db/ident :git.log/date
  :db/valueType :db.type/instant
  :db/cardinality :db.cardinality/one
  :db/doc "The time a commit was made"
  :db.install/_attribute :db.part/db}

 {:db/id #db/id[:db.part/db]
  :db/ident :git.log/timestamp
  :db/valueType :db.type/long
  :db/cardinality :db.cardinality/one
  :db/doc "The timestamp a commit was made"
  :db.install/_attribute :db.part/db}

  {:db/id #db/id[:db.part/db]
  :db/ident :git.log/owner
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/doc "The username of the owner of the repository"
  :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
  :db/ident :git.log/repo
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/doc "The name of the repository"
  :db.install/_attribute :db.part/db}

 {:db/id #db/id[:db.part/db]
  :db/ident :git.change/action
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/doc "The type of change A for add, M for modify, D for delete"
  :db.install/_attribute :db.part/db}
 
 {:db/id #db/id[:db.part/db]
  :db/ident :git.change/file
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/doc "The file that was changed"
  :db.install/_attribute :db.part/db}
 
 {:db/id #db/id[:db.part/db]
  :db/ident :git.change/commit-id
  :db/index true
  :db/valueType :db.type/string
  :db/cardinality :db.cardinality/one
  :db/doc "The commit on which this change was made"
  :db.install/_attribute :db.part/db}
 ]
)
