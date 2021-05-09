CREATE database sns_db WITH
    OWNER developer
    TEMPLATE template0
    ENCODING 'UTF-8'
    LC_COLLATE 'ja_JP.utf8'
    LC_CTYPE 'ja_JP.utf8'
    TABLESPACE default
    connection limit -1;

\connect sns_db

create table users
(
    id         uuid                                not null
        constraint users_pkey
            primary key,
    mail       varchar(100)                        not null,
    name       varchar(100)                        not null,
    birthday   date                                not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null
);

create table tweets
(
    id         uuid                                not null
        constraint tweets_pkey
            primary key,
    user_id    uuid                                not null
        constraint tweets_user_id_fkey
            references users
            on delete cascade,
    text       varchar(500),
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null
);

create table comments
(
    id         uuid                                not null
        constraint comments_pkey
            primary key,
    user_id    uuid                                not null
        constraint comments_user_id_fkey
            references users
            on delete cascade,
    tweet_id   uuid                                not null
        constraint comments_tweet_id_fkey
            references tweets
            on delete cascade,
    text       varchar(500)                        not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null
);

create table tags
(
    id   uuid         not null
        constraint tags_pkey
            primary key,
    name varchar(100) not null
);

create table tag_tweet_map
(
    id       uuid not null
        constraint tag_tweet_map_pkey
            primary key,
    tag_id   uuid not null
        constraint tag_tweet_map_tag_id_fkey
            references tags
            on delete cascade,
    tweet_id uuid not null
        constraint tag_map_tweet_id_fkey
            references tweets
            on delete cascade
);


create table tag_comment_map
(
    id         uuid not null
        constraint tag_comment_map_pkey
            primary key,
    tag_id     uuid not null
        constraint tag_comment_map_tag_id_fkey
            references tags
            on delete cascade,
    comment_id uuid not null
        constraint tag_map_comment_id_fkey
            references comments
            on delete cascade
);
