
CREATE TABLE main.urm_env (
                env_id INTEGER NOT NULL,
                meta_fkid INTEGER,
                meta_fkname VARCHAR(30),
                matched BOOLEAN NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                env_type INTEGER NOT NULL,
                baseline_env_fkid INTEGER,
                baseline_env_fkname VARCHAR(30),
                offline BOOLEAN NOT NULL,
                envkey_resource_fkid INTEGER,
                envkey_resource_fkname VARCHAR(64),
                distr_remote BOOLEAN NOT NULL,
                distr_account_fkid INTEGER,
                distr_account_fkname VARCHAR(64),
                distr_path VARCHAR,
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_pk PRIMARY KEY (env_id)
);
COMMENT ON TABLE main.urm_env IS 'Product environment';


CREATE TABLE main.urm_env_segment (
                segment_id INTEGER NOT NULL,
                env_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR NOT NULL,
                baseline_segment_fkid INTEGER,
                baseline_segment_fkname VARCHAR(30),
                offline BOOLEAN NOT NULL,
                datacenter_fkid INTEGER,
                datacenter_fkname VARCHAR(30),
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_segment_pk PRIMARY KEY (segment_id)
);
COMMENT ON TABLE main.urm_env_segment IS 'Environment segment';


CREATE TABLE main.urm_env_server (
                server_id INTEGER NOT NULL,
                env_id INTEGER NOT NULL,
                segment_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                serverrun_type INTEGER NOT NULL,
                serveraccess_type INTEGER NOT NULL,
                os_type INTEGER NOT NULL,
                baseline_server_fkid INTEGER,
                baseline_server_fkname VARCHAR(30),
                offline BOOLEAN NOT NULL,
                dbms_type INTEGER NOT NULL,
                db_admschema_fkid INTEGER,
                db_admschema_fkname VARCHAR(30),
                baseitem_fkid INTEGER,
                baseitem_fkname VARCHAR(64),
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_server_pk PRIMARY KEY (server_id)
);
COMMENT ON TABLE main.urm_env_server IS 'Environment server';


CREATE TABLE main.urm_env_node (
                node_id INTEGER NOT NULL,
                env_id INTEGER NOT NULL,
                server_id INTEGER NOT NULL,
                pos INTEGER NOT NULL,
                node_type INTEGER NOT NULL,
                account_fkid INTEGER,
                account_fkname VARCHAR(64),
                deploygroup VARCHAR(64),
                offline BOOLEAN NOT NULL,
                dbinstance VARCHAR(64),
                dbstandby BOOLEAN NOT NULL,
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_node_pk PRIMARY KEY (node_id)
);
COMMENT ON TABLE main.urm_env_node IS 'Environment server node';


CREATE TABLE main.urm_env_deployment (
                deployment_id INTEGER NOT NULL,
                env_id INTEGER NOT NULL,
                server_id INTEGER NOT NULL,
                serverdeployment_type INTEGER NOT NULL,
                comp_fkid INTEGER,
                comp_fkname VARCHAR(64),
                binaryitem_fkid INTEGER,
                binaryitem_fkname VARCHAR(64),
                confitem_fkid INTEGER,
                confitem_fkname VARCHAR(64),
                schema_fkid INTEGER,
                schema_fkname VARCHAR(64),
                deploymode_type INTEGER NOT NULL,
                deploypath VARCHAR,
                dbname VARCHAR(30),
                dbuser VARCHAR(30),
                node_type INTEGER NOT NULL,
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_deployment_pk PRIMARY KEY (deployment_id)
);
COMMENT ON TABLE main.urm_env_deployment IS 'Environment server deployment';


CREATE TABLE main.urm_env_server_deps (
                server_id INTEGER NOT NULL,
                dep_server_id INTEGER NOT NULL,
                env_id INTEGER NOT NULL,
                serverdependency_type INTEGER NOT NULL,
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_server_deps_pk PRIMARY KEY (server_id, dep_server_id)
);
COMMENT ON TABLE main.urm_env_server_deps IS 'Environment server dependencies';


CREATE TABLE main.urm_env_startgroup (
                startgroup_id INTEGER NOT NULL,
                env_id INTEGER NOT NULL,
                segment_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_startgroup_pk PRIMARY KEY (startgroup_id)
);
COMMENT ON TABLE main.urm_env_startgroup IS 'Environment segment start group';


CREATE TABLE main.urm_env_startgroup_server (
                startgroup_id INTEGER NOT NULL,
                server_id INTEGER NOT NULL,
                env_id INTEGER NOT NULL,
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_startgroup_server_pk PRIMARY KEY (startgroup_id, server_id)
);
COMMENT ON TABLE main.urm_env_startgroup_server IS 'Environment start group server';


ALTER TABLE main.urm_env ADD CONSTRAINT urm_env_baseline_fk
FOREIGN KEY (baseline_env_fkid)
REFERENCES main.urm_env (env_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_env_segment ADD CONSTRAINT urm_env_segment_fk
FOREIGN KEY (env_id)
REFERENCES main.urm_env (env_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_env_segment ADD CONSTRAINT urm_segment_baseline_fk
FOREIGN KEY (baseline_segment_fkid)
REFERENCES main.urm_env_segment (segment_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_env_startgroup ADD CONSTRAINT urm_segment_startgroup_fk
FOREIGN KEY (segment_id)
REFERENCES main.urm_env_segment (segment_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_env_server ADD CONSTRAINT urm_segment_server_fk
FOREIGN KEY (segment_id)
REFERENCES main.urm_env_segment (segment_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_env_server ADD CONSTRAINT urm_server_baseline_fk
FOREIGN KEY (baseline_server_fkid)
REFERENCES main.urm_env_server (server_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_env_server_deps ADD CONSTRAINT urm_server_deps_fk
FOREIGN KEY (server_id)
REFERENCES main.urm_env_server (server_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_env_server_deps ADD CONSTRAINT urm_server_deps_reference_fk1
FOREIGN KEY (dep_server_id)
REFERENCES main.urm_env_server (server_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_env_startgroup_server ADD CONSTRAINT urm_server_startgroup_fk
FOREIGN KEY (server_id)
REFERENCES main.urm_env_server (server_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_env_deployment ADD CONSTRAINT urm_server_deployment_fk
FOREIGN KEY (server_id)
REFERENCES main.urm_env_server (server_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_env_node ADD CONSTRAINT urm_server_node_fk
FOREIGN KEY (server_id)
REFERENCES main.urm_env_server (server_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_env_startgroup_server ADD CONSTRAINT urm_startgroup_server_fk
FOREIGN KEY (startgroup_id)
REFERENCES main.urm_env_startgroup (startgroup_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
