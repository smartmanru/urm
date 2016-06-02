drop table if exists adm_scripts;
drop table if exists adm_releases;

  CREATE TABLE adm_releases
   (	
	zrelease varchar(30),
	zrel_p1 integer default 0,
	zrel_p2 integer default 0,
	zrel_p3 integer default 0,
	zrel_p4 integer default 0,
	zbegin_apply_time timestamp without time zone,
	zend_apply_time timestamp without time zone,
	zrel_status char(1)
   ) ;

  CREATE UNIQUE INDEX CONCURRENTLY adm_releases_pk ON adm_releases (zrelease);
  ALTER TABLE adm_releases ADD CONSTRAINT adm_releases_pk PRIMARY KEY USING INDEX adm_releases_pk;

  CREATE TABLE adm_scripts
   (	
	zrelease varchar(30),
	zdelivery varchar(30),
	zkey varchar(100),
	zschema varchar(30),
	zfilename varchar(255),
	zbegin_apply_time timestamp without time zone,
	zend_apply_time timestamp without time zone,
	zscript_status char(1)
   );

  CREATE UNIQUE INDEX CONCURRENTLY adm_scripts_pk ON adm_scripts (zrelease, zdelivery, zschema, zkey);
  ALTER TABLE adm_scripts ADD CONSTRAINT adm_scripts_pk PRIMARY KEY USING INDEX adm_scripts_pk;
  ALTER TABLE adm_scripts ADD CONSTRAINT adm_scripts_fk FOREIGN KEY (zrelease) REFERENCES adm_releases (zrelease) MATCH FULL;
