package org.urm.db;

// all queries executed versus database
public abstract class DBQueries {

	public static String QUERY_SEQ_GETNEXTVAL0 = "select nextval( 'urm_object_seq' )";
	public static String QUERY_NAMES_GETALL0 = "select parent , name , id from urm_object_name";
	public static String QUERY_ENUMS_GETALL0 = "select category , id , name from urm_object_type order by category , id";
	public static String QUERY_ENUMS_DROP0 = "delete from urm_object_type";
	public static String QUERY_ENUMS_ADD3 = "insert into urm_object_type( category , id , name ) values ( @1@ , @2@ , '@3@' )";
}
