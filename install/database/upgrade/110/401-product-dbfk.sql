-- product.coreALTER TABLE main.urm_product_meta ADD CONSTRAINT urm_product_meta_product_fkFOREIGN KEY (product_id)REFERENCES main.urm_product (product_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;-- product.distALTER TABLE main.urm_dist_delivery ADD CONSTRAINT urm_product_dist_delivery_fkFOREIGN KEY (meta_id)REFERENCES main.urm_product_meta (meta_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_dist_delivery ADD CONSTRAINT urm_unit_dist_delivery_fkFOREIGN KEY (unit_id)REFERENCES main.urm_product_unit (unit_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_dist_docitem ADD CONSTRAINT urm_doc_dist_docitem_fkFOREIGN KEY (doc_id)REFERENCES main.urm_product_doc (doc_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_dist_schemaitem ADD CONSTRAINT urm_schema_dist_schemaitem_fkFOREIGN KEY (schema_id)REFERENCES main.urm_product_schema (schema_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_dist_comp ADD CONSTRAINT urm_product_dist_comp_fkFOREIGN KEY (meta_id)REFERENCES main.urm_product_meta (meta_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;ALTER TABLE main.urm_dist_compitem ADD CONSTRAINT urm_schema_dist_compitem_fkFOREIGN KEY (schema_id)REFERENCES main.urm_product_schema (schema_id)ON DELETE NO ACTIONON UPDATE NO ACTIONNOT DEFERRABLE;

-- product.design
ALTER TABLE main.urm_design_diagram ADD CONSTRAINT urm_diagram_meta_fk
FOREIGN KEY (meta_id)
REFERENCES main.urm_product_meta (meta_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
