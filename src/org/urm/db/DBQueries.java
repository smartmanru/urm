package org.urm.db;

// all queries executed versus database
public abstract class DBQueries {

	public static String QUERY_SEQ_GETNEXTVAL0 = "select nextval( 'urm_object_seq' )";

	public static String QUERY_VERSIONS_GETVERSION1 = "select version from urm_object_version where id = @1@";
	public static String MODIFY_VERSIONS_MERGEVERSION3 = "insert into urm_object_version ( id , version , object_version_type ) values( @1@ , @2@ , @3@ ) on conflict (id) do update set version = excluded.version , object_version_type = excluded.object_version_type";
	
	public static String QUERY_NAMES_GETALL0 = "select parent , name , id from urm_object_name";
	public static String MODIFY_NAMES_DROPPARENT1 = "delete from urm_object_name where parent = @1@";
	public static String MODIFY_NAMES_MERGEITEM4 = "insert into urm_object_name ( parent , name , id , object_type ) values ( @1@ , @2@ , @3@ , @4@ ) on conflict (parent,name) do update set id = excluded.id , object_type = excluded.object_type";
	
	public static String QUERY_ENUMS_GETALL0 = "select category , item , name from urm_object_type order by category , item";
	public static String MODIFY_ENUMS_DROP0 = "delete from urm_object_type";
	public static String MODIFY_ENUMS_ADD3 = "insert into urm_object_type( category , item , name ) values ( @1@ , @2@ , @3@ )";
	
	public static String QUERY_SYSTEM_GETALL0 = "select id , name , xdesc , offline , matched , sv from urm_system";
	public static String MODIFY_SYSTEM_ADD6 = "insert into urm_system ( id , name , xdesc , offline , matched , cv ) values ( @1@ , @2@ , @3@ , @4@ , @5@ , @6@ )";
	public static String MODIFY_SYSTEM_UPDATE4 = "update urm_system set name = @2@ , xdesc = @3@ , cv = @4@ where id = @1@";
	public static String MODIFY_SYSTEM_DELETE2 = "delete from urm_system where id = @1@";
	public static String MODIFY_SYSTEM_DELETEALLPARAMS2 = "delete from urm_system_param where system = @1@";
	
	public static String MODIFY_AUTH_DROP_ACCESSPRODUCT0 = "delete from urm_auth_access_product";
	public static String MODIFY_AUTH_DROP_ACCESSRESOURCE0 = "delete from urm_auth_access_resource";
	public static String MODIFY_AUTH_DROP_ACCESSNETWORK0 = "delete from urm_auth_access_network";
	public static String MODIFY_AUTH_DROP_USER0 = "delete from urm_auth_user";
	public static String MODIFY_AUTH_DROP_GROUP0 = "delete from urm_auth_group";
	
	public static String MODIFY_INFRA_DROP_ACCOUNT0 = "delete from urm_account";
	public static String MODIFY_INFRA_DROP_HOST0 = "delete from urm_host";
	public static String MODIFY_INFRA_DROP_NETWORK0 = "delete from urm_network";
	public static String MODIFY_INFRA_DROP_DATACENTER0 = "delete from urm_datacenter";

	public static String MODIFY_BASE_DROP_ITEMDEPS0 = "delete from urm_base_item_deps";
	public static String MODIFY_BASE_DROP_ITEM0 = "delete from urm_base_item";
	public static String MODIFY_BASE_DROP_GROUP0 = "delete from urm_base_group";
	public static String MODIFY_BASE_DROP_CATEGORY0 = "delete from urm_base_category";

	public static String MODIFY_APP_DROP_SYSTEMPARAM0 = "delete from urm_system_param";
	public static String MODIFY_APP_DROP_PRODUCT0 = "delete from urm_product";
	public static String MODIFY_APP_DROP_SYSTEM0 = "delete from urm_system";

	public static String MODIFY_CORE_DROP_RESOURCE0 = "delete from urm_resource";
	public static String MODIFY_CORE_DROP_MIRROR0 = "delete from urm_mirror";
	public static String MODIFY_CORE_DROP_COREPARAM0 = "delete from urm_core_param";
	
	public static String MODIFY_RELEASES_DROP_BUILDERS0 = "delete from urm_project_builder";
	
}
