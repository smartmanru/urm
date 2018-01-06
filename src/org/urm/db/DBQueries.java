package org.urm.db;

// all queries executed versus database
public abstract class DBQueries {

	public static String QUERY_SEQ_GETNEXTVAL0 = "select nextval( 'urm_object_seq' )";

	public static String QUERY_VERSIONS_GETVERSION1 = "select owner_object_id , version , objectversion_type , last_import_id , last_name , ownerstatus_type from urm_object_version where owner_object_id = @1@";
	public static String MODIFY_VERSIONS_MERGEVERSION6 = "insert into urm_object_version ( owner_object_id , version , objectversion_type , last_import_id , last_name , ownerstatus_type ) values( @values@ ) on conflict (owner_object_id) do update set version = excluded.version , objectversion_type = excluded.objectversion_type , last_import_id = excluded.last_import_id , last_name = excluded.last_name , ownerstatus_type = excluded.ownerstatus_type";
	
	public static String QUERY_NAMES_GETALL0 = "select parent , object_type , name , object_id from urm_object_name";
	public static String MODIFY_NAMES_MERGEITEM4 = "insert into urm_object_name ( parent , object_type , name , object_id ) values ( @values@ ) on conflict (parent,object_type,name) do update set object_id = excluded.object_id";
	
	public static String QUERY_ENUMS_GETALL0 = "select category , item , name from urm_object_type order by category , item";
	public static String MODIFY_ENUMS_DROP0 = "delete from urm_object_type";
	public static String MODIFY_ENUMS_ADD4 = "insert into urm_object_type( category , item , name , av ) values ( @values@ )";
	
	public static String MODIFY_PARAM_ADDENTITY11 = "insert into urm_object_entity ( param_object_id , paramentity_type , custom , use_props , app_table , id_field , object_type , meta_object_id , meta_objectversion_type , data_objectversion_type , version ) values ( @values@ )";
	public static String QUERY_PARAM_ENTITY2 = "select custom , use_props , app_table , id_field , object_type , meta_object_id , meta_objectversion_type , data_objectversion_type , version from urm_object_entity where param_object_id = @1@ and paramentity_type = @2@";
	public static String MODIFY_PARAM_ADDPARAM15 = "insert into urm_object_param ( param_object_id , paramentity_type , param_id , entitycolumn , name , dbname , xmlname , xdesc , paramvalue_type , paramvalue_subtype , object_type , enumname , required , expr_def , version ) values ( @values@ )";
	public static String MODIFY_PARAM_UPDATEPARAM15 = "update urm_object_param set entitycolumn = @4@ , name = @5@ , dbname = @6@ , xmlname = @7@ , xdesc = @8@ , paramvalue_type = @9@ , paramvalue_subtype = @10@ , object_type = @11@ , enumname = @12@ , required = @13@ , expr_def = @14@ , version = @15@ where param_object_id = @1@ and paramentity_type = @2@ and param_id = @3@";
	public static String MODIFY_PARAM_DROPENTITY2 = "delete from urm_object_entity where param_object_id = @1@ and paramentity_type = @2@";
	public static String MODIFY_PARAM_DROPENTITYPARAMS2 = "delete from urm_object_param where param_object_id = @1@ and paramentity_type = @2@";
	public static String MODIFY_PARAM_DROPPARAM1 = "delete from urm_object_param where param_id = @1@";
	public static String MODIFY_PARAM_DROPPARAMVALUES1 = "delete from urm_object_param_value where param_id = @1@";
	public static String MODIFY_PARAM_DECREMENTENTITYINDEX3 = "update urm_object_param set entitycolumn = entitycolumn - 1 where param_object_id = @1@ and paramentity_type = @2@ and entitycolumn > @3@";
	public static String MODIFY_PARAM_DROPENTITYVALUES2 = "delete from urm_object_param_value where param_object_id = @1@ and paramentity_type = @2@";
	public static String QUERY_PARAM_GETENTITYPARAMS2 = "select param_id , name , dbname , xmlname , xdesc , paramvalue_type , paramvalue_subtype , object_type , enumname , required , expr_def , version from urm_object_param where param_object_id = @1@ and paramentity_type = @2@ order by entitycolumn";
	public static String QUERY_PARAM_GETOBJECTPARAMVALUES2 = "select paramentity_type , param_id , expr_value , version from urm_object_param_value where object_id = @1@ and paramrole_type = @2@";
	public static String MODIFY_PARAM_DROPOBJECTPARAMVALUES2 = "delete from urm_object_param_value where object_id = @1@ and paramrole_type = @2@";
	public static String MODIFY_PARAM_DROPOBJECTPARAMVALUESAPP2 = "delete from urm_object_param_value where object_id = @1@ and paramrole_type = @2@ and ( param_object_id , paramentity_type ) in ( select param_object_id , paramentity_type from urm_object_entity where custom = 'no' )";
	public static String MODIFY_PARAM_DROPOBJECTPARAMVALUESCUSTOM2 = "delete from urm_object_param_value where object_id = @1@ and paramrole_type = @2@ and ( param_object_id , paramentity_type ) in ( select param_object_id , paramentity_type from urm_object_entity where custom = 'yes' )";
	public static String MODIFY_PARAM_ADDOBJECTPARAMVALUE7  = "insert into urm_object_param_value ( object_id , paramrole_type , param_object_id , paramentity_type , param_id , expr_value , version ) values ( @values@ )";
	public static String MODIFY_PARAM_DROPOWNERVALUES1 = "delete from urm_object_param_value where param_object_id = @1@";
	public static String MODIFY_PARAM_DROPOWNERPARAMS1 = "delete from urm_object_param where param_object_id = @1@";
	public static String MODIFY_PARAM_DROPOWNERENTITIES1 = "delete from urm_object_entity where param_object_id = @1@";
	public static String MODIFY_PARAM_DROPOBJECTVALUES1 = "delete from urm_object_param_value where object_id = @1@";
	
