-- server.infra
ALTER TABLE main.urm_account ADD CONSTRAINT urm_resource_account_fk
FOREIGN KEY (resource_id)
REFERENCES main.urm_resource (resource_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;-- server.authALTER TABLE main.urm_auth_access_resource ADD CONSTRAINT urm_resource_auth_access_fkFOREIGN KEY (resource_id)REFERENCES main.urm_resource (resource_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_auth_access_product ADD CONSTRAINT urm_product_auth_access_fkFOREIGN KEY (product_id)REFERENCES main.urm_product (product_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_auth_access_network ADD CONSTRAINT urm_network_auth_access_fkFOREIGN KEY (network_id)REFERENCES main.urm_network (network_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;-- server.releasesALTER TABLE main.urm_project_builder ADD CONSTRAINT urm_target_resource_project_builder_fkFOREIGN KEY (target_resource_id)REFERENCES main.urm_resource (resource_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_project_builder ADD CONSTRAINT urm_remote_account_project_builder_fkFOREIGN KEY (remote_account_id)REFERENCES main.urm_account (account_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_project_builder ADD CONSTRAINT urm_builder_remote_account_fkFOREIGN KEY (remote_account_id)REFERENCES main.urm_account (account_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;