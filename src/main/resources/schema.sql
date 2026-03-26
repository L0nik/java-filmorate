CREATE TABLE IF NOT EXISTS "film" (
  "id" integer PRIMARY KEY,
  "name" varchar NOT NULL,
  "description" varchar NOT NULL,
  "release_date" date NOT NULL,
  "duration" integer NOT NULL,
  "rating_id" integer
);

CREATE TABLE IF NOT EXISTS "user" (
  "id" integer PRIMARY KEY,
  "email" varchar NOT NULL,
  "login" varchar NOT NULL,
  "name" varchar,
  "birthday" date
);

CREATE TABLE IF NOT EXISTS "friendship" (
  "id" integer PRIMARY KEY,
  "requester_id" integer NOT NULL,
  "addressee_id" integer NOT NULL,
  "status" ENUM('pending', 'confirmed') NOT NULL
);

CREATE TABLE IF NOT EXISTS "genre" (
  "id" integer PRIMARY KEY,
  "name" varchar NOT NULL
);

CREATE TABLE IF NOT EXISTS "film_genre" (
  "id" integer PRIMARY KEY,
  "film_id" integer NOT NULL,
  "genre_id" integer NOT NULL
);

CREATE TABLE IF NOT EXISTS "film_likes" (
  "id" integer PRIMARY KEY,
  "film_id" integer NOT NULL,
  "user_id" integer NOT NULL
);

CREATE TABLE IF NOT EXISTS "rating_mpa" (
  "id" integer PRIMARY KEY,
  "name" ENUM('G', 'PG', 'PG_13', 'R', 'NC_17') NOT NULL
);

ALTER TABLE "film" ADD FOREIGN KEY ("rating_id") REFERENCES "rating_mpa" ("id");

ALTER TABLE "friendship" ADD FOREIGN KEY ("requester_id") REFERENCES "user" ("id");

ALTER TABLE "friendship" ADD FOREIGN KEY ("addressee_id") REFERENCES "user" ("id");

ALTER TABLE "film_genre" ADD FOREIGN KEY ("film_id") REFERENCES "film" ("id");

ALTER TABLE "film_genre" ADD FOREIGN KEY ("genre_id") REFERENCES "genre" ("id");

ALTER TABLE "film_likes" ADD FOREIGN KEY ("film_id") REFERENCES "film" ("id");

ALTER TABLE "film_likes" ADD FOREIGN KEY ("user_id") REFERENCES "user" ("id");