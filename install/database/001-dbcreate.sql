-- run under urm user

-- recreate schema
DROP SCHEMA main CASCADE;

CREATE SCHEMA main;

-- initial create
CREATE SEQUENCE main.urm_object_seq
    INCREMENT 1
    START 1000
;
