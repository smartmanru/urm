-- product.core

-- product.design
ALTER TABLE main.urm_design_diagram ADD CONSTRAINT urm_diagram_meta_fk
FOREIGN KEY (meta_id)
REFERENCES main.urm_product_meta (meta_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;