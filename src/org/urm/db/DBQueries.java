package org.urm.db;

// all queries executed versus database
public abstract class DBQueries {

	public static String FILTER_META_ID1 = "meta_id = @1@";
	public static String FILTER_META_FK2 = "meta_fkid = @1@ or meta_fkname = @2@";
	public static String FILTER_META_NAME1 = "name = @1@";
	public static String FILTER_DELIVERY_ID1 = "delivery_id = @1@";
	public static String FILTER_ENV_ID1 = "env_id = @1@";
	public static String FILTER_ENV_META1 = "env_id in ( select env_id from urm_env where meta_fkid = @1@ )";
	public static String FILTER_REL_META1 = "release_id in ( select release_id from urm_rel_repository a , urm_rel_main b where a.repo_id = b.repo_id and a.meta_fkid = @1@ )";
	public static String FILTER_REL_MAINMETA1 = "repo_id in ( select repo_id from urm_rel_repository where meta_fkid = @1@ )";
	public static String FILTER_REL_SCHEDULEMETA3 = "release_id in ( select release_id from urm_rel_repository a , urm_rel_main b where a.repo_id = b.repo_id and a.meta_fkid = @1@ ) and ( @2@ is null or @2@ = released ) and ( @3@ is null or @3@ = completed )";
	public static String FILTER_REL_REPOMETA1 = "meta_fkid = @1@";
	public static String FILTER_REL_REPO1 = "repo_id = @1@";
	public static String FILTER_REL_REPOACTIVE1 = "repo_id = @1@ and archived = 'no'";
	public static String FILTER_REL_REPORELEASEACTIVE1 = "release_id in ( select release_id from urm_rel_main where repo_id = @1@ and archived = 'no' )";
	public static String FILTER_REL_RELEASE1 = "release_id = @1@";
	public static String FILTER_REL_SCOPERELEASE1 = "release_id = @1@ and scopetarget = 'yes'";
	public static String FILTER_REL_TICKETSET1 = "ticketset_id = @1@";
	public static String FILTER_REL_BUILDTARGET_TICKETSET1 = "buildtarget_id in ( select buildtarget_id from urm_rel_tickettarget where ticketset_id = @1@ and buildtarget_id is not null )";
	public static String FILTER_REL_DISTTARGET_TICKETSET1 = "disttarget_id in ( select disttarget_id from urm_rel_tickettarget where ticketset_id = @1@ and disttarget_id is not null )";
	public static String FILTER_LIFECYCLE_ID1 = "lifecycle_id = @1@";
	public static String FILTER_PRODUCT_ID1 = "product_id = @1@";
	
	public static String QUERY_SEQ_GETNEXTVAL0 = "select nextval( 'urm_object_seq' )";

	public static String QUERY_VERSIONS_GETVERSION1 = "select owner_object_id , version , objectversion_type , last_import_id , last_name , ownerstatus_type from urm_object_version where owner_object_id = @1@";
	public static String MODIFY_VERSIONS_MERGEVERSION6 = "insert into urm_object_version ( owner_object_id , version , objectversion_type , last_import_id , last_name , ownerstatus_type ) values( @values@ ) on conflict (owner_object_id) do update set version = excluded.version , objectversion_type = excluded.objectversion_type , last_import_id = excluded.last_import_id , last_name = excluded.last_name , ownerstatus_type = excluded.ownerstatus_type";
	
	public static String QUERY_NAMES_GETALL0 = "select parent , paramentity_type , name , object_id from urm_object_name";
	public static String MODIFY_NAMES_MERGEITEM4 = "insert into urm_object_name ( parent , paramentity_type , name , object_id ) values ( @values@ ) on conflict (parent,paramentity_type,name) do update set object_id = excluded.object_id";
	
	public static String QUERY_ENUMS_GETALL0 = "select category , item , name from urm_object_type order by category , item";
	public static String MODIFY_ENUMS_DROP0 = "delete from urm_object_type";
	public static String MODIFY_ENUMS_ADD4 = "insert into urm_object_type( category , item , name , av ) values ( @values@ )";
	
