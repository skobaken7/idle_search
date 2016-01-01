# idles schema
 
# --- !Ups

CREATE TABLE idles (
  id VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  kana VARCHAR(255),
  birth DATETIME,
  height SMALLINT,
  weight SMALLINT,
  bust SMALLINT,
  waist SMALLINT,
  hip SMALLINT,
  cup CHAR(1),
  PRIMARY KEY(id)
);

# --- !Downs

DROP TABLE idles;
