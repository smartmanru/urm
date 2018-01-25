
CREATE TABLE main.urm_product_meta (
                meta_id INTEGER NOT NULL,
                product_fkid INTEGER,
                product_fkname VARCHAR(30),
                matched BOOLEAN NOT NULL,
                last_major1 INTEGER NOT NULL,
                last_major2 INTEGER NOT NULL,
                last_minor1 INTEGER NOT NULL,
                last_minor2 INTEGER NOT NULL,
                next_major1 INTEGER NOT NULL,
                next_major2 INTEGER NOT NULL,
                next_minor1 INTEGER NOT NULL,
                next_minor2 INTEGER NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_product_meta_pk PRIMARY KEY (meta_id)
);
COMMENT ON TABLE main.urm_product_meta IS 'Product';


CREATE TABLE main.urm_product_policy (
                meta_id INTEGER NOT NULL,
                lcurgent_any BOOLEAN NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_product_policy_pk PRIMARY KEY (meta_id)
);
COMMENT ON TABLE main.urm_product_policy IS 'Product policy';


CREATE TABLE main.urm_product_lifecycle (
                meta_id INTEGER NOT NULL,
                lc_index INTEGER NOT NULL,
                lifecycle_fkid INTEGER,
                lifecycle_fkname VARCHAR(64),
                pv INTEGER NOT NULL,
                CONSTRAINT urm_product_lifecycle_pk PRIMARY KEY (meta_id, lc_index)
);
COMMENT ON TABLE main.urm_product_lifecycle IS 'Product lifecycle';


CREATE TABLE main.urm_source_set (
                srcset_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_source_set_pk PRIMARY KEY (srcset_id)
);
COMMENT ON TABLE main.urm_source_set IS 'Product source project set';


CREATE TABLE main.urm_product_schema (
                schema_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                dbms_type INTEGER NOT NULL,
                dbname VARCHAR(64),
                dbuser VARCHAR(64),
                pv INTEGER NOT NULL,
                CONSTRAINT urm_product_schema_pk PRIMARY KEY (schema_id)
);
COMMENT ON TABLE main.urm_product_schema IS 'Product database logical schema';


CREATE TABLE main.urm_product_unit (
                unit_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_product_unit_pk PRIMARY KEY (unit_id)
);
COMMENT ON TABLE main.urm_product_unit IS 'Product unit';


CREATE TABLE main.urm_source_project (
                project_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                srcset_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                project_pos INTEGER NOT NULL,
                project_type INTEGER NOT NULL,
                codebase_prod BOOLEAN NOT NULL,
                unit_id INTEGER,
                tracker VARCHAR(30),
                branch VARCHAR(64),
                builder_fkid INTEGER,
                builder_fkname VARCHAR(64),
                builder_options VARCHAR,
                mirror_fkid INTEGER,
                mirror_fkresource VARCHAR(64),
                mirror_fkrepository VARCHAR(64),
                mirror_fkrepopath VARCHAR,
                mirror_fkcodepath VARCHAR,
                custom_build BOOLEAN NOT NULL,
                custom_get BOOLEAN NOT NULL,
                pv VARCHAR NOT NULL,
                CONSTRAINT urm_source_project_pk PRIMARY KEY (project_id)
);
COMMENT ON TABLE main.urm_source_project IS 'Product source project';


CREATE TABLE main.urm_source_item (
                srcitem_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                project_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                sourceitem_type INTEGER NOT NULL,
                basename VARCHAR(64) NOT NULL,
                ext VARCHAR(30),
                staticext VARCHAR(30),
                artefact_path VARCHAR,
                fixed_version VARCHAR(30),
                nodist BOOLEAN NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_source_item_pk PRIMARY KEY (srcitem_id)
);
COMMENT ON TABLE main.urm_source_item IS 'Product source project item';


ALTER TABLE main.urm_product_unit ADD CONSTRAINT urm_product_unit_fk
FOREIGN KEY (meta_id)
REFERENCES main.urm_product_meta (meta_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_product_schema ADD CONSTRAINT urm_product_schema_fk
FOREIGN KEY (meta_id)
REFERENCES main.urm_product_meta (meta_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_source_set ADD CONSTRAINT urm_product_source_set_fk
FOREIGN KEY (meta_id)
REFERENCES main.urm_product_meta (meta_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_product_policy ADD CONSTRAINT urm_product_meta_urm_product_policy_fk
FOREIGN KEY (meta_id)
REFERENCES main.urm_product_meta (meta_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_product_lifecycle ADD CONSTRAINT urm_product_policy_urm_product_lifecycle_fk
FOREIGN KEY (meta_id)
REFERENCES main.urm_product_policy (meta_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_source_project ADD CONSTRAINT urm_source_set_project_fk
FOREIGN KEY (srcset_id)
REFERENCES main.urm_source_set (srcset_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_source_project ADD CONSTRAINT urm_unit_source_project_fk
FOREIGN KEY (unit_id)
REFERENCES main.urm_product_unit (unit_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_source_item ADD CONSTRAINT urm_source_project_item_fk
FOREIGN KEY (project_id)
REFERENCES main.urm_source_project (project_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
