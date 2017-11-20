package org.urm.db;

// all queries executed versus database
public abstract class DBQueries {

	public static String QUERY_SEQ_GETNEXTVAL0 = "select nextval( 'urm_object_seq' )";

	public static String QUERY_VERSIONS_GETVERSION1 = "select version from urm_object_version where id = @1@";
	public static String MODIFY_VERSIONS_MERGEVERSION3 = "insert into urm_object_version ( id , version , object_version_type ) values( @1@ , @2@ , @3@ ) on conflict (id) do update set version = excluded.version , object_version_type = excluded.object_version_type";
	
	public static String QUERY_NAMES_GETALL0 = "select parent , object_type , name , id from urm_object_name";
	public static String MODIFY_NAMES_DROPPARENT1 = "delete from urm_object_name where parent = @1@";
	public static String MODIFY_NAMES_MERGEITEM4 = "insert into urm_object_name ( parent , object_type , name , id ) values ( @1@ , @2@ , @3@ , @4@ ) on conflict (parent,object_type,name) do update set id = excluded.id";
	
	public static String QUERY_ENUMS_GETALL0 = "select category , item , name from urm_object_type order by category , item";
	public static String MODIFY_ENUMS_DROP0 = "delete from urm_object_type";
	public static String MODIFY_ENUMS_ADD4 = "insert into urm_object_type( category , item , name , av ) values ( @1@ , @2@ , @3@ , @4@ )";
	
	public static String MODIFY_PARAM_ADD10 = "insert into urm_object_param( owner_object , paramentity_type , id , name , xdesc , paramvalue_type , required , custom , expr_def , version ) values ( @1@ , @2@ , @3@ , @4@ , @5@ , @6@ , @7@ , @8@ , @9@ , @10@ )";
	public static String QUERY_PARAM_GETENTITYFIXEDPARAMS2 = "select id , name , xdesc , paramvalue_type , required , expr_def , version from urm_object_param where owner_object = @1@ and paramentity_type = @2@ and custom = 'no' order by name";
	
	public static String QUERY_SYSTEM_GETALL0 = "select id , name , xdesc , offline , matched , sv from urm_system";
	public static String MODIFY_SYSTEM_ADD6 = "insert into urm_system ( id , name , xdesc , offline , matched , sv ) values ( @1@ , @2@ , @3@ , @4@ , @5@ , @6@ )";
	public static String MODIFY_SYSTEM_UPDATE4 = "update urm_system set name = @2@ , xdesc = @3@ , sv = @4@ where id = @1@";
	public static String MODIFY_SYSTEM_DELETE2 = "delete from urm_system where id = @1@";
	public static String MODIFY_SYSTEM_DELETEALLPARAMS2 = "delete from urm_system_param where system = @1@";

	public static String QUERY_PRODUCT_GETALL0 = "select id , system , name , xdesc , path , offline , monitoring_enabled , sv from urm_product";
	public static String MODIFY_PRODUCT_ADD8 = "insert into urm_product ( id , system , name , xdesc , path , offline , monitoring_enabled , sv ) values ( @1@ , @2@ , @3@ , @4@ , @5@ , @6@ , @7@ , @8@ )";
	
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

	public static String MODIFY_APP_DROP_SYSTEMPARAMVALUE0 = "delete from urm_object_param_value where owner_object in ( select id from urm_system )";
	public static String MODIFY_APP_DROP_SYSTEMPARAM0 = "delete from urm_object_param where owner_object in ( select id from urm_system )";
	public static String MODIFY_APP_DROP_PRODUCT0 = "delete from urm_product";
	public static String MODIFY_APP_DROP_SYSTEM0 = "delete from urm_system";

	public static String MODIFY_CORE_DROP_PARAMVALUE1 = "delete from urm_object_param_value where owner_object = @1@";
	public static String MODIFY_CORE_DROP_PARAM1 = "delete from urm_object_param where owner_object = @1@";
	public static String MODIFY_CORE_DROP_RESOURCE0 = "delete from urm_resource";
	public static String MODIFY_CORE_DROP_MIRROR0 = "delete from urm_mirror";
	
	public static String MODIFY_RELEASES_DROP_BUILDERS0 = "delete from urm_project_builder";
	
}
