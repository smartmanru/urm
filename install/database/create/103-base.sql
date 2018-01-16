
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
                item_id INTEGER NOT NULL,
                group_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                basesrc_type INTEGER NOT NULL,
                basesrcformat_type INTEGER NOT NULL,
                os_type INTEGER NOT NULL,
                serveraccess_type INTEGER NOT NULL,
                basename VARCHAR(64),
                baseversion VARCHAR(64),
                srcdir VARCHAR,
                srcfile VARCHAR,
                srcfiledir VARCHAR,
                installscript VARCHAR(64),
                installpath VARCHAR,
                installlink VARCHAR,
                charset VARCHAR(30),
                offline BOOLEAN NOT NULL,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_base_item_pk PRIMARY KEY (item_id)
);
COMMENT ON TABLE main.urm_base_item IS 'Base software installable item';


CREATE TABLE main.urm_base_item_deps (
                item_id INTEGER NOT NULL,
                dep_item_id INTEGER NOT NULL,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_base_item_deps_pk PRIMARY KEY (item_id, dep_item_id)
);
COMMENT ON TABLE main.urm_base_item_deps IS 'Base software item installation dependencies';


ALTER TABLE main.urm_base_item ADD CONSTRAINT urm_base_group_item_fk
FOREIGN KEY (group_id)
REFERENCES main.urm_base_group (group_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_base_item_deps ADD CONSTRAINT urm_base_item_deps_item_fk
FOREIGN KEY (item_id)
REFERENCES main.urm_base_item (item_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_base_item_deps ADD CONSTRAINT urm_base_item_deps_depitem_fk
FOREIGN KEY (dep_item_id)
REFERENCES main.urm_base_item (item_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
