execute block
as
begin
if (exists(select * from rdb$relations where RDB$RELATION_NAME = 'ADM_SCRIPTS')) then
  execute statement 'drop table ADM_SCRIPTS';
if (exists(select * from rdb$relations where RDB$RELATION_NAME = 'ADM_RELEASES')) then
  execute statement 'drop table ADM_RELEASES';
end;

  CREATE TABLE adm_releases
   (	
	release varchar(30),
	rel_p1 int default 0,
	rel_p2 int default 0,
	rel_p3 int default 0,
	rel_p4 int default 0,
	begin_apply_time timestamp without time zone,
	end_apply_time timestamp without time zone,
	rel_status char(1) ,
	constraint pk_adm_releases primary key (release) using index idx_pk_adm_releases
   ) ;

  CREATE TABLE adm_scripts
   (	
	release varchar(30) references adm_releases,
	delivery varchar(30),
	key varchar(100),
	schema varchar(30),
	filename varchar(255),
	begin_apply_time timestamp without time zone,
	end_apply_time timestamp without time zone,
	script_status char(1) ,
	constraint pk_adm_scripts primary key (release, delivery, schema, key) using index idx_pk_adm_scripts
   );
