
CREATE TABLE main.urm_rel_repository (
                repo_id INTEGER NOT NULL,
                product_fkid INTEGER,
                product_fkname VARCHAR(30),
                master_release_id INTEGER NOT NULL,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_repository_pk PRIMARY KEY (repo_id)
);
COMMENT ON TABLE main.urm_rel_repository IS 'Release repository';


CREATE TABLE main.urm_rel_main (
                release_id INTEGER NOT NULL,
                repo_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                lifecycle_type INTEGER NOT NULL,
                v1 INTEGER NOT NULL,
                v2 INTEGER NOT NULL,
                v3 INTEGER NOT NULL,
                v4 INTEGER NOT NULL,
                label_version VARCHAR(30) NOT NULL,
                archived BOOLEAN NOT NULL,
                uat_env_fkid INTEGER,
                uat_env_fkname VARCHAR(30),
                uat_ev INTEGER,
                prod_env_fkid INTEGER,
                prod_env_fkname VARCHAR(30),
                prod_ev INTEGER,
                issue_pv INTEGER,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_main_pk PRIMARY KEY (release_id)
);
COMMENT ON TABLE main.urm_rel_main IS 'Product release';


CREATE TABLE main.urm_rel_target (
                releasetarget_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                scopecategory_type INTEGER NOT NULL,
                releasetarget_type INTEGER NOT NULL,
                scope_all BOOLEAN NOT NULL,
                srcset_fkid INTEGER,
                srcset_fkname VARCHAR(30),
                project_fkid INTEGER,
                project_fkname VARCHAR(30),
                delivery_fkid INTEGER,
                delivery_fkname VARCHAR(30),
                binary_fkid INTEGER,
                binary_fkname VARCHAR(64),
                confitem_fkid INTEGER,
                confitem_fkname VARCHAR(64),
                srcitem_fkid INTEGER,
                srcitem_fkname VARCHAR(30),
                schema_fkid INTEGER,
                schema_fkname VARCHAR(30),
                doc_fkid INTEGER,
                doc_fkname VARCHAR(30),
                build_branch VARCHAR(30),
                build_tag VARCHAR(30),
                build_version VARCHAR(30),
                targetfile VARCHAR,
                targetfile_hash VARCHAR(30),
                targetfile_size BIGINT,
                targetfile_time DATE,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_target_pk PRIMARY KEY (releasetarget_id)
);
COMMENT ON TABLE main.urm_rel_target IS 'Release target';


CREATE TABLE main.urm_rel_scopeset (
                scopeset_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                releasetarget_id INTEGER NOT NULL,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_scopeset_pk PRIMARY KEY (scopeset_id)
);
COMMENT ON TABLE main.urm_rel_scopeset IS 'Release scope set';


CREATE TABLE main.urm_rel_scopetarget (
                scopetarget_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                scopeset_id INTEGER NOT NULL,
                releasetarget_id INTEGER NOT NULL,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_scopetarget_pk PRIMARY KEY (scopetarget_id)
);
COMMENT ON TABLE main.urm_rel_scopetarget IS 'Release scope set target';


CREATE TABLE main.urm_rel_scopeitem (
                scopeitem_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                scopetarget_id INTEGER NOT NULL,
                releasetarget_id INTEGER NOT NULL,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_scopeitem_pk PRIMARY KEY (scopeitem_id)
);
COMMENT ON TABLE main.urm_rel_scopeitem IS 'Release scope item';


CREATE TABLE main.urm_rel_dist (
                release_id INTEGER NOT NULL,
                master BOOLEAN NOT NULL,
                data_hash VARCHAR(30) NOT NULL,
                dist_date DATE,
                dist_variant VARCHAR(64),
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_dist_pk PRIMARY KEY (release_id)
);
COMMENT ON TABLE main.urm_rel_dist IS 'Release distibutive';


ALTER TABLE main.urm_rel_main ADD CONSTRAINT urm_rel_repository_release_fk
FOREIGN KEY (repo_id)
REFERENCES main.urm_rel_repository (repo_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_dist ADD CONSTRAINT urm_rel_main_dist_fk
FOREIGN KEY (release_id)
REFERENCES main.urm_rel_main (release_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_repository ADD CONSTRAINT urm_rel_repository_master_release_fk
FOREIGN KEY (master_release_id)
REFERENCES main.urm_rel_main (release_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_scopeset ADD CONSTRAINT urm_rel_main_scopeset_fk
FOREIGN KEY (release_id)
REFERENCES main.urm_rel_main (release_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_scopetarget ADD CONSTRAINT urm_rel_main_scopetarget_fk
FOREIGN KEY (release_id)
REFERENCES main.urm_rel_main (release_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_scopeitem ADD CONSTRAINT urm_rel_main_copeitem_fk
FOREIGN KEY (release_id)
REFERENCES main.urm_rel_main (release_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_target ADD CONSTRAINT urm_rel_main_target_fk
FOREIGN KEY (release_id)
REFERENCES main.urm_rel_main (release_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_scopeset ADD CONSTRAINT urm_rel_target_scopeset_fk
FOREIGN KEY (releasetarget_id)
REFERENCES main.urm_rel_target (releasetarget_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_scopetarget ADD CONSTRAINT urm_rel_target_scopetarget_fk
FOREIGN KEY (releasetarget_id)
REFERENCES main.urm_rel_target (releasetarget_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_scopeitem ADD CONSTRAINT urm_rel_target_scopeitem_fk
FOREIGN KEY (releasetarget_id)
REFERENCES main.urm_rel_target (releasetarget_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_scopetarget ADD CONSTRAINT urm_rel_scopeset_scopetarget_fk
FOREIGN KEY (scopeset_id)
REFERENCES main.urm_rel_scopeset (scopeset_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_scopeitem ADD CONSTRAINT urm_rel_scopetarget_scopeitem_fk
FOREIGN KEY (scopetarget_id)
REFERENCES main.urm_rel_scopetarget (scopetarget_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
