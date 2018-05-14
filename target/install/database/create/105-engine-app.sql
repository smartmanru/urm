
CREATE TABLE main.urm_system (
                system_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                offline BOOLEAN NOT NULL,
                matched BOOLEAN NOT NULL,
                sv INTEGER NOT NULL,
                CONSTRAINT urm_system_pk PRIMARY KEY (system_id)
);
COMMENT ON TABLE main.urm_system IS 'Application system';


CREATE TABLE main.urm_product (
                product_id INTEGER NOT NULL,
                system_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                path VARCHAR NOT NULL,
                xdesc VARCHAR,
                offline BOOLEAN NOT NULL,
                monitoring_enabled BOOLEAN NOT NULL,
                last_major1 INTEGER NOT NULL,
                last_major2 INTEGER NOT NULL,
                last_minor1 INTEGER NOT NULL,
                last_minor2 INTEGER NOT NULL,
                next_major1 INTEGER NOT NULL,
                next_major2 INTEGER NOT NULL,
                next_minor1 INTEGER NOT NULL,
                next_minor2 INTEGER NOT NULL,
                sv INTEGER NOT NULL,
                CONSTRAINT urm_product_pk PRIMARY KEY (product_id)
);
COMMENT ON TABLE main.urm_product IS 'Application product';


CREATE TABLE main.urm_product_montarget (
                montarget_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                target_fkenv VARCHAR(30),
                target_fksg VARCHAR(30),
                target_fkid INTEGER,
                sv INTEGER NOT NULL,
                CONSTRAINT urm_product_montarget_pk PRIMARY KEY (montarget_id)
);
COMMENT ON TABLE main.urm_product_montarget IS 'Product monitoring target';


CREATE TABLE main.urm_product_monitem (
                monitem_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                montarget_id INTEGER NOT NULL,
                monitem_type INTEGER NOT NULL,
                url VARCHAR,
                xdesc VARCHAR,
                wsdata VARCHAR,
                wscheck VARCHAR,
                sv INTEGER NOT NULL,
                CONSTRAINT urm_product_monitem_pk PRIMARY KEY (monitem_id)
);
COMMENT ON TABLE main.urm_product_monitem IS 'Product monitoring item';


CREATE TABLE main.urm_product_dbdump (
                dump_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                db_fkid INTEGER,
                db_fkenv VARCHAR(30),
                db_fksg VARCHAR(30),
                db_fkserver VARCHAR(30),
                modeexport BOOLEAN NOT NULL,
                dataset VARCHAR NOT NULL,
                owntableset BOOLEAN NOT NULL,
                dumpdir VARCHAR,
                postrefresh VARCHAR,
                remote_setdbenv VARCHAR,
                database_datapumpdir VARCHAR,
                schedule VARCHAR,
                usenfs BOOLEAN NOT NULL,
                usestandby BOOLEAN NOT NULL,
                offline BOOLEAN NOT NULL,
                sv INTEGER NOT NULL,
                CONSTRAINT urm_product_dbdump_pk PRIMARY KEY (dump_id)
);
COMMENT ON TABLE main.urm_product_dbdump IS 'Product export and import dumps';


CREATE TABLE main.urm_product_tablemask (
                tablemask_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                dump_id INTEGER NOT NULL,
                modeinclude BOOLEAN NOT NULL,
                schema_fkid INTEGER,
                schema_fkname VARCHAR(30),
                tablemask VARCHAR NOT NULL,
                sv INTEGER NOT NULL,
                CONSTRAINT urm_product_tablemask_pk PRIMARY KEY (tablemask_id)
);
COMMENT ON TABLE main.urm_product_tablemask IS 'Product dump table group';


CREATE TABLE main.urm_product_policy (
                product_id INTEGER NOT NULL,
                lcurgent_any BOOLEAN NOT NULL,
                sv INTEGER NOT NULL,
                CONSTRAINT urm_product_policy_pk PRIMARY KEY (product_id)
);
COMMENT ON TABLE main.urm_product_policy IS 'Instance product policy';


CREATE TABLE main.urm_product_lifecycle (
                product_id INTEGER NOT NULL,
                lifecycle_id INTEGER NOT NULL,
                sv INTEGER NOT NULL,
                CONSTRAINT urm_product_lifecycle_pk PRIMARY KEY (product_id, lifecycle_id)
);
COMMENT ON TABLE main.urm_product_lifecycle IS 'Product lifecycle by policy';


ALTER TABLE main.urm_product ADD CONSTRAINT urm_system_product_fk
FOREIGN KEY (system_id)
REFERENCES main.urm_system (system_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_product_policy ADD CONSTRAINT urm_product_urm_product_policy_fk
FOREIGN KEY (product_id)
REFERENCES main.urm_product (product_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_product_dbdump ADD CONSTRAINT urm_product_urm_product_dbdump_fk
FOREIGN KEY (product_id)
REFERENCES main.urm_product (product_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_product_montarget ADD CONSTRAINT urm_product_urm_product_montarget_fk
FOREIGN KEY (product_id)
REFERENCES main.urm_product (product_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_product_tablemask ADD CONSTRAINT urm_product_urm_product_tablemask_fk
FOREIGN KEY (product_id)
REFERENCES main.urm_product (product_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_product_monitem ADD CONSTRAINT urm_product_urm_product_monitem_fk
FOREIGN KEY (product_id)
REFERENCES main.urm_product (product_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_product_monitem ADD CONSTRAINT urm_product_montarget_urm_env_monitem_fk
FOREIGN KEY (montarget_id)
REFERENCES main.urm_product_montarget (montarget_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_product_tablemask ADD CONSTRAINT urm_product_dbdump_urm_product_tablemask_fk
FOREIGN KEY (dump_id)
REFERENCES main.urm_product_dbdump (dump_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_product_lifecycle ADD CONSTRAINT urm_product_policy_urm_product_lifecycle_fk
FOREIGN KEY (product_id)
REFERENCES main.urm_product_policy (product_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
