CREATE TABLE Commit_actions
(
    id Varchar(40) NOT NULL,
    action Varchar(4) NOT NULL,
    path Varchar(256) NOT NULL
);

CREATE TABLE Commit_log
(
    author_email Varchar(128),
    author_name  Varchar(128),
    date         timestamp,
    id           Varchar(40) PRIMARY KEY,
    subject      text,
    timestamp    bigint,
    body         text 
);
