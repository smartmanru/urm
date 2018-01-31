
CREATE TABLE main.urm_datacenter (
                datacenter_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_datacenter_pk PRIMARY KEY (datacenter_id)
);
COMMENT ON TABLE main.urm_datacenter IS 'Infrastructure datacenter';


CREATE TABLE main.urm_network (
                network_id INTEGER NOT NULL,
                datacenter_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                mask VARCHAR(30) NOT NULL,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_network_pk PRIMARY KEY (network_id)
);
COMMENT ON TABLE main.urm_network IS 'Infrastructure datacenter subnet';


CREATE TABLE main.urm_host (
                host_id INTEGER NOT NULL,
                network_id INTEGER NOT NULL,
                name VARCHAR NOT NULL,
                ip VARCHAR(30),
                port INTEGER,
                xdesc VARCHAR,
                os_type INTEGER NOT NULL,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_host_pk PRIMARY KEY (host_id)
);
COMMENT ON TABLE main.urm_host IS 'Network host';


CREATE TABLE main.urm_account (
                account_id INTEGER NOT NULL,
                host_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                admin BOOLEAN NOT NULL,
                resource_id INTEGER,
                cv INTEGER NOT NULL,
                CONSTRAINT urm_account_pk PRIMARY KEY (account_id)
);
COMMENT ON TABLE main.urm_account IS 'Host account';


ALTER TABLE main.urm_network ADD CONSTRAINT urm_datacenter_network_fk
FOREIGN KEY (datacenter_id)
REFERENCES main.urm_datacenter (datacenter_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_host ADD CONSTRAINT urm_network_host_fk
FOREIGN KEY (network_id)
REFERENCES main.urm_network (network_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_account ADD CONSTRAINT urm_host_account_fk
FOREIGN KEY (host_id)
REFERENCES main.urm_host (host_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
