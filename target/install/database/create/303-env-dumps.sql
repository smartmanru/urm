
CREATE TABLE main.urm_env_dbdump (
                dump_id INTEGER NOT NULL,
                env_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                server_id INTEGER NOT NULL,
                modeexport BOOLEAN NOT NULL,
                dataset VARCHAR NOT NULL,
                owntableset BOOLEAN NOT NULL,
                dumpdir VARCHAR NOT NULL,
                remote_setdbenv VARCHAR,
                database_datapumpdir VARCHAR,
                postrefresh VARCHAR,
                schedule VARCHAR,
                usestandby BOOLEAN NOT NULL,
                usenfs BOOLEAN NOT NULL,
                offline BOOLEAN NOT NULL,
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_dbdump_pk PRIMARY KEY (dump_id)
);
COMMENT ON TABLE main.urm_env_dbdump IS 'Database dump';


CREATE TABLE main.urm_env_tablemask (
                tablemask_id INTEGER NOT NULL,
                env_id INTEGER NOT NULL,
                dump_id INTEGER NOT NULL,
                modeinclude BOOLEAN NOT NULL,
                schema_fkid INTEGER,
                schema_fkname VARCHAR(30),
                tablemask VARCHAR NOT NULL,
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_tablemask_pk PRIMARY KEY (tablemask_id)
);
COMMENT ON TABLE main.urm_env_tablemask IS 'Database dump table set table mask';


ALTER TABLE main.urm_env_tablemask ADD CONSTRAINT urm_env_dbdump_urm_env_tablemask_fk
FOREIGN KEY (dump_id)
REFERENCES main.urm_env_dbdump (dump_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
