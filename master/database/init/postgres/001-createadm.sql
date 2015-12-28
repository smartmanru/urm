drop table if exists adm_releases;
drop table if exists adm_scripts;

  CREATE TABLE adm_releases
   (	
	release varchar(30),
	rel_p1 integer default 0,
	rel_p2 integer default 0,
	rel_p3 integer default 0,
	rel_p4 integer default 0,
	begin_apply_time timestamp without time zone,
	end_apply_time timestamp without time zone,
	rel_status char(1)
   ) ;

  CREATE UNIQUE INDEX CONCURRENTLY adm_releases_pk ON adm_releases (release);
  ALTER TABLE adm_releases ADD CONSTRAINT adm_releases_pk PRIMARY KEY USING INDEX adm_releases_pk;

  CREATE TABLE adm_scripts
   (	
	release varchar(30),
	delivery varchar(30),
	key varchar(100),
	schema varchar(30),
	filename varchar(255),
	begin_apply_time timestamp without time zone,
	end_apply_time timestamp without time zone,
	script_status char(1)
   );

  CREATE UNIQUE INDEX CONCURRENTLY adm_scripts_pk ON adm_scripts (release, delivery, schema, key);
  ALTER TABLE adm_scripts ADD CONSTRAINT adm_scripts_pk PRIMARY KEY USING INDEX adm_scripts_pk;
  ALTER TABLE adm_scripts ADD CONSTRAINT adm_scripts_fk FOREIGN KEY (release) REFERENCES adm_releases (release) MATCH FULL;
