
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


ALTER TABLE main.urm_product ADD CONSTRAINT urm_system_product_fk
FOREIGN KEY (system_id)
REFERENCES main.urm_system (system_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
