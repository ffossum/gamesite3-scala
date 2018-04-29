CREATE EXTENSION citext;

CREATE TABLE users (
    id serial PRIMARY KEY,
    created_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    username citext NOT NULL UNIQUE,
    email citext NOT NULL UNIQUE,
    password_hash text NOT NULL
);

CREATE UNIQUE INDEX ON users (id);
CREATE UNIQUE INDEX ON users (email);
