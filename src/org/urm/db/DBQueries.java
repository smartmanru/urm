package org.urm.db;

// all queries executed versus database
public abstract class DBQueries {

	public static String QUERY_VERSIONS_GETVERSION1 = "select version from urm_object_version where id = @1@";
	public static String UPDATE_VERSIONS_INSERTVERSION2 = "insert into urm_object_version ( id , version ) values( @1@ , @2@ )";
	public static String UPDATE_VERSIONS_UPDATEVERSION2 = "update urm_object_version set version = @2@ where id = @1@";
	public static String QUERY_VERSIONS_SETSV1 = "select version from urm_object_version where id = 0";
	public static String QUERY_SEQ_GETNEXTVAL0 = "select nextval( 'urm_object_seq' )";
	public static String QUERY_NAMES_GETALL0 = "select parent , name , id from urm_object_name";
	public static String UPDATE_NAMES_DROPPARENT1 = "delete from urm_object_name where parent = @1@";
	public static String UPDATE_NAMES_ADDITEM4 = "insert into urm_object_name ( parent , name , id , object_type ) values ( @1@ , @2@ , @3@ , @4@ )";
	public static String QUERY_ENUMS_GETALL0 = "select category , item , name from urm_object_type order by category , item";
	public static String UPDATE_ENUMS_DROP0 = "delete from urm_object_type";
	public static String UPDATE_ENUMS_ADD3 = "insert into urm_object_type( category , item , name ) values ( @1@ , @2@ , @3@ )";
	public static String QUERY_SYSTEM_GETALL0 = "select id , name , xdesc , offline , sv from urm_system";
	public static String UPDATE_SYSTEM_ADD5 = "insert into urm_system ( id , name , xdesc , offline , sv ) values ( @1@ , @2@ , @3@ , @4@ , @5@ )";
	
	public static String UPDATE_AUTH_DROP_ACCESSPRODUCT0 = "delete from urm_auth_access_product";
	public static String UPDATE_AUTH_DROP_ACCESSRESOURCE0 = "delete from urm_auth_access_resource";
	public static String UPDATE_AUTH_DROP_ACCESSNETWORK0 = "delete from urm_auth_access_network";
	public static String UPDATE_AUTH_DROP_USER0 = "delete from urm_auth_user";
	public static String UPDATE_AUTH_DROP_GROUP0 = "delete from urm_auth_group";
	
	public static String UPDATE_INFRA_DROP_ACCOUNT0 = "delete from urm_account";
	public static String UPDATE_INFRA_DROP_HOST0 = "delete from urm_host";
	public static String UPDATE_INFRA_DROP_NETWORK0 = "delete from urm_network";
	public static String UPDATE_INFRA_DROP_DATACENTER0 = "delete from urm_datacenter";

	public static String UPDATE_BASE_DROP_ITEMPARAM0 = "delete from urm_base_item_param";
	public static String UPDATE_BASE_DROP_ITEMDEPS0 = "delete from urm_base_item_deps";
	public static String UPDATE_BASE_DROP_ITEM0 = "delete from urm_base_item";
	public static String UPDATE_BASE_DROP_GROUP0 = "delete from urm_base_group";
	public static String UPDATE_BASE_DROP_CATEGORY0 = "delete from urm_base_category";

	public static String UPDATE_APP_DROP_SYSTEMPARAM0 = "delete from urm_system_param";
	public static String UPDATE_APP_DROP_PRODUCT0 = "delete from urm_product";
	public static String UPDATE_APP_DROP_SYSTEM0 = "delete from urm_system";

	public static String UPDATE_CORE_DROP_RESOURCE0 = "delete from urm_resource";
	public static String UPDATE_CORE_DROP_PARAM0 = "delete from urm_param";
	
}
