-- run under urm user

-- recreate schema
DROP SCHEMA IF EXISTS main CASCADE;

CREATE SCHEMA main;

-- initial create
CREATE SEQUENCE main.urm_object_seq
    INCREMENT 10
    START 1000
;