	public static String MODIFY_SYSTEM_MATCHED3 = "update urm_system set matched = @2@ , sv = @3@ where system_id = @1@";

	public static String MODIFY_AUTH_DROP_ACCESSPRODUCT0 = "delete from urm_auth_access_product";
	public static String MODIFY_AUTH_DROP_ACCESSRESOURCE0 = "delete from urm_auth_access_resource";
	public static String MODIFY_AUTH_DROP_ACCESSNETWORK0 = "delete from urm_auth_access_network";
	public static String MODIFY_AUTH_DROP_GROUPUSERS0 = "delete from urm_auth_groupuser";
	public static String MODIFY_AUTH_DROP_USER0 = "delete from urm_auth_user";
	public static String MODIFY_AUTH_DROP_GROUP0 = "delete from urm_auth_group";
	
	public static String MODIFY_BASE_DROP_ITEMDEPS0 = "delete from urm_base_item_deps";
	public static String MODIFY_BASE_ADDDEPITEM3 = "insert into urm_base_item_deps ( item_id , dep_item_id , cv ) values ( @values@ )";
	public static String MODIFY_BASE_DELETEDEPITEM2 = "delete from urm_base_item_deps where item_id = @1@ and dep_item_id = @2@";
	public static String QUERY_BASE_ITEMDEPS0 = "select item_id , dep_item_id , cv from urm_base_item_deps";

	public static String MODIFY_APP_DROP_SYSTEMPARAMVALUES0 = "delete from urm_object_param_value where param_object_id in ( select system_id from urm_system )";
	public static String MODIFY_APP_DROP_SYSTEMPARAMS0 = "delete from urm_object_param where param_object_id in ( select system_id from urm_system )";
	public static String MODIFY_APP_DROP_SYSTEMENTITIES0 = "delete from urm_object_entity where param_object_id in ( select system_id from urm_system )";

	public static String MODIFY_CORE_DROP_PARAMVALUE1 = "delete from urm_object_param_value where param_object_id = @1@";
	public static String MODIFY_CORE_DROP_PARAM1 = "delete from urm_object_param where param_object_id = @1@";
	public static String MODIFY_CORE_DROP_ENTITY1 = "delete from urm_object_entity where param_object_id = @1@";
	
	public static String MODIFY_LIFECYCLE_DROPPHASES1 = "delete from urm_lifecycle_phase where lifecycle_id = @1@";

	public static String MODIFY_AUTH_DROP_DATACENTERACCESS1 = "delete from urm_auth_access_network where network_id in ( select network_id from urm_network where datacenter_id = @1@ )";
	public static String MODIFY_AUTH_DROP_RESOURCEACCESS1 = "delete from urm_auth_access_resource where resource_id = @1@";
	public static String MODIFY_AUTH_DROP_NETWORKACCESS1 = "delete from urm_auth_access_network where network_id = @1@";
	public static String MODIFY_AUTH_DROP_PRODUCTACCESS1 = "delete from urm_auth_access_product where product_id = @1@";
	public static String MODIFY_AUTH_DROPGROUP_RESOURCEACCESS1 = "delete from urm_auth_access_resource where group_id = @1@";
	public static String MODIFY_AUTH_DROPGROUP_NETWORKACCESS1 = "delete from urm_auth_access_network where group_id = @1@";
	public static String MODIFY_AUTH_DROPGROUP_PRODUCTACCESS1 = "delete from urm_auth_access_product where group_id = @1@";
	public static String MODIFY_AUTH_DROPGROUP_USERS1 = "delete from urm_auth_groupuser where group_id = @1@";
	public static String MODIFY_AUTH_DROPUSER_GROUPS1 = "delete from urm_auth_groupuser where user_id = @1@";
	public static String MODIFY_AUTH_GROUPUSER_ADD3 = "insert into urm_auth_groupuser ( group_id , user_id , uv ) values ( @values@ )";
	public static String MODIFY_AUTH_GROUPUSER_DROP2 = "delete from urm_auth_groupuser where group_id = @1@ and user_id = @2@";
	public static String MODIFY_AUTH_GROUPACCESS_ADDRESOURCE3 = "insert into urm_auth_access_resource ( group_id , resource_id , uv ) values ( @values@ )";
	public static String MODIFY_AUTH_GROUPACCESS_ADDPRODUCT3 = "insert into urm_auth_access_product ( group_id , product_id , uv ) values ( @values@ )";
	public static String MODIFY_AUTH_GROUPACCESS_ADDNETWORK3 = "insert into urm_auth_access_network ( group_id , network_id , uv ) values ( @values@ )";
	public static String MODIFY_AUTH_GROUPACCESS_DROPRESOURCES1 = "delete from urm_auth_access_resource where group_id = @1@";
	public static String MODIFY_AUTH_GROUPACCESS_DROPPRODUCTS1 = "delete from urm_auth_access_product where group_id = @1@";
	public static String MODIFY_AUTH_GROUPACCESS_DROPNETWORKS1 = "delete from urm_auth_access_network where group_id = @1@";
	public static String QUERY_AUTH_GROUPACCESS_RESOURCES0 = "select group_id , resource_id , uv from urm_auth_access_resource";
	public static String QUERY_AUTH_GROUPACCESS_PRODUCTS0 = "select group_id , product_id , uv from urm_auth_access_product";
	public static String QUERY_AUTH_GROUPACCESS_NETWORKS0 = "select group_id , network_id , uv from urm_auth_access_network";
	public static String QUERY_AUTH_GROUPUSERS0 = "select group_id , user_id , uv from urm_auth_groupuser";
	
}
