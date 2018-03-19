
CREATE TABLE main.urm_rel_schedule (
                release_id INTEGER NOT NULL,
                start_date_actual DATE NOT NULL,
                release_date_scheduled DATE NOT NULL,
                release_date_actual DATE,
                complete_date_actual DATE,
                released BOOLEAN NOT NULL,
                completed BOOLEAN NOT NULL,
                phase_current INTEGER NOT NULL,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_schedule_pk PRIMARY KEY (release_id)
);
COMMENT ON TABLE main.urm_rel_schedule IS 'Release schedule info';


CREATE TABLE main.urm_rel_phase (
                phase_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                lifecyclestage_type INTEGER NOT NULL,
                stage_pos INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                days INTEGER NOT NULL,
                normal_days INTEGER NOT NULL,
                finished BOOLEAN NOT NULL,
                unlimited BOOLEAN NOT NULL,
                start_date DATE,
                finish_date DATE,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_phase_pk PRIMARY KEY (phase_id)
);
COMMENT ON TABLE main.urm_rel_phase IS 'Release schedule phase';


ALTER TABLE main.urm_rel_phase ADD CONSTRAINT urm_rel_schedule_phase_fk
FOREIGN KEY (release_id)
REFERENCES main.urm_rel_schedule (release_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