	public static String MODIFY_PARAM_ADDENTITY13 = "insert into urm_object_entity ( param_object_id , paramentity_type , custom , use_props , changeable , pk_field_count , app_table , id_field , object_type , meta_object_id , meta_objectversion_type , data_objectversion_type , version ) values ( @values@ )";
	public static String QUERY_PARAM_ENTITY2 = "select custom , use_props , changeable , pk_field_count , app_table , id_field , object_type , meta_object_id , meta_objectversion_type , data_objectversion_type , version from urm_object_entity where param_object_id = @1@ and paramentity_type = @2@";
	public static String MODIFY_PARAM_ADDPARAM18 = "insert into urm_object_param ( param_object_id , paramentity_type , param_id , entitycolumn , name , dbname , xmlname , xdesc , paramvalue_type , paramvalue_subtype , object_type , enumname , required , secured , inherited , expr_def , customenum_def , version ) values ( @values@ )";
	public static String MODIFY_PARAM_UPDATEPARAM18 = "update urm_object_param set entitycolumn = @4@ , name = @5@ , dbname = @6@ , xmlname = @7@ , xdesc = @8@ , paramvalue_type = @9@ , paramvalue_subtype = @10@ , object_type = @11@ , enumname = @12@ , required = @13@ , secured = @14@ , inherited = @15@ , expr_def = @16@ , customenum_def = @17@ , version = @18@ where param_object_id = @1@ and paramentity_type = @2@ and param_id = @3@";
	public static String MODIFY_PARAM_DROPENTITY2 = "delete from urm_object_entity where param_object_id = @1@ and paramentity_type = @2@";
	public static String MODIFY_PARAM_DROPENTITYPARAMS2 = "delete from urm_object_param where param_object_id = @1@ and paramentity_type = @2@";
	public static String MODIFY_PARAM_DROPPARAM1 = "delete from urm_object_param where param_id = @1@";
	public static String MODIFY_PARAM_DROPPARAMVALUES1 = "delete from urm_object_param_value where param_id = @1@";
	public static String MODIFY_PARAM_DECREMENTENTITYINDEX3 = "update urm_object_param set entitycolumn = entitycolumn - 1 where param_object_id = @1@ and paramentity_type = @2@ and entitycolumn > @3@";
	public static String MODIFY_PARAM_DROPENTITYVALUES2 = "delete from urm_object_param_value where param_object_id = @1@ and paramentity_type = @2@";
	public static String QUERY_PARAM_GETENTITYPARAMS2 = "select param_id , name , dbname , xmlname , xdesc , paramvalue_type , paramvalue_subtype , object_type , enumname , required , secured , inherited , expr_def , customenum_def , version from urm_object_param where param_object_id = @1@ and paramentity_type = @2@ order by entitycolumn";
	public static String QUERY_PARAM_GETOBJECTPARAMVALUES2 = "select paramentity_type , param_id , expr_value , version from urm_object_param_value where object_id = @1@ and paramrole_type = @2@";
	public static String QUERY_PARAM_GETPARAMROLEVALUES1 = "select object_id , param_object_id , paramentity_type , param_id , expr_value , version from urm_object_param_value where paramrole_type = @1@";
	public static String MODIFY_PARAM_DROPOBJECTPARAMVALUES2 = "delete from urm_object_param_value where object_id = @1@ and paramrole_type = @2@";
	public static String MODIFY_PARAM_DROPOBJECTENTITYPARAMVALUES3 = "delete from urm_object_param_value where object_id = @1@ and paramrole_type = @2@ and paramentity_type = @3@";
	public static String MODIFY_PARAM_DROPOBJECTPARAMVALUESAPP2 = "delete from urm_object_param_value where object_id = @1@ and paramrole_type = @2@ and ( param_object_id , paramentity_type ) in ( select param_object_id , paramentity_type from urm_object_entity where custom = 'no' )";
	public static String MODIFY_PARAM_DROPOBJECTPARAMVALUESCUSTOM2 = "delete from urm_object_param_value where object_id = @1@ and paramrole_type = @2@ and ( param_object_id , paramentity_type ) in ( select param_object_id , paramentity_type from urm_object_entity where custom = 'yes' )";
	public static String MODIFY_PARAM_ADDOBJECTPARAMVALUE7  = "insert into urm_object_param_value ( object_id , paramrole_type , param_object_id , paramentity_type , param_id , expr_value , version ) values ( @values@ )";
	public static String MODIFY_PARAM_DROPOBJECTPARAMVALUE5  = "delete from urm_object_param_value where object_id = @1@ and paramrole_type = @2@ and param_object_id = @3@ and paramentity_type = @4@ and param_id = @5@";
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
	public static String MODIFY_BASE_ADDDEPITEM3 = "insert into urm_base_item_deps ( baseitem_id , dep_baseitem_id , cv ) values ( @values@ )";
	public static String MODIFY_BASE_DELETEDEPITEM2 = "delete from urm_base_item_deps where baseitem_id = @1@ and dep_baseitem_id = @2@";
	public static String QUERY_BASE_ITEMDEPS0 = "select baseitem_id , dep_baseitem_id , cv from urm_base_item_deps";

