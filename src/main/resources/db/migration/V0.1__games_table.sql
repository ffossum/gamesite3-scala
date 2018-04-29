CREATE TYPE game_status AS ENUM ('not_started', 'in_progress');

CREATE TABLE games (
    id serial PRIMARY KEY,
    created_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    host_id int REFERENCES users (id) NOT NULL,
    game_status game_status NOT NULL DEFAULT 'not_started'
);

CREATE TABLE games_users (
    game_id int REFERENCES games (id) ON DELETE CASCADE NOT NULL,
    user_id int REFERENCES users (id) ON DELETE CASCADE NOT NULL,
    CONSTRAINT games_users_key PRIMARY KEY (game_id, user_id)
);

CREATE VIEW games_view AS
    SELECT
        id,
        created_time,
        host_id,
        game_status,
        ARRAY(SELECT user_id FROM games_users where game_id=id) AS other_players
    FROM games;
