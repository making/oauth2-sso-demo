CREATE TABLE IF NOT EXISTS users
(
    username VARCHAR(50)  NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled  BOOLEAN      NOT NULL
);

CREATE TABLE IF NOT EXISTS authorities
(
    username  VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users (username)
);
CREATE UNIQUE INDEX IF NOT EXISTS ix_auth_username ON authorities (username, authority);

CREATE TABLE IF NOT EXISTS groups
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    group_name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS group_authorities
(
    group_id  INTEGER     NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_group_authorities_group FOREIGN KEY (group_id) REFERENCES groups (id)
);

CREATE TABLE IF NOT EXISTS group_members
(
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL,
    group_id INTEGER     NOT NULL,
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES groups (id)
);