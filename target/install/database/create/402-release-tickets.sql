
CREATE TABLE main.urm_rel_ticketset (
                ticketset_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                code VARCHAR(30) NOT NULL,
                name VARCHAR NOT NULL,
                xdesc VARCHAR,
                ticketsetstatus_type INTEGER NOT NULL,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_ticketset_pk PRIMARY KEY (ticketset_id)
);
COMMENT ON TABLE main.urm_rel_ticketset IS 'Releasse ticket set';


CREATE TABLE main.urm_rel_tickettarget (
                tickettarget_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                ticketset_id INTEGER NOT NULL,
                pos INTEGER NOT NULL,
                buildtarget_id INTEGER,
                disttarget_id INTEGER,
                descoped BOOLEAN NOT NULL,
                accepted BOOLEAN NOT NULL,
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_tickettarget_pk PRIMARY KEY (tickettarget_id)
);
COMMENT ON TABLE main.urm_rel_tickettarget IS 'Release ticket target';


CREATE TABLE main.urm_rel_ticket (
                ticket_id INTEGER NOT NULL,
                release_id INTEGER NOT NULL,
                ticketset_id INTEGER NOT NULL,
                pos INTEGER NOT NULL,
                code VARCHAR(30) NOT NULL,
                name VARCHAR NOT NULL,
                xdesc VARCHAR,
                link VARCHAR,
                ticket_type INTEGER NOT NULL,
                ticketstatus_type INTEGER NOT NULL,
                active BOOLEAN NOT NULL,
                accepted BOOLEAN NOT NULL,
                descoped BOOLEAN NOT NULL,
                owner_user_fkid INTEGER,
                owner_user_fkname VARCHAR(30),
                dev_user_fkid INTEGER,
                dev_user_fkname VARCHAR(30),
                qa_user_fkid INTEGER,
                qa_user_fkname VARCHAR(30),
                rv INTEGER NOT NULL,
                CONSTRAINT urm_rel_ticket_pk PRIMARY KEY (ticket_id)
);
COMMENT ON TABLE main.urm_rel_ticket IS 'Release ticket';


ALTER TABLE main.urm_rel_ticket ADD CONSTRAINT urm_rel_ticketset_ticket_fk
FOREIGN KEY (ticketset_id)
REFERENCES main.urm_rel_ticketset (ticketset_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_rel_tickettarget ADD CONSTRAINT urm_rel_ticketset_tickettarget_fk
FOREIGN KEY (ticketset_id)
REFERENCES main.urm_rel_ticketset (ticketset_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
