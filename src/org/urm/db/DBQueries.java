package org.urm.db;

// all queries executed versus database
public abstract class DBQueries {

	public static String QUERY_SEQ_GETNEXTVAL0 = "select nextval( 'urm_object_seq' )";

	public static String QUERY_VERSIONS_GETVERSION1 = "select owner_object_id , version , object_version_type , last_import_id , last_name , ownerstatus_type from urm_object_version where owner_object_id = @1@";
	public static String MODIFY_VERSIONS_MERGEVERSION6 = "insert into urm_object_version ( owner_object_id , version , object_version_type , last_import_id , last_name , ownerstatus_type ) values( @values@ ) on conflict (owner_object_id) do update set version = excluded.version , object_version_type = excluded.object_version_type , last_import_id = excluded.last_import_id , last_name = excluded.last_name , ownerstatus_type = excluded.ownerstatus_type";
	
	public static String QUERY_NAMES_GETALL0 = "select parent , object_type , name , object_id from urm_object_name";
	public static String MODIFY_NAMES_DROPPARENT1 = "delete from urm_object_name where parent = @1@";
	public static String MODIFY_NAMES_MERGEITEM4 = "insert into urm_object_name ( parent , object_type , name , object_id ) values ( @values@ ) on conflict (parent,object_type,name) do update set object_id = excluded.object_id";
	
	public static String QUERY_ENUMS_GETALL0 = "select category , item , name from urm_object_type order by category , item";
	public static String MODIFY_ENUMS_DROP0 = "delete from urm_object_type";
	public static String MODIFY_ENUMS_ADD4 = "insert into urm_object_type( category , item , name , av ) values ( @values@ )";
	
	public static String MODIFY_PARAM_ADDENTITY7 = "insert into urm_object_entity ( param_object_id , paramentity_type , custom , app_props , app_table , param_object_type , version ) values ( @values@ )";
	public static String MODIFY_PARAM_ADDPARAM10 = "insert into urm_object_param ( param_object_id , paramentity_type , param_id , name , xdesc , paramvalue_type , object_type , required , expr_def , version ) values ( @values@ )";
	public static String MODIFY_PARAM_DROPENTITY2 = "delete from urm_object_entity where param_object_id = @1@ and paramentity_type = @2@";
	public static String MODIFY_PARAM_DROPENTITYPARAMS2 = "delete from urm_object_param where param_object_id = @1@ and paramentity_type = @2@";
	public static String MODIFY_PARAM_DROPENTITYVALUES2 = "delete from urm_object_param_value where param_object_id = @1@ and paramentity_type = @2@";
	public static String QUERY_PARAM_GETENTITYPARAMS2 = "select param_id , name , xdesc , paramvalue_type , object_type , required , expr_def , version from urm_object_param where param_object_id = @1@ and paramentity_type = @2@ order by name";
	public static String QUERY_PARAM_GETOBJECTPARAMVALUES2 = "select paramentity_type , param_id , expr_value , version from urm_object_param_value where object_id = @1@ and paramrole_type = @2@";
	public static String MODIFY_PARAM_DROPOBJECTPARAMVALUES2 = "delete from urm_object_param_value where object_id = @1@ and paramrole_type = @2@";
	public static String MODIFY_PARAM_ADDOBJECTPARAMVALUE7  = "insert into urm_object_param_value ( object_id , paramrole_type , param_object_id , paramentity_type , param_id , expr_value , version ) values ( @values@ )";
	public static String MODIFY_PARAM_DROPOWNERVALUES1 = "delete from urm_object_param_value where param_object_id = @1@";
	public static String MODIFY_PARAM_DROPOWNERPARAMS1 = "delete from urm_object_param where param_object_id = @1@";
	public static String MODIFY_PARAM_DROPOWNERENTITIES1 = "delete from urm_object_entity where param_object_id = @1@";
	public static String MODIFY_PARAM_DROPOBJECTVALUES1 = "delete from urm_object_param_value where object_id = @1@";
	
	public static String QUERY_SYSTEM_GETALL0 = "select system_id , name , xdesc , offline , matched , sv from urm_system";
	public static String MODIFY_SYSTEM_ADD6 = "insert into urm_system ( system_id , name , xdesc , offline , matched , sv ) values ( @values@ )";
	public static String MODIFY_SYSTEM_UPDATE5 = "update urm_system set name = @2@ , xdesc = @3@ , offline=@4@ , sv = @5@ where system_id = @1@";
	public static String MODIFY_SYSTEM_DELETE2 = "delete from urm_system where system_id = @1@";
	public static String MODIFY_SYSTEM_MATCHED2 = "update urm_system set matched = @2@ where system_id = @1@";

	public static String QUERY_PRODUCT_GETALL0 = "select product_id , system_id , name , xdesc , path , offline , monitoring_enabled , sv from urm_product";
	public static String MODIFY_PRODUCT_ADD8 = "insert into urm_product ( product_id , system_id , name , xdesc , path , offline , monitoring_enabled , sv ) values ( @values@ )";
	
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
	public static String MODIFY_BASE_ADDITEM19 = "insert into urm_base_item ( item_id , group_id , name , xdesc , basesrc_type , basesrcformat_type , os_type , serveraccess_type , basename , baseversion , srcdir , srcfile , srcfiledir , installscript , installpath , installlink , charset , offline , cv ) values ( @values@ )";
	public static String MODIFY_BASE_ADDGROUP6 = "insert into urm_base_group ( group_id , basecategory_type , name , xdesc , offline , cv ) values ( @values@ )";

	public static String MODIFY_APP_DROP_SYSTEMPARAMVALUES0 = "delete from urm_object_param_value where param_object_id in ( select system_id from urm_system )";
	public static String MODIFY_APP_DROP_SYSTEMPARAMS0 = "delete from urm_object_param where param_object_id in ( select system_id from urm_system )";
	public static String MODIFY_APP_DROP_SYSTEMENTITIES0 = "delete from urm_object_entity where param_object_id in ( select system_id from urm_system )";
	public static String MODIFY_APP_DROP_SYSTEMPRODUCTS0 = "delete from urm_product";
	public static String MODIFY_APP_DROP_SYSTEMS0 = "delete from urm_system";

	public static String MODIFY_CORE_DROP_PARAMVALUE1 = "delete from urm_object_param_value where param_object_id = @1@";
	public static String MODIFY_CORE_DROP_PARAM1 = "delete from urm_object_param where param_object_id = @1@";
	public static String MODIFY_CORE_DROP_ENTITY1 = "delete from urm_object_entity where param_object_id = @1@";
	public static String MODIFY_CORE_DROP_RESOURCE0 = "delete from urm_resource";
	public static String MODIFY_CORE_DROP_MIRROR0 = "delete from urm_mirror";
	
	public static String MODIFY_RELEASES_DROP_BUILDERS0 = "delete from urm_project_builder";
	
}
