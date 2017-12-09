
CREATE TABLE main.urm_auth_user (
                user_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                fullname VARCHAR,
                email VARCHAR(30),
                admin BOOLEAN NOT NULL,
                local BOOLEAN NOT NULL,
                uv INTEGER NOT NULL,
                CONSTRAINT urm_auth_user_pk PRIMARY KEY (user_id)
);
COMMENT ON TABLE main.urm_auth_user IS 'Authorized user';


CREATE TABLE main.urm_auth_group (
                group_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                any_resources BOOLEAN NOT NULL,
                any_products BOOLEAN NOT NULL,
                any_networks BOOLEAN NOT NULL,
                roledev BOOLEAN NOT NULL,
                rolerel BOOLEAN NOT NULL,
                roletest BOOLEAN NOT NULL,
                roleopr BOOLEAN NOT NULL,
                roleinfra BOOLEAN NOT NULL,
                specialrights_baseadm BOOLEAN NOT NULL,
                specialrights_baseitems BOOLEAN NOT NULL,
                uv INTEGER NOT NULL,
                CONSTRAINT urm_auth_group_pk PRIMARY KEY (group_id)
);
COMMENT ON TABLE main.urm_auth_group IS 'Authorization user group';


CREATE TABLE main.urm_auth_groupuser (
                group_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                uv VARCHAR NOT NULL,
                CONSTRAINT urm_auth_groupuser_pk PRIMARY KEY (group_id, user_id)
);


CREATE TABLE main.urm_auth_access_network (
                group_id INTEGER NOT NULL,
                network_id INTEGER NOT NULL,
                uv INTEGER NOT NULL,
                CONSTRAINT urm_auth_access_network_pk PRIMARY KEY (group_id, network_id)
);
COMMENT ON TABLE main.urm_auth_access_network IS 'Group access rights to networks';


CREATE TABLE main.urm_auth_access_product (
                group_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                uv INTEGER NOT NULL,
                CONSTRAINT urm_auth_access_product_pk PRIMARY KEY (group_id, product_id)
);
COMMENT ON TABLE main.urm_auth_access_product IS 'Group access rights to products';


CREATE TABLE main.urm_auth_access_resource (
                group_id INTEGER NOT NULL,
                resource_id INTEGER NOT NULL,
                uv INTEGER NOT NULL,
                CONSTRAINT urm_auth_access_resource_pk PRIMARY KEY (group_id, resource_id)
);
COMMENT ON TABLE main.urm_auth_access_resource IS 'Group access rights to resources';


ALTER TABLE main.urm_auth_groupuser ADD CONSTRAINT urm_auth_user_urm_auth_groupuser_fk
FOREIGN KEY (user_id)
REFERENCES main.urm_auth_user (user_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_auth_access_resource ADD CONSTRAINT urm_auth_group_urm_auth_access_resource_fk
FOREIGN KEY (group_id)
REFERENCES main.urm_auth_group (group_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_auth_access_product ADD CONSTRAINT urm_auth_group_urm_auth_access_product_fk
FOREIGN KEY (group_id)
REFERENCES main.urm_auth_group (group_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_auth_access_network ADD CONSTRAINT urm_auth_group_urm_auth_access_network_fk
FOREIGN KEY (group_id)
REFERENCES main.urm_auth_group (group_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_auth_groupuser ADD CONSTRAINT urm_auth_group_urm_auth_groupuser_fk
FOREIGN KEY (group_id)
REFERENCES main.urm_auth_group (group_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
