SET TERM #;

execute block
as
begin
if (exists(select * from rdb$relations where RDB$RELATION_NAME = 'ADM_SCRIPTS')) then
  execute statement 'drop table ADM_SCRIPTS';
if (exists(select * from rdb$relations where RDB$RELATION_NAME = 'ADM_RELEASES')) then
  execute statement 'drop table ADM_RELEASES';
end#

SET TERM ;#

  CREATE TABLE adm_releases
   (	
	zrelease varchar(30) ,
	zrel_p1 int default 0 ,
	zrel_p2 int default 0 ,
	zrel_p3 int default 0 ,
	zrel_p4 int default 0 ,
	zbegin_apply_time timestamp ,
	zend_apply_time timestamp ,
	zrel_status char(1) ,
	constraint pk_adm_releases primary key (zrelease) using index idx_pk_adm_releases
   ) ;

  CREATE TABLE adm_scripts
   (	
	zrelease varchar(30) references adm_releases ,
	zdelivery varchar(30) ,
	zkey varchar(100) ,
	zschema varchar(30) ,
	zfilename varchar(255) ,
	zbegin_apply_time timestamp ,
	zend_apply_time timestamp ,
	zscript_status char(1) ,
	constraint pk_adm_scripts primary key (zrelease, zdelivery, zschema, zkey) using index idx_pk_adm_scripts
   );
