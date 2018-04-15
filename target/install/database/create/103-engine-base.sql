
CREATE TABLE main.urm_base_group (
                group_id INTEGER NOT NULL,
                basecategory_type INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                offline BOOLEAN NOT NULL,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_base_group_pk PRIMARY KEY (group_id)
);
COMMENT ON TABLE main.urm_base_group IS 'Base software item group';


CREATE TABLE main.urm_base_item (
                baseitem_id INTEGER NOT NULL,
                group_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                offline BOOLEAN NOT NULL,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_base_item_pk PRIMARY KEY (baseitem_id)
);
COMMENT ON TABLE main.urm_base_item IS 'Base software installable item';


CREATE TABLE main.urm_base_data (
                baseitem_id INTEGER NOT NULL,
                srcdir VARCHAR,
                serveraccess_type INTEGER NOT NULL,
                os_type INTEGER NOT NULL,
                installlink VARCHAR,
                baseversion VARCHAR(64),
                srcfiledir VARCHAR,
                srcfile VARCHAR,
                installscript VARCHAR(64),
                charset VARCHAR(30),
                basename VARCHAR(64),
                basesrcformat_type INTEGER NOT NULL,
                admin BOOLEAN NOT NULL,
                basesrc_type INTEGER NOT NULL,
                installpath VARCHAR,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_base_data_pk PRIMARY KEY (baseitem_id)
);
COMMENT ON TABLE main.urm_base_data IS 'Base item implementation';


CREATE TABLE main.urm_base_item_deps (
                baseitem_id INTEGER NOT NULL,
                dep_baseitem_id INTEGER NOT NULL,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_base_item_deps_pk PRIMARY KEY (baseitem_id, dep_baseitem_id)
);
COMMENT ON TABLE main.urm_base_item_deps IS 'Base software item installation dependencies';


ALTER TABLE main.urm_base_item ADD CONSTRAINT urm_base_group_item_fk
FOREIGN KEY (group_id)
REFERENCES main.urm_base_group (group_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_base_item_deps ADD CONSTRAINT urm_base_item_deps_item_fk
FOREIGN KEY (baseitem_id)
REFERENCES main.urm_base_item (baseitem_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_base_item_deps ADD CONSTRAINT urm_base_item_deps_depitem_fk
FOREIGN KEY (dep_baseitem_id)
REFERENCES main.urm_base_item (baseitem_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_base_data ADD CONSTRAINT urm_base_item_urm_base_data_fk
FOREIGN KEY (baseitem_id)
REFERENCES main.urm_base_item (baseitem_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
