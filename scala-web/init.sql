set timezone to 'Asia/Chongqing';
create user scala with nosuperuser
  replication
  encrypted password 'scala.2018';
create database scaladb owner = scala template = template0 encoding = 'UTF-8' lc_ctype = 'zh_CN.UTF-8' lc_collate = 'zh_CN.UTF-8';

-- create extension
\c scaladb;
create extension adminpack;
create extension hstore;

-- create tables ....

create table book (
  id          bigserial primary key,
  isbn        varchar(64)  not null,
  title       varchar(128) not null,
  author      int []       not null,
  description text,
  created_at  timestamptz,
  updated_at  timestamptz
);
create unique index book_uidx_isbn
  on book (isbn);

create table author (
  id          serial primary key,
  name        varchar(128) not null,
  age         int,
  description text,
  created_at  timestamptz,
  updated_at  timestamptz
);

-- change tables, views, sequences owner to scala
DO $$DECLARE r record;
BEGIN
  FOR r IN SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'
  LOOP
    EXECUTE 'alter table ' || r.table_name || ' owner to scala;';
  END LOOP;
END$$;

DO $$DECLARE r record;
BEGIN
  FOR r IN select sequence_name from information_schema.sequences where sequence_schema = 'public'
  LOOP
    EXECUTE 'alter sequence ' || r.sequence_name || ' owner to scala;';
  END LOOP;
END$$;

DO $$DECLARE r record;
BEGIN
  FOR r IN select table_name from information_schema.views where table_schema = 'public'
  LOOP
    EXECUTE 'alter table ' || r.table_name || ' owner to scala;';
  END LOOP;
END$$;
-- grant all privileges on all tables in schema public to scala;
-- grant all privileges on all sequences in schema public to scala;

