
CREATE TABLE main.urm_product_doc (
                doc_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR,
                ext VARCHAR(30) NOT NULL,
                unitbound BOOLEAN NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_product_doc_pk PRIMARY KEY (doc_id)
);
COMMENT ON TABLE main.urm_product_doc IS 'Product document';


CREATE TABLE main.urm_design_diagram (
                diagram_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR,
                diagram_type INTEGER NOT NULL,
                env_name VARCHAR(30),
                env_id INTEGER,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_design_diagram_pk PRIMARY KEY (diagram_id)
);
COMMENT ON TABLE main.urm_design_diagram IS 'Product design diagram';


CREATE TABLE main.urm_design_group (
                dggroup_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                diagram_id INTEGER NOT NULL,
                name VARCHAR(30) NOT NULL,
                xdesc VARCHAR NOT NULL,
                line_color VARCHAR(30),
                fill_color VARCHAR(30),
                CONSTRAINT urm_design_group_pk PRIMARY KEY (dggroup_id)
);
COMMENT ON TABLE main.urm_design_group IS 'Product design diagram group of elements';


CREATE TABLE main.urm_design_grouplink (
                src_dggroup_id INTEGER NOT NULL,
                dst_dggroup_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                xinfo VARCHAR(64),
                dglink_type INTEGER NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_design_grouplink_pk PRIMARY KEY (src_dggroup_id, dst_dggroup_id)
);
COMMENT ON TABLE main.urm_design_grouplink IS 'Design diagram group-to-group link';


CREATE TABLE main.urm_design_item (
                dgitem_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                diagram_id INTEGER NOT NULL,
                dggroup_id INTEGER,
                name VARCHAR(64) NOT NULL,
                xdesc VARCHAR NOT NULL,
                dgitem_type INTEGER NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_design_item_pk PRIMARY KEY (dgitem_id)
);
COMMENT ON TABLE main.urm_design_item IS 'Diagram design element';


CREATE TABLE main.urm_design_itemgrouplink (
                src_dgitem_id INTEGER NOT NULL,
                dst_dggroup_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                xinfo VARCHAR(64),
                dglink_type INTEGER NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_design_itemgrouplink_pk PRIMARY KEY (src_dgitem_id, dst_dggroup_id)
);
COMMENT ON TABLE main.urm_design_itemgrouplink IS 'Design diagram item-to-group link';


CREATE TABLE main.urm_design_groupitemlink (
                src_dggroup_id INTEGER NOT NULL,
                dst_dgitem_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                xinfo VARCHAR(64),
                dglink_type INTEGER NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_design_groupitemlink_pk PRIMARY KEY (src_dggroup_id, dst_dgitem_id)
);
COMMENT ON TABLE main.urm_design_groupitemlink IS 'Design diagram group-to-item link';


CREATE TABLE main.urm_design_itemlink (
                src_dgitem_id INTEGER NOT NULL,
                dst_dgitem_id INTEGER NOT NULL,
                meta_id INTEGER NOT NULL,
                xinfo VARCHAR(64),
                dglink_type INTEGER NOT NULL,
                pv INTEGER NOT NULL,
                CONSTRAINT urm_design_itemlink_pk PRIMARY KEY (src_dgitem_id, dst_dgitem_id)
);
COMMENT ON TABLE main.urm_design_itemlink IS 'Design diagram item-to-item link';


ALTER TABLE main.urm_design_item ADD CONSTRAINT urm_design_diagram_element_fk
FOREIGN KEY (diagram_id)
REFERENCES main.urm_design_diagram (diagram_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_design_group ADD CONSTRAINT urm_design_diagram_group_fk
FOREIGN KEY (diagram_id)
REFERENCES main.urm_design_diagram (diagram_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_design_grouplink ADD CONSTRAINT urm_design_group_grouplink_src_fk
FOREIGN KEY (src_dggroup_id)
REFERENCES main.urm_design_group (dggroup_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_design_item ADD CONSTRAINT urm_design_group_item_fk
FOREIGN KEY (dggroup_id)
REFERENCES main.urm_design_group (dggroup_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_design_grouplink ADD CONSTRAINT urm_design_group_grouplink_dst_fk
FOREIGN KEY (dst_dggroup_id)
REFERENCES main.urm_design_group (dggroup_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_design_groupitemlink ADD CONSTRAINT urm_design_group_groupitemlink_fk
FOREIGN KEY (src_dggroup_id)
REFERENCES main.urm_design_group (dggroup_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_design_itemgrouplink ADD CONSTRAINT urm_design_group_itemgrouplink_fk
FOREIGN KEY (dst_dggroup_id)
REFERENCES main.urm_design_group (dggroup_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_design_itemlink ADD CONSTRAINT urm_design_item_itemlink_src_fk
FOREIGN KEY (src_dgitem_id)
REFERENCES main.urm_design_item (dgitem_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_design_itemlink ADD CONSTRAINT urm_design_item_itemlink_dst_fk
FOREIGN KEY (dst_dgitem_id)
REFERENCES main.urm_design_item (dgitem_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_design_groupitemlink ADD CONSTRAINT urm_design_item_groupitemlink_fk
FOREIGN KEY (dst_dgitem_id)
REFERENCES main.urm_design_item (dgitem_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE main.urm_design_itemgrouplink ADD CONSTRAINT urm_design_item_itemgrouplink_fk
FOREIGN KEY (src_dgitem_id)
REFERENCES main.urm_design_item (dgitem_id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
