
CREATE TABLE main.urm_env_montarget (
                montarget_id INTEGER NOT NULL,
                env_id INTEGER NOT NULL,
                segment_id INTEGER NOT NULL,
                major_enabled BOOLEAN NOT NULL,
                major_schedule VARCHAR,
                major_maxtime INTEGER NOT NULL,
                minor_enabled BOOLEAN NOT NULL,
                minor_schedule VARCHAR,
                minor_maxtime INTEGER NOT NULL,
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_montarget_pk PRIMARY KEY (montarget_id)
);
COMMENT ON TABLE main.urm_env_montarget IS 'Environment monitoring target';


CREATE TABLE main.urm_env_monitem (
                monitem_id INTEGER NOT NULL,
                env_id INTEGER NOT NULL,
                montarget_id INTEGER NOT NULL,
                monitem_type INTEGER NOT NULL,
                url VARCHAR,
                wsdata VARCHAR,
                wscheck VARCHAR,
                ev INTEGER NOT NULL,
                CONSTRAINT urm_env_monitem_pk PRIMARY KEY (monitem_id)
);
COMMENT ON TABLE main.urm_env_monitem IS 'Environment monitoring item';


ALTER TABLE main.urm_env_monitem ADD CONSTRAINT urm_product_montarget_urm_product_monitem_fk
FOREIGN KEY (montarget_id)
REFERENCES main.urm_env_montarget (montarget_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
