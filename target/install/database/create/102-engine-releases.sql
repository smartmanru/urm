
CREATE TABLE main.urm_release_lifecycle (
                lifecycle_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                lifecycle_type INTEGER NOT NULL,
                enabled BOOLEAN NOT NULL,
                regular BOOLEAN NOT NULL,
                days_to_release INTEGER NOT NULL,
                days_to_deploy INTEGER NOT NULL,
                shift_days INTEGER NOT NULL,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_release_lifecycle_pk PRIMARY KEY (lifecycle_id)
);
COMMENT ON TABLE main.urm_release_lifecycle IS 'Registered release lifecycle types';


CREATE TABLE main.urm_lifecycle_phase (
                phase_id INTEGER NOT NULL,
                lifecycle_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                lifecyclestage_type INTEGER NOT NULL,
                stage_pos INTEGER NOT NULL,
                unlimited BOOLEAN NOT NULL,
                start_day BOOLEAN NOT NULL,
                days INTEGER NOT NULL,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_lifecycle_phase_pk PRIMARY KEY (phase_id)
);
COMMENT ON TABLE main.urm_lifecycle_phase IS 'Release lifecycle type phases';


CREATE TABLE main.urm_project_builder (
                builder_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                version VARCHAR(30),
                buildermethod_type INTEGER NOT NULL,
                builder_command VARCHAR,
                builder_homepath VARCHAR,
                builder_options VARCHAR,
                java_jdkhomepath VARCHAR,
                buildertarget_type INTEGER NOT NULL,
                target_resource_id INTEGER,
                target_path VARCHAR,
                target_platform VARCHAR(64),
                builder_remote BOOLEAN NOT NULL,
                remote_account_id INTEGER,
                remote_mirrorpath VARCHAR,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_project_builder_pk PRIMARY KEY (builder_id)
);
COMMENT ON TABLE main.urm_project_builder IS 'Registered source project build procedure';


ALTER TABLE main.urm_lifecycle_phase ADD CONSTRAINT urm_release_lifecycle_urm_lifecycle_phase_fk
FOREIGN KEY (lifecycle_id)
REFERENCES main.urm_release_lifecycle (lifecycle_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
