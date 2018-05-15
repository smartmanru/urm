
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

ALTER TABLE main.urm_product_lifecycle ADD CONSTRAINT urm_product_policy_urm_product_lifecycle_fk
FOREIGN KEY (product_id)
REFERENCES main.urm_product_policy (product_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
