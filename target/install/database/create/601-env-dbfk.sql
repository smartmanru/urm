﻿-- env.coreALTER TABLE main.urm_env ADD CONSTRAINT urm_env_meta_fkFOREIGN KEY (meta_fkid)REFERENCES main.urm_product_meta (meta_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_env ADD CONSTRAINT urm_resource_envkey_fkFOREIGN KEY (envkey_resource_fkid)REFERENCES main.urm_resource (resource_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_env ADD CONSTRAINT urm_account_distr_fkFOREIGN KEY (distr_account_fkid)REFERENCES main.urm_account (account_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_env_segment ADD CONSTRAINT urm_datacenter_segment_fkFOREIGN KEY (datacenter_fkid)REFERENCES main.urm_account (datacenter_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_env_server ADD CONSTRAINT urm_schema_server_fkFOREIGN KEY (db_admschema_fkid)REFERENCES main.urm_product_schema (schema_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_env_server ADD CONSTRAINT urm_baseitem_server_fkFOREIGN KEY (baseitem_fkid)REFERENCES main.urm_base_item (baseitem_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_env_node ADD CONSTRAINT urm_baseitem_node_fkFOREIGN KEY (account_fkid)REFERENCES main.urm_account (account_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_env_deployment ADD CONSTRAINT urm_comp_deployment_fkFOREIGN KEY (comp_fkid)REFERENCES main.urm_dist_comp (comp_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_env_deployment ADD CONSTRAINT urm_binaryitem_deployment_fkFOREIGN KEY (binaryitem_fkid)REFERENCES main.urm_dist_binaryitem (binary_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_env_deployment ADD CONSTRAINT urm_confitem_deployment_fkFOREIGN KEY (binaryitem_fkid)REFERENCES main.urm_dist_confitem (confitem_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_env_deployment ADD CONSTRAINT urm_schema_deployment_fkFOREIGN KEY (schema_fkid)REFERENCES main.urm_product_schema (schema_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;