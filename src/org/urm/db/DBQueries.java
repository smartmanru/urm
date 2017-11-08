package org.urm.db;

// all queries executed versus database
public abstract class DBQueries {

	public static String QUERY_VERSIONS_GETSV0 = "select version from urm_object_version where id = 0";
	public static String QUERY_SEQ_GETNEXTVAL0 = "select nextval( 'urm_object_seq' )";
	public static String QUERY_NAMES_GETALL0 = "select parent , name , id from urm_object_name";
	public static String UPDATE_NAMES_DROPPARENT1 = "delete from urm_object_name where parent = @1@";
	public static String UPDATE_NAMES_ADDITEM4 = "insert into urm_object_name ( parent , name , id , object_type ) values ( @1@ , @2@ , @3@ , @4@ )";
	public static String QUERY_ENUMS_GETALL0 = "select category , item , name from urm_object_type order by category , item";
	public static String UPDATE_ENUMS_DROP0 = "delete from urm_object_type";
	public static String UPDATE_ENUMS_ADD3 = "insert into urm_object_type( category , item , name ) values ( @1@ , @2@ , @3@ )";
	public static String QUERY_SYSTEM_GETALL0 = "select id , name , xdesc , offline , sv from urm_system";
	public static String UPDATE_SYSTEM_ADD5 = "insert into urm_system ( id , name , xdesc , offline , sv ) values ( @1@ , @2@ , @3@ , @4@ , @5@ )";
}