	public static String MODIFY_APP_DROP_SYSTEMPARAMVALUES0 = "delete from urm_object_param_value where param_object_id in ( select system_id from urm_system )";
	public static String MODIFY_APP_DROP_SYSTEMPARAMS0 = "delete from urm_object_param where param_object_id in ( select system_id from urm_system )";
	public static String MODIFY_APP_DROP_SYSTEMENTITIES0 = "delete from urm_object_entity where param_object_id in ( select system_id from urm_system )";
	public static String MODIFY_APP_UNMATCHPRODUCTS0 = "update urm_product_meta set product_fkid = null , matched = 'no' where product_fkid is not null";

	public static String MODIFY_CORE_DROP_ENTITY1 = "delete from urm_object_entity where param_object_id = @1@";
	public static String MODIFY_CORE_UNMATCHPROJECTBUILDERS0 = "update urm_source_project set builder_fkid = null , builder_fkname = builder.name from ( select builder_id , name from urm_project_builder ) as builder where builder_fkid is not null and urm_source_project.builder_fkid = builder.builder_id";
	public static String MODIFY_CORE_UNMATCHPROJECTMIRRORS0 = "update urm_source_project set mirror_fkid = null , mirror_fkresource = mirror.resource_name , mirror_fkrepository = mirror.resource_repo , mirror_fkrepopath = mirror.resource_root , mirror_fkcodepath = mirror.resource_data from ( select mirror_id , urm_resource.name as resource_name , resource_repo , resource_root , resource_data from urm_mirror , urm_resource where urm_mirror.resource_id = urm_resource.resource_id ) as mirror where mirror_fkid is not null and urm_source_project.mirror_fkid = mirror.mirror_id";
	public static String MODIFY_CORE_UNMATCHDATACENTERS0 = "update urm_env_segment set datacenter_fkid = null , datacenter_fkname = datacenter.name from ( select datacenter_id , name from urm_datacenter ) as datacenter where datacenter_fkid is not null and urm_env_segment.datacenter_fkid = datacenter.datacenter_id";
	public static String MODIFY_CORE_UNMATCHACCOUNTS0 = "update urm_env_node set account_fkid = null , account_fkname = account.name from ( select account_id , urm_account.name || '@' || urm_host.name as name from urm_host , urm_account where urm_host.host_id = urm_account.host_id ) as account where account_fkid is not null and urm_env_node.account_fkid = account.account_id";
	
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

	public static String MODIFY_META_SETSTATUS2 = "update urm_product_meta set matched = @2@ where meta_id = @1@";
	
