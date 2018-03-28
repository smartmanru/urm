
CREATE TABLE main.urm_rel_repository (
                repo_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                meta_fkid INTEGER,
                meta_fkname VARCHAR(30),
                av INTEGER NOT NULL,
                CONSTRAINT urm_rel_repository_pk PRIMARY KEY (repo_id)
);
COMMENT ON TABLE main.urm_rel_repository IS 'Release repository';


CREATE TABLE main.urm_rel_main (
                release_id INTEGER NOT NULL,
                repo_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                master BOOLEAN NOT NULL,
                lifecycle_type INTEGER NOT NULL,
                v1 INTEGER NOT NULL,
                v2 INTEGER NOT NULL,
                v3 INTEGER NOT NULL,
                v4 INTEGER NOT NULL,
                releasever VARCHAR(30) NOT NULL,
                buildmode_type INTEGER NOT NULL,
                compatibility VARCHAR,
                cumulative BOOLEAN NOT NULL,
                archived BOOLEAN NOT NULL,
                cancelled BOOLEAN NOT NULL,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_main_pk PRIMARY KEY (release_id)
);
COMMENT ON TABLE main.urm_rel_main IS 'Product release';


CREATE TABLE main.urm_rel_buildtarget (
                buildtarget_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                scopetarget BOOLEAN NOT NULL,
                buildtarget_type INTEGER NOT NULL,
                scope_all BOOLEAN NOT NULL,
                srcset_fkid INTEGER,
                srcset_fkname VARCHAR(30),
                project_fkid INTEGER,
                project_fkname VARCHAR(30),
                build_tag VARCHAR(30),
                build_branch VARCHAR(30),
                build_version VARCHAR(30),
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_buildtarget_pk PRIMARY KEY (buildtarget_id)
);
COMMENT ON TABLE main.urm_rel_buildtarget IS 'Release build target';


CREATE TABLE main.urm_rel_disttarget (
                disttarget_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                scopetarget BOOLEAN NOT NULL,
                disttarget_type INTEGER NOT NULL,
                scope_all BOOLEAN NOT NULL,
                delivery_fkid INTEGER,
                delivery_fkname VARCHAR(30),
                binary_fkid INTEGER,
                binary_fkname VARCHAR(64),
                confitem_fkid INTEGER,
                confitem_fkname VARCHAR(64),
                schema_fkid INTEGER,
                schema_fkname VARCHAR(30),
                doc_fkid INTEGER,
                doc_fkname VARCHAR(30),
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_disttarget_pk PRIMARY KEY (disttarget_id)
);
COMMENT ON TABLE main.urm_rel_disttarget IS 'Release distributive target';


CREATE TABLE main.urm_rel_dist (
                dist_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                dist_variant VARCHAR(64),
                dist_date DATE NOT NULL,
                data_hash VARCHAR(64) NOT NULL,
                meta_hash VARCHAR(64) NOT NULL,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_dist_pk PRIMARY KEY (dist_id)
);
COMMENT ON TABLE main.urm_rel_dist IS 'Release distibutive';


CREATE TABLE main.urm_rel_distitem (
                distitem_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                disttarget_id INTEGER NOT NULL,
                dist_id INTEGER NOT NULL,
                targetfile VARCHAR,
                targetfile_folder VARCHAR,
                targetfile_size BIGINT,
                targetfile_hash VARCHAR(30),
                targetfile_time DATE,
                source_releasedir VARCHAR(64),
                source_releasetime DATE,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_distitem_pk PRIMARY KEY (distitem_id)
);
COMMENT ON TABLE main.urm_rel_distitem IS 'Distributive item';


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

ALTER TABLE main.urm_rel_disttarget ADD CONSTRAINT urm_rel_main_target_fk
FOREIGN KEY (release_id)
REFERENCES main.urm_rel_main (release_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_distitem ADD CONSTRAINT urm_rel_main_urm_rel_distfile_fk
FOREIGN KEY (release_id)
REFERENCES main.urm_rel_main (release_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_buildtarget ADD CONSTRAINT urm_rel_main_urm_rel_buildtarget_fk
FOREIGN KEY (release_id)
REFERENCES main.urm_rel_main (release_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_distitem ADD CONSTRAINT urm_rel_target_urm_rel_distfile_fk
FOREIGN KEY (disttarget_id)
REFERENCES main.urm_rel_disttarget (disttarget_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_distitem ADD CONSTRAINT urm_rel_dist_urm_rel_distfile_fk
FOREIGN KEY (dist_id)
REFERENCES main.urm_rel_dist (dist_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
