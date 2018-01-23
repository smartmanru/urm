
CREATE TABLE main.urm_dist_comp (
                comp_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                desc_1 VARCHAR,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_dist_comp_pk PRIMARY KEY (comp_id)
);
COMMENT ON TABLE main.urm_dist_comp IS 'Product deployment component';


CREATE TABLE main.urm_dist_delivery (
                delivery_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                unit_id INTEGER,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                folder VARCHAR NOT NULL,
                schema_any BOOLEAN NOT NULL,
                doc_any BOOLEAN NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_dist_delivery_pk PRIMARY KEY (delivery_id)
);
COMMENT ON TABLE main.urm_dist_delivery IS 'Product distributive delivery';


CREATE TABLE main.urm_dist_docitem (
                delivery_id INTEGER NOT NULL,
                doc_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_dist_docitem_pk PRIMARY KEY (delivery_id, doc_id)
);
COMMENT ON TABLE main.urm_dist_docitem IS 'Product distributive delivery document item';


CREATE TABLE main.urm_dist_confitem (
                confitem_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                delivery_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                confitem_type INTEGER NOT NULL,
                files VARCHAR,
                templates VARCHAR,
                secured VARCHAR,
                exclude VARCHAR,
                extconf VARCHAR,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_dist_confitem_pk PRIMARY KEY (confitem_id)
);
COMMENT ON TABLE main.urm_dist_confitem IS 'Product distributive configuration item';


CREATE TABLE main.urm_dist_schemaitem (
                delivery_id INTEGER NOT NULL,
                schema_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_dist_schemaitem_pk PRIMARY KEY (delivery_id, schema_id)
);
COMMENT ON TABLE main.urm_dist_schemaitem IS 'Product distributive schema';


CREATE TABLE main.urm_dist_binaryitem (
                binary_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                delivery_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                distitem_type INTEGER NOT NULL,
                basename_dist VARCHAR(64) NOT NULL,
                basename_deploy VARCHAR(64) NOT NULL,
                ext VARCHAR(30) NOT NULL,
                deployversion_type INTEGER NOT NULL,
                itemorigin_type INTEGER NOT NULL,
                srcitem_id INTEGER,
                src_binary_id INTEGER,
                src_itempath VARCHAR,
                archive_files VARCHAR,
                archive_files_1 VARCHAR,
                war_staticext VARCHAR(30),
                war_context VARCHAR(30),
                custom_get BOOLEAN NOT NULL,
                custom_deploy BOOLEAN NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_dist_binaryitem_pk PRIMARY KEY (binary_id)
);
COMMENT ON TABLE main.urm_dist_binaryitem IS 'Product distributive binary item';


CREATE TABLE main.urm_dist_compitem (
                compitem_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                comp_id INTEGER NOT NULL,
                compitem_type INTEGER NOT NULL,
                binary_id INTEGER,
                confitem_id INTEGER,
                schema_id INTEGER,
                deploy_name VARCHAR(64),
                wsdl_request VARCHAR,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_dist_compitem_pk PRIMARY KEY (compitem_id)
);
COMMENT ON TABLE main.urm_dist_compitem IS 'Product deployment component item';


ALTER TABLE main.urm_dist_compitem ADD CONSTRAINT urm_comp_compitem_fk
FOREIGN KEY (comp_id)
REFERENCES main.urm_dist_comp (comp_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_dist_binaryitem ADD CONSTRAINT urm_delivery_binaryitem_fk
FOREIGN KEY (delivery_id)
REFERENCES main.urm_dist_delivery (delivery_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_dist_schemaitem ADD CONSTRAINT urm_delivery_schema_fk
FOREIGN KEY (delivery_id)
REFERENCES main.urm_dist_delivery (delivery_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_dist_confitem ADD CONSTRAINT urm_delivery_confitem_fk
FOREIGN KEY (delivery_id)
REFERENCES main.urm_dist_delivery (delivery_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_dist_docitem ADD CONSTRAINT urm_delivery_doc_fk
FOREIGN KEY (delivery_id)
REFERENCES main.urm_dist_delivery (delivery_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_dist_compitem ADD CONSTRAINT urm_confitem_compitem_fk
FOREIGN KEY (confitem_id)
REFERENCES main.urm_dist_confitem (confitem_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_dist_binaryitem ADD CONSTRAINT urm_binaryitem_src_fk
FOREIGN KEY (src_binary_id)
REFERENCES main.urm_dist_binaryitem (binary_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_dist_compitem ADD CONSTRAINT urm_binaryitem_compitem_fk
FOREIGN KEY (binary_id)
REFERENCES main.urm_dist_binaryitem (binary_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