	public static String MODIFY_SOURCE_SHIFTPOS_ONDELETEPROJECT3 = "update urm_source_project set project_pos = project_pos - 1 where meta_id = @1@ and srcset_id = @2@ and project_pos > @3@";
	public static String MODIFY_SOURCE_SHIFTPOS_ONDELETESET2 = "update urm_source_set set set_pos = set_pos - 1 where meta_id = @1@ and set_pos > @2@";
	public static String MODIFY_SOURCE_SHIFTPOS_ONINSERTPROJECT3 = "update urm_source_project set project_pos = project_pos + 1 where meta_id = @1@ and srcset_id = @2@ and project_pos >= @3@";
	public static String MODIFY_SOURCE_SHIFTPOS_ONINSERTSET2 = "update urm_source_set set set_pos = set_pos + 1 where meta_id = @1@ and set_pos >= @2@";
	public static String MODIFY_SOURCE_CHANGEPROJECTSET2 = "update urm_source_project set srcset_id = @2@ where meta_id = @1@";
	public static String MODIFY_SOURCE_CHANGEPROJECTORDER2 = "update urm_source_project set project_pos = @2@ where meta_id = @1@";
	public static String MODIFY_SOURCE_CHANGESETORDER2 = "update urm_source_set set set_pos = @2@ where meta_id = @1@";
	public static String MODIFY_SOURCE_DELETEPROJECTITEMS1 = "delete from urm_source_item where project_id = @1@";
	
	public static String MODIFY_DISTR_CASCADEBINARY_COMPITEM1 = "delete from urm_dist_compitem where binary_id = @1@";
	public static String MODIFY_DISTR_CASCADECONF_COMPITEM1 = "delete from urm_dist_compitem where confitem_id = @1@";
	public static String MODIFY_DISTR_CASCADECOMP_ALLITEMS1 = "delete from urm_dist_compitem where comp_id = @1@";

	public static String MODIFY_ENVALL_DELETEALL_PARAMVALUES1 = "delete from urm_object_param_value where object_id in ( " +
			"select node_id from urm_env_node a , urm_env b where a.env_id = b.env_id and b.meta_fkid = @1@ union all " +
			"select server_id from urm_env_server a , urm_env b where a.env_id = b.env_id and b.meta_fkid = @1@ union all " +
			"select segment_id from urm_env_segment a , urm_env b where a.env_id = b.env_id and b.meta_fkid = @1@ union all " + 
			"select env_id from urm_env where meta_fkid = @1@ )";
	public static String MODIFY_ENV_DELETEALL_PARAMVALUES1 = "delete from urm_object_param_value where object_id in ( " +
			"select node_id from urm_env_node where env_id = @1@ union all " +
			"select server_id from urm_env_server where env_id = @1@ union all " +
			"select segment_id from urm_env_segment where env_id = @1@ union all " + 
			"select env_id from urm_env where env_id = @1@ )";

	public static String MODIFY_ENV_SETSTATUS2 = "update urm_env set matched = @2@ where env_id = @1@";
	public static String MODIFY_ENV_MATCHBASELINE2 = "update urm_env set baseline_env_fkid = @2@ , baseline_env_fkname = null where env_id = @1@";
	public static String MODIFY_ENVSG_MATCHBASELINE2 = "update urm_env_segment set baseline_segment_fkid = @2@ , baseline_segment_fkname = null where segment_id = @1@";
	public static String MODIFY_ENVSERVER_MATCHBASELINE2 = "update urm_env_server set baseline_server_fkid = @2@ , baseline_server_fkname = null where server_id = @1@";

	public static String MODIFY_ENV_CASCADESEGMENT_ALLSTARTGROUPITEMS1 = "delete from urm_env_startgroup_server where startgroup_id in ( select startgroup_id from urm_env_startgroup where segment_id = @1@ )";
	public static String MODIFY_ENV_CASCADESEGMENT_ALLSTARTGROUPS1 = "delete from urm_env_startgroup where segment_id = @1@";
	public static String MODIFY_ENV_CASCADESERVER_ALLDEPLOYMENTS1 = "delete from urm_env_deployment where server_id = @1@";

	public static String MODIFY_REL_REPO_MATCHMETA3 = "update urm_rel_repository set meta_fkid = @1@ , meta_fkname = null , meta_fkrevision = null where meta_fkid is null and meta_fkname = @2@ and meta_fkrevision = @3@";
	public static String MODIFY_REL_REPO_UNMATCHRELEASES1 = "update urm_rel_repository set meta_fkid = null , meta_fkname = meta.name , meta_fkrevision = meta.revision from ( select meta_id , name , revision from urm_product_meta ) as meta where meta_fkid is not null and meta_fkid is not null and meta_fkid = meta.meta_id and meta_fkid = @1@";
	
}
