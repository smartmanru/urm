
CREATE TABLE main.urm_object_entity (
                param_object_id INTEGER NOT NULL,
                paramentity_type INTEGER NOT NULL,
                custom BOOLEAN NOT NULL,
                use_props BOOLEAN NOT NULL,
                app_table VARCHAR(64),
                id_field VARCHAR(64),
                object_type INTEGER NOT NULL,
                meta_object_id INTEGER NOT NULL,
                meta_objectversion_type INTEGER NOT NULL,
                data_objectversion_type INTEGER NOT NULL,
                version INTEGER NOT NULL,
                CONSTRAINT urm_object_entity_pk PRIMARY KEY (param_object_id, paramentity_type)
);
COMMENT ON TABLE main.urm_object_entity IS 'Owner of parameter definition';


CREATE TABLE main.urm_object_version (
                owner_object_id INTEGER NOT NULL,
                version INTEGER NOT NULL,
                objectversion_type INTEGER NOT NULL,
                last_import_id INTEGER,
                last_name VARCHAR(30) NOT NULL,
                ownerstatus_type INTEGER NOT NULL,
                CONSTRAINT urm_object_version_pk PRIMARY KEY (owner_object_id)
);
COMMENT ON TABLE main.urm_object_version IS 'Core, system, product or environment version';


CREATE TABLE main.urm_object_import (
                import_id INTEGER NOT NULL,
                owner_object_id INTEGER NOT NULL,
                owner_object_version INTEGER NOT NULL,
                CONSTRAINT urm_object_import_pk PRIMARY KEY (import_id)
);
COMMENT ON TABLE main.urm_object_import IS 'Import operation performed';


CREATE TABLE main.urm_object_import_mismatch (
                import_id INTEGER NOT NULL,
                mismatched_object_id INTEGER NOT NULL,
                mismatched_param VARCHAR(64) NOT NULL,
                mismatched_object_type INTEGER NOT NULL,
                param_custom BOOLEAN NOT NULL,
                mismatched_value VARCHAR,
                replace_do BOOLEAN NOT NULL,
                replace_value VARCHAR,
                CONSTRAINT urm_object_import_mismatch_pk PRIMARY KEY (import_id, mismatched_object_id, mismatched_param)
);
COMMENT ON TABLE main.urm_object_import_mismatch IS 'Import matching problems, available for correction';


CREATE TABLE main.urm_object_param (
                param_object_id INTEGER NOT NULL,
                paramentity_type INTEGER NOT NULL,
                param_id INTEGER NOT NULL,
                entitycolumn INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                dbname VARCHAR(64),
                xmlname VARCHAR(64),
                xdesc VARCHAR,
                paramvalue_type INTEGER NOT NULL,
                paramvalue_subtype INTEGER NOT NULL,
                object_type INTEGER NOT NULL,
                enumname VARCHAR(64),
                required BOOLEAN NOT NULL,
                expr_def VARCHAR,
                version INTEGER NOT NULL,
                CONSTRAINT urm_object_param_pk PRIMARY KEY (param_object_id, paramentity_type, param_id)
);
COMMENT ON TABLE main.urm_object_param IS 'Instance configuration parameters';


CREATE TABLE main.urm_object_param_value (
                object_id INTEGER NOT NULL,
                paramrole_type INTEGER NOT NULL,
                param_object_id INTEGER NOT NULL,
                paramentity_type INTEGER NOT NULL,
                param_id INTEGER NOT NULL,
                expr_value VARCHAR NOT NULL,
                version INTEGER NOT NULL,
                CONSTRAINT urm_object_param_value_pk PRIMARY KEY (object_id, paramrole_type, param_object_id, paramentity_type, param_id)
);
COMMENT ON TABLE main.urm_object_param_value IS 'Object parameter value';


CREATE TABLE main.urm_object_type (
                category INTEGER NOT NULL,
                item INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                av INTEGER NOT NULL,
                CONSTRAINT urm_object_type_pk PRIMARY KEY (category, item)
);
COMMENT ON TABLE main.urm_object_type IS 'Database enum items';


CREATE TABLE main.urm_resource (
                resource_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                resource_type INTEGER NOT NULL,
                baseurl VARCHAR,
                verified BOOLEAN NOT NULL,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_resource_pk PRIMARY KEY (resource_id)
);
COMMENT ON TABLE main.urm_resource IS 'Instance authorizable resources';


CREATE TABLE main.urm_mirror (
                mirror_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                mirror_type INTEGER NOT NULL,
                resource_id INTEGER,
                resource_repo VARCHAR(64),
                resource_root VARCHAR,
                resource_data VARCHAR,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_mirror_pk PRIMARY KEY (mirror_id)
);
COMMENT ON TABLE main.urm_mirror IS 'Code repository';


CREATE TABLE main.urm_object_name (
                parent INTEGER NOT NULL,
                object_type INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                object_id INTEGER NOT NULL,
                CONSTRAINT urm_object_name_pk PRIMARY KEY (parent, object_type, name)
);
COMMENT ON TABLE main.urm_object_name IS 'Named objects mapping to support import';


ALTER TABLE main.urm_object_param ADD CONSTRAINT urm_object_entity_param_fk
FOREIGN KEY (paramentity_type, param_object_id)
REFERENCES main.urm_object_entity (paramentity_type, param_object_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_object_import ADD CONSTRAINT urm_object_version_owner_fk
FOREIGN KEY (owner_object_id)
REFERENCES main.urm_object_version (owner_object_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_object_import_mismatch ADD CONSTRAINT urm_object_import_mismatch_fk
FOREIGN KEY (import_id)
REFERENCES main.urm_object_import (import_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_object_version ADD CONSTRAINT urm_object_import_fk
FOREIGN KEY (last_import_id)
REFERENCES main.urm_object_import (import_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_object_param_value ADD CONSTRAINT urm_object_param_value_object_fk
FOREIGN KEY (param_id, param_object_id, paramentity_type)
REFERENCES main.urm_object_param (param_id, param_object_id, paramentity_type)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_mirror ADD CONSTRAINT urm_resource_mirror_fk
FOREIGN KEY (resource_id)
REFERENCES main.urm_resource (resource_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
