CREATE TABLE language (
  id              INT(7)        NOT NULL AUTO_INCREMENT,
  cd              CHAR(2)       NOT NULL,
  description     VARCHAR(50),
  PRIMARY KEY (id)
);

CREATE TABLE author (
  id              INT(7)        NOT NULL AUTO_INCREMENT,
  first_name      VARCHAR(50),
  last_name       VARCHAR(50)   NOT NULL,
  date_of_birth   DATE,
  year_of_birth   INT(7),
  distinguished   INT(1),
  PRIMARY KEY (id)
);

CREATE TABLE book (
  id              INT(7)     NOT NULL AUTO_INCREMENT,
  author_id       INT(7)     NOT NULL,
  title           VARCHAR(400) NOT NULL,
  published_in    INT(7)     NOT NULL,
  language_id     INT(7)     NOT NULL,

  PRIMARY KEY (id),
  CONSTRAINT fk_book_author     FOREIGN KEY (author_id)   REFERENCES author(id),
  CONSTRAINT fk_book_language   FOREIGN KEY (language_id) REFERENCES language(id)
);

CREATE TABLE book_store (
  name            VARCHAR(400) NOT NULL UNIQUE
);

CREATE TABLE book_to_book_store (
  name            VARCHAR(400) NOT NULL,
  book_id         INT       NOT NULL,
  stock           INT,

  PRIMARY KEY(name, book_id),
  CONSTRAINT fk_b2bs_book_store FOREIGN KEY (name)        REFERENCES book_store (name) ON DELETE CASCADE,
  CONSTRAINT fk_b2bs_book       FOREIGN KEY (book_id)     REFERENCES book (id)         ON DELETE CASCADE
);

INSERT INTO language (id, cd, description) VALUES (1, 'en', 'English');
INSERT INTO language (id, cd, description) VALUES (2, 'de', 'Deutsch');
INSERT INTO language (id, cd, description) VALUES (3, 'fr', 'Français');
INSERT INTO language (id, cd, description) VALUES (4, 'pt', 'Português');

INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (1 , 'George'  , 'Orwell' , DATE '1903-06-26', 1903         );
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (2 , 'Paulo'   , 'Coelho' , DATE '1947-08-24', 1947         );
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (3 , 'Robert'  , 'Musil' , DATE '1903-06-26', 1903         );
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (4 , 'Claudia Simone'   , 'Dorchain' , DATE '1947-08-24', 1947);
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (5 , 'Georgia'  , 'Agamben' , DATE '1903-06-26', 1903);
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (6 , 'Leo'   , 'Tolstoi' , DATE '1947-08-24', 1947);
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (7 , 'Fjodor'  , 'Dostojewski' , DATE '1903-06-26', 1903);
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (8 , 'Franz'   , 'Kafka' , DATE '1947-08-24', 1947         );
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (9 , 'Hermann'  , 'Hesse' , DATE '1903-06-26', 1903         );
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (10 , 'Thomas'   , 'Mann' , DATE '1947-08-24', 1947         );
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (11 , 'Sybille'  , 'Berg' , DATE '1903-06-26', 1903         );
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (12 , 'Julia'   , 'Zeh' , DATE '1947-08-24', 1947         );
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (13 , 'Frank'  , 'Herbert' , DATE '1903-06-26', 1903);
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (14 , 'Michael'   , 'Hedges' , DATE '1947-08-24', 1947);
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (15 , 'Anna'  , 'Sutter' , DATE '1903-06-26', 1903         );
INSERT INTO author (id, first_name, last_name, date_of_birth    , year_of_birth)
  VALUES           (16 , 'Rainer'   , 'Mai' , DATE '1947-08-24', 1947         );



INSERT INTO book (id, author_id, title         , published_in, language_id)
  VALUES         (1 , 1        , '1984'        , 1948        , 1          );
INSERT INTO book (id, author_id, title         , published_in, language_id)
  VALUES         (2 , 1        , 'Animal Farm' , 1945        , 1          );
INSERT INTO book (id, author_id, title         , published_in, language_id)
  VALUES         (3 , 2        , 'O Alquimista', 1988        , 4          );
INSERT INTO book (id, author_id, title         , published_in, language_id)
  VALUES         (4 , 3        , 'Brida1'       , 1990        , 2          );
INSERT INTO book (id, author_id, title         , published_in, language_id)
  VALUES         (5 , 3        , 'Brida2'       , 1990        , 2          );
INSERT INTO book (id, author_id, title         , published_in, language_id)
  VALUES         (6 , 4        , 'Brida3'       , 1990        , 2          );
INSERT INTO book (id, author_id, title         , published_in, language_id)
  VALUES         (7 , 8        , 'Brida4'       , 1990        , 2          );
INSERT INTO book (id, author_id, title         , published_in, language_id)
  VALUES         (8 , 8        , 'Brida5'       , 1990        , 2          );
INSERT INTO book (id, author_id, title         , published_in, language_id)
  VALUES         (9 , 9        , 'Brida6'       , 1990        , 2          );
INSERT INTO book (id, author_id, title         , published_in, language_id)
  VALUES         (10 , 10        , 'Brida7'       , 1990        , 2          );



INSERT INTO book_store VALUES ('Orell Füssli');
INSERT INTO book_store VALUES ('Ex Libris');
INSERT INTO book_store VALUES ('Buchhandlung im Volkshaus');

INSERT INTO book_to_book_store VALUES ('Orell Füssli'             , 1, 10);
INSERT INTO book_to_book_store VALUES ('Orell Füssli'             , 2, 10);
INSERT INTO book_to_book_store VALUES ('Orell Füssli'             , 3, 10);
INSERT INTO book_to_book_store VALUES ('Ex Libris'                , 1, 1 );
INSERT INTO book_to_book_store VALUES ('Ex Libris'                , 3, 2 );
INSERT INTO book_to_book_store VALUES ('Buchhandlung im Volkshaus', 3, 1 );
