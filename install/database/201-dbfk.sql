﻿-- server.infra
ALTER TABLE main.urm_account ADD CONSTRAINT urm_resource_account_fk
FOREIGN KEY (resource_id)
REFERENCES main.urm_resource (resource_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;