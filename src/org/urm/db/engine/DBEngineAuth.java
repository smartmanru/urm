package org.urm.db.engine;

import java.sql.ResultSet;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AuthGroup;
import org.urm.meta.engine.AuthLdap;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.AuthRoleSet;
import org.urm.meta.engine.AuthUser;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineAuth.SourceType;
import org.urm.meta.engine.EngineAuth.SpecialRights;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.engine.Network;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBEngineAuth {

	public static String ELEMENT_LDAP = "ldap";
	public static String ELEMENT_LOCALUSERS = "localusers";
	public static String ELEMENT_LOCALUSER = "user";
	public static String ELEMENT_GROUP_LOCALUSER = "localuser";
	public static String ELEMENT_GROUP_LDAPUSER = "ldapuser";
	public static String ELEMENT_GROUPS = "groups";
	public static String ELEMENT_GROUP = "group";
	public static String ELEMENT_PERMISSIONS = "permissions";
	public static String ELEMENT_PERMISSIONS_RESOURCE = "resource";
	public static String ELEMENT_PERMISSIONS_PRODUCT = "product";
	public static String ELEMENT_PERMISSIONS_NETWORK = "network";
	public static String ELEMENT_PERMISSIONS_SPECIAL = "special";
	public static String TABLE_GROUP = "urm_auth_group";
	public static String TABLE_USER = "urm_auth_user";
	public static String FIELD_GROUP_ID = "group_id";
	public static String FIELD_GROUP_DESC = "xdesc";
	public static String FIELD_USER_ID = "user_id";
	public static String FIELD_USER_DESC = "xdesc";
	public static String XMLPROP_ROLEDEV = "devacc";
	public static String XMLPROP_ROLEREL = "relacc";
	public static String XMLPROP_ROLETEST = "testacc";
	public static String XMLPROP_ROLEOPR = "opracc";
	public static String XMLPROP_ROLEINFRA = "infacc";
	public static String XMLPROP_ANY_RESOURCES = "anyresource";
	public static String XMLPROP_ANY_PRODUCTS = "anyproduct";
	public static String XMLPROP_ANY_NETWORKS = "anynetwork";
	
	public static PropertyEntity upgradeEntityLDAPSettings( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppAttrsEntity( DBEnumObjectType.ROOT , DBEnumParamEntityType.LDAPSETTINGS , DBEnumObjectVersionType.LOCAL );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaBoolean( AuthLdap.PROPERTY_LDAPUSE , "Use LDAP Authentification" , true , false ) ,
				EntityVar.metaString( AuthLdap.PROPERTY_HOST , "LDAP Server Host" , false , null ) ,
				EntityVar.metaInteger( AuthLdap.PROPERTY_PORT , "LDAP Server Port" , true , 6000 ) ,
				EntityVar.metaObject( AuthLdap.PROPERTY_LOGIN_RESOURCE , "Login Resource" , DBEnumObjectType.RESOURCE , false ) ,
				EntityVar.metaString( AuthLdap.PROPERTY_USERDN , "Search Base" , false , null ) ,
				EntityVar.metaString( AuthLdap.PROPERTY_USERCLASS , "Object Class" , false , null ) ,
				EntityVar.metaString( AuthLdap.PROPERTY_USERFILTER , "User Filter" , false , null ) ,
				EntityVar.metaString( AuthLdap.PROPERTY_NAMEATTR , "User Identity" , false , null ) ,
				EntityVar.metaString( AuthLdap.PROPERTY_DISPLAYNAMEATTR , "Display Name" , false , null ) ,
				EntityVar.metaString( AuthLdap.PROPERTY_EMAILATTR , "User Email" , false , null ) ,
				EntityVar.metaString( AuthLdap.PROPERTY_PASSWORDATTR , "Password" , false , null )
		} ) );
	}

	public static PropertyEntity upgradeEntityAuthUser( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.AUTH_USER , DBEnumParamEntityType.AUTHUSER , DBEnumObjectVersionType.LOCAL , TABLE_USER , FIELD_USER_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( AuthUser.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( AuthUser.PROPERTY_DESC , FIELD_USER_DESC , AuthUser.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaString( AuthUser.PROPERTY_FULLNAME , "" , true , null ) ,
				EntityVar.metaString( AuthUser.PROPERTY_EMAIL , "" , true , null ) ,
				EntityVar.metaBoolean( AuthUser.PROPERTY_ADMIN , "" , true , false ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthUser.PROPERTY_LOCAL , "" , true , false )
		} ) );
	}

	public static PropertyEntity upgradeEntityAuthGroup( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.AUTH_GROUP , DBEnumParamEntityType.AUTHGROUP , DBEnumObjectVersionType.LOCAL , TABLE_GROUP , FIELD_GROUP_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( AuthGroup.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( AuthGroup.PROPERTY_DESC , FIELD_GROUP_DESC , AuthGroup.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthGroup.PROPERTY_ANY_RESOURCES , "" , true , false ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthGroup.PROPERTY_ANY_PRODUCTS , "" , true , false ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthGroup.PROPERTY_ANY_NETWORKS , "" , true , false ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthGroup.PROPERTY_ROLEDEV , "" , true , false ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthGroup.PROPERTY_ROLEREL , "" , true , false ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthGroup.PROPERTY_ROLETEST , "" , true , false ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthGroup.PROPERTY_ROLEOPR , "" , true , false ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthGroup.PROPERTY_ROLEINFRA , "" , true , false ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthGroup.PROPERTY_SPECIAL_ADMCORE , "" , true , false ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthGroup.PROPERTY_SPECIAL_BASEADM , "" , true , false ) ,
				EntityVar.metaBooleanDatabaseOnly( AuthGroup.PROPERTY_SPECIAL_BASEITEMS , "" , true , false )
		} ) );
	}

	public static PropertyEntity loaddbEntityLDAPSettings( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppAttrsEntity( DBEnumObjectType.ROOT , DBEnumParamEntityType.LDAPSETTINGS , DBEnumObjectVersionType.LOCAL );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity loaddbEntityAuthUser( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.AUTH_USER , DBEnumParamEntityType.AUTHUSER , DBEnumObjectVersionType.LOCAL , TABLE_USER , FIELD_USER_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity loaddbEntityAuthGroup( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.AUTH_GROUP , DBEnumParamEntityType.AUTHGROUP , DBEnumObjectVersionType.LOCAL , TABLE_GROUP , FIELD_GROUP_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static void importxml( EngineLoader loader , EngineAuth auth , Node root ) throws Exception {
		importxmlLDAPSettings( loader , auth , root );
		importxmlLocalUsers( loader , auth , root );
		importxmlGroups( loader , auth , root );
	}
	
	public static void importxmlLDAPSettings( EngineLoader loader , EngineAuth auth , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		EngineSettings settings = loader.getSettings();
		
		int version = c.getNextLocalVersion();
		ObjectProperties ops = entities.createLdapProps( settings.getEngineProperties() );
		Node ldap = ConfReader.xmlGetFirstChild( root , ELEMENT_LDAP );
		if( ldap != null )
			DBSettings.importxml( loader , ldap , ops , true , false , version );
		
		auth.setLdapSettings( ops );
	}

	public static void importxmlLocalUsers( EngineLoader loader , EngineAuth auth , Node root ) throws Exception {
		Node users = ConfReader.xmlGetFirstChild( root , ELEMENT_LOCALUSERS );
		if( users == null )
			return;
		
		Node[] list = ConfReader.xmlGetChildren( users , ELEMENT_LOCALUSER );
		if( list != null ) {
			for( Node node : list ) {
				AuthUser user = importxmlLocalUser( loader , auth , node );
				auth.addLocalUser( user );
			}
		}
	}
	
	public static void importxmlGroups( EngineLoader loader , EngineAuth auth , Node root ) throws Exception {
		Node groups = ConfReader.xmlGetFirstChild( root , ELEMENT_GROUPS );
		if( groups == null )
			return;
		
		Node[] list = ConfReader.xmlGetChildren( groups , ELEMENT_GROUP );
		if( list != null ) {
			for( Node node : list ) {
				AuthGroup group = importxmlGroup( loader , auth , node );
				auth.addGroup( group );
			}
		}
	}
	
	private static AuthUser importxmlLocalUser( EngineLoader loader , EngineAuth auth , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppAuthUser;
		
		AuthUser user = new AuthUser( auth );
		user.createUser(
				entity.importxmlStringAttr( root , AuthUser.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , AuthUser.PROPERTY_DESC ) ,
				entity.importxmlStringAttr( root , AuthUser.PROPERTY_FULLNAME ) ,
				entity.importxmlStringAttr( root , AuthUser.PROPERTY_EMAIL ) ,
				entity.importxmlBooleanAttr( root , AuthUser.PROPERTY_ADMIN , false ) ,
				true
				);
		
		modifyUser( c , user , true );
		
		return( user );
	}

	private static AuthGroup importxmlGroup( EngineLoader loader , EngineAuth auth , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppAuthGroup;
		
		AuthGroup group = new AuthGroup( auth );
		group.createGroup(
				entity.importxmlStringAttr( root , AuthGroup.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , AuthGroup.PROPERTY_DESC )
				);

		for( String userName : ConfReader.xmlGetNamedElements( root , ELEMENT_GROUP_LOCALUSER ) ) {
			AuthUser user = auth.findLocalUser( userName );
			group.addLocalUser( user.ID );
		}
		
		for( String userName : ConfReader.xmlGetNamedElements( root , ELEMENT_GROUP_LDAPUSER ) ) {
			AuthUser user = importxmlLdapUser( loader , auth , userName );
			group.addLdapUser( user.ID );
		}
		
		Node permissions = ConfReader.xmlGetFirstChild( root , ELEMENT_PERMISSIONS );
		if( permissions != null ) {
			String[] resourceNames = ConfReader.xmlGetNamedElements( permissions , ELEMENT_PERMISSIONS_RESOURCE );
			String[] productNames = ConfReader.xmlGetNamedElements( permissions , ELEMENT_PERMISSIONS_PRODUCT );
			String[] networkNames = ConfReader.xmlGetNamedElements( permissions , ELEMENT_PERMISSIONS_NETWORK );
			String[] specialNames = ConfReader.xmlGetNamedElements( permissions , ELEMENT_PERMISSIONS_SPECIAL );
			
			boolean anyResources = ConfReader.getBooleanAttrValue( permissions , XMLPROP_ANY_RESOURCES , false );
			boolean anyProducts = ConfReader.getBooleanAttrValue( permissions , XMLPROP_ANY_PRODUCTS , false );
			boolean anyNetworks = ConfReader.getBooleanAttrValue( permissions , XMLPROP_ANY_NETWORKS , false );
			
			AuthRoleSet roles = new AuthRoleSet();
			roles.secDev = ConfReader.getBooleanAttrValue( permissions , XMLPROP_ROLEDEV , false );
			roles.secRel = ConfReader.getBooleanAttrValue( permissions , XMLPROP_ROLEREL , false );
			roles.secTest = ConfReader.getBooleanAttrValue( permissions , XMLPROP_ROLETEST , false );
			roles.secOpr = ConfReader.getBooleanAttrValue( permissions , XMLPROP_ROLEOPR , false );
			roles.secInfra = ConfReader.getBooleanAttrValue( permissions , XMLPROP_ROLEINFRA , false );
			
			// resolve
			Integer[] resourcesList = resolveResources( loader.getResources() , resourceNames );
			Integer[] productList = resolveProducts( loader.getDirectory() , productNames );
			Integer[] networkList = resolveNetworks( loader.getInfrastructure() , networkNames );
			SpecialRights[] specialList = resolveSpecialRights( specialNames );
			
			group.setGroupPermissions( roles , anyResources , resourcesList , anyProducts , productList , anyNetworks , networkList , false , specialList );
		}
		
		modifyGroup( c , group , true );

		for( Integer resource : group.getPermissionResources() )
			modifyGroupResources( c , group , resource , true );
		for( Integer product : group.getPermissionProducts() )
			modifyGroupProducts( c , group , product , true );
		for( Integer network : group.getPermissionNetworks() )
			modifyGroupNetworks( c , group , network , true );
		
		for( String name : group.getUsers( null ) ) {
			AuthUser user = auth.getUser( name ); 
			modifyGroupUser( c , group , user , true );
		}
		
		return( group );
	}

	private static AuthUser importxmlLdapUser( EngineLoader loader , EngineAuth auth , String userName ) throws Exception {
		AuthUser user = auth.findLdapUser( userName );
		if( user != null )
			return( user );
		
		DBConnection c = loader.getConnection();
		return( createLdapUser( c , auth , userName ) );
	}
	
	private static void modifyUser( DBConnection c , AuthUser user , boolean insert ) throws Exception {
		if( insert )
			user.ID = DBNames.getNameIndex( c , DBVersions.LOCAL_ID , user.NAME , DBEnumParamEntityType.AUTHUSER );
		else
			DBNames.updateName( c , DBVersions.LOCAL_ID , user.NAME , user.ID , DBEnumParamEntityType.AUTHUSER );
		
		user.UV = c.getNextLocalVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppAuthUser , user.ID , user.UV , new String[] {
				EngineDB.getString( user.NAME ) , 
				EngineDB.getString( user.DESC ) ,
				EngineDB.getString( user.FULLNAME ) ,
				EngineDB.getString( user.EMAIL ) ,
				EngineDB.getBoolean( user.ADMIN ) ,
				EngineDB.getBoolean( user.LOCAL )
				} , insert );
	}

	private static void modifyGroup( DBConnection c , AuthGroup group , boolean insert ) throws Exception {
		if( insert )
			group.ID = DBNames.getNameIndex( c , DBVersions.LOCAL_ID , group.NAME , DBEnumParamEntityType.AUTHGROUP );
		else
			DBNames.updateName( c , DBVersions.LOCAL_ID , group.NAME , group.ID , DBEnumParamEntityType.AUTHGROUP );
		
		group.UV = c.getNextLocalVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppAuthGroup , group.ID , group.UV , new String[] {
				EngineDB.getString( group.NAME ) , 
				EngineDB.getString( group.DESC ) ,
				EngineDB.getBoolean( group.anyResources ) ,
				EngineDB.getBoolean( group.anyProducts ) ,
				EngineDB.getBoolean( group.anyNetworks ) ,
				EngineDB.getBoolean( group.roles.secDev ) ,
				EngineDB.getBoolean( group.roles.secRel ) ,
				EngineDB.getBoolean( group.roles.secTest ) ,
				EngineDB.getBoolean( group.roles.secOpr ) ,
				EngineDB.getBoolean( group.roles.secInfra ) ,
				EngineDB.getBoolean( group.checkSpecialPermission( SpecialRights.SPECIAL_ADMCORE ) ) ,
				EngineDB.getBoolean( group.checkSpecialPermission( SpecialRights.SPECIAL_BASEADM ) ) ,
				EngineDB.getBoolean( group.checkSpecialPermission( SpecialRights.SPECIAL_BASEITEMS ) )
				} , insert );
	}

	private static void modifyGroupUser( DBConnection c , AuthGroup group , AuthUser user , boolean insert ) throws Exception {
		if( insert ) {
			if( !c.modify( DBQueries.MODIFY_AUTH_GROUPUSER_ADD3 , new String[] {
					EngineDB.getInteger( group.ID ) ,
					EngineDB.getInteger( user.ID ) ,
					EngineDB.getInteger( c.getNextLocalVersion() )
					}))
					Common.exitUnexpected();
		}
		else {
			if( !c.modify( DBQueries.MODIFY_AUTH_GROUPUSER_DROP2 , new String[] {
					EngineDB.getInteger( group.ID ) ,
					EngineDB.getInteger( user.ID )
					}))
					Common.exitUnexpected();
		}
	}
	
	private static void modifyGroupResources( DBConnection c , AuthGroup group , Integer resource , boolean insert ) throws Exception {
		if( insert ) {
			if( !c.modify( DBQueries.MODIFY_AUTH_GROUPACCESS_ADDRESOURCE3 , new String[] {
					EngineDB.getInteger( group.ID ) ,
					EngineDB.getInteger( resource ) ,
					EngineDB.getInteger( c.getNextLocalVersion() )
					}))
				Common.exitUnexpected();
		}
		else {
			if( !c.modify( DBQueries.MODIFY_AUTH_GROUPACCESS_DROPRESOURCES1 , new String[] {
					EngineDB.getInteger( group.ID )
					}))
				Common.exitUnexpected();
		}
	}
	
	private static void modifyGroupProducts( DBConnection c , AuthGroup group , Integer product , boolean insert ) throws Exception {
		if( insert ) {
			if( !c.modify( DBQueries.MODIFY_AUTH_GROUPACCESS_ADDPRODUCT3 , new String[] {
					EngineDB.getInteger( group.ID ) ,
					EngineDB.getInteger( product ) ,
					EngineDB.getInteger( c.getNextLocalVersion() )
					}))
				Common.exitUnexpected();
		}
		else {
			if( !c.modify( DBQueries.MODIFY_AUTH_GROUPACCESS_DROPPRODUCTS1 , new String[] {
					EngineDB.getInteger( group.ID )
					}))
				Common.exitUnexpected();
		}
	}
	
	private static void modifyGroupNetworks( DBConnection c , AuthGroup group , Integer network , boolean insert ) throws Exception {
		if( insert ) {
			if( !c.modify( DBQueries.MODIFY_AUTH_GROUPACCESS_ADDNETWORK3 , new String[] {
					EngineDB.getInteger( group.ID ) ,
					EngineDB.getInteger( network ) ,
					EngineDB.getInteger( c.getNextLocalVersion() )
					}))
				Common.exitUnexpected();
		}
		else {
			if( !c.modify( DBQueries.MODIFY_AUTH_GROUPACCESS_DROPNETWORKS1 , new String[] {
					EngineDB.getInteger( group.ID )
					}))
				Common.exitUnexpected();
		}
	}
	
	public static void loaddb( EngineLoader loader , EngineAuth auth ) throws Exception {
		EngineEntities entities = loader.getEntities();
		EngineSettings settings = loader.getSettings();
		
		ObjectProperties ops = entities.createLdapProps( settings.getEngineProperties() );
		DBSettings.loaddbValues( loader , ops );
		auth.setLdapSettings( ops );
		
		loaddbUsers( loader , auth );
		loaddbGroups( loader , auth );
	}

	public static void loaddbUsers( EngineLoader loader , EngineAuth auth ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppAuthUser;
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				AuthUser user = new AuthUser( auth );
				user.ID = entity.loaddbId( rs );
				user.UV = entity.loaddbVersion( rs );
				user.createUser( 
						entity.loaddbString( rs , AuthUser.PROPERTY_NAME ) , 
						entity.loaddbString( rs , AuthUser.PROPERTY_DESC ) ,
						entity.loaddbString( rs , AuthUser.PROPERTY_FULLNAME ) ,
						entity.loaddbString( rs , AuthUser.PROPERTY_EMAIL ) ,
						entity.loaddbBoolean( rs , AuthUser.PROPERTY_ADMIN ) ,
						entity.loaddbBoolean( rs , AuthUser.PROPERTY_LOCAL )
						);
				if( user.LOCAL )
					auth.addLocalUser( user );
				else
					auth.addLdapUser( user );
			}
		}
		finally {
			c.closeQuery();
		}
	}

	public static void loaddbGroups( EngineLoader loader , EngineAuth auth ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppAuthGroup;
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				AuthGroup group = new AuthGroup( auth );
				group.ID = entity.loaddbId( rs );
				group.UV = entity.loaddbVersion( rs );
				group.createGroup( 
						entity.loaddbString( rs , AuthGroup.PROPERTY_NAME ) , 
						entity.loaddbString( rs , AuthGroup.PROPERTY_DESC )
						);
				AuthRoleSet roles = new AuthRoleSet();
				roles.secDev = entity.loaddbBoolean( rs , AuthGroup.PROPERTY_ROLEDEV );
				roles.secRel = entity.loaddbBoolean( rs , AuthGroup.PROPERTY_ROLEREL );
				roles.secTest = entity.loaddbBoolean( rs , AuthGroup.PROPERTY_ROLETEST );
				roles.secOpr = entity.loaddbBoolean( rs , AuthGroup.PROPERTY_ROLEOPR );
				roles.secInfra = entity.loaddbBoolean( rs , AuthGroup.PROPERTY_ROLEINFRA );
				
				SpecialRights[] specials = loaddbSpecials(
						new SpecialRights[] {
								SpecialRights.SPECIAL_ADMCORE ,
								SpecialRights.SPECIAL_BASEADM ,
								SpecialRights.SPECIAL_BASEITEMS
						} ,
						new boolean[] {
								entity.loaddbBoolean( rs , AuthGroup.PROPERTY_SPECIAL_ADMCORE ) ,
								entity.loaddbBoolean( rs , AuthGroup.PROPERTY_SPECIAL_BASEADM ) ,
								entity.loaddbBoolean( rs , AuthGroup.PROPERTY_SPECIAL_BASEITEMS )
						}
						);
				
				group.setGroupPermissions( 
						roles , 
						entity.loaddbBoolean( rs , AuthGroup.PROPERTY_ANY_RESOURCES ) , 
						null ,
						entity.loaddbBoolean( rs , AuthGroup.PROPERTY_ANY_PRODUCTS ) ,
						null ,
						entity.loaddbBoolean( rs , AuthGroup.PROPERTY_ANY_NETWORKS ) ,
						null ,
						false ,
						specials );
				auth.addGroup( group );
			}
		}
		finally {
			c.closeQuery();
		}
		
		loaddbGroupsAccessResources( loader , auth );
		loaddbGroupsAccessProducts( loader , auth );
		loaddbGroupsAccessNetworks( loader , auth );
		loaddbGroupsUsers( loader , auth );
	}	

	private static void loaddbGroupsAccessResources( EngineLoader loader , EngineAuth auth ) throws Exception {
		DBConnection c = loader.getConnection();
		
		ResultSet rs = c.query( DBQueries.QUERY_AUTH_GROUPACCESS_RESOURCES0 );
		try {
			while( rs.next() ) {
				int groupId = rs.getInt( 1 );
				int resourceId = rs.getInt( 2 );
				AuthGroup group = auth.getGroup( groupId );
				group.addResource( resourceId );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	private static void loaddbGroupsAccessProducts( EngineLoader loader , EngineAuth auth ) throws Exception {
		DBConnection c = loader.getConnection();
		
		ResultSet rs = c.query( DBQueries.QUERY_AUTH_GROUPACCESS_PRODUCTS0 );
		try {
			while( rs.next() ) {
				int groupId = rs.getInt( 1 );
				int productId = rs.getInt( 2 );
				AuthGroup group = auth.getGroup( groupId );
				group.addProduct( productId );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	private static void loaddbGroupsAccessNetworks( EngineLoader loader , EngineAuth auth ) throws Exception {
		DBConnection c = loader.getConnection();
		
		ResultSet rs = c.query( DBQueries.QUERY_AUTH_GROUPACCESS_NETWORKS0 );
		try {
			while( rs.next() ) {
				int groupId = rs.getInt( 1 );
				int networkId = rs.getInt( 2 );
				AuthGroup group = auth.getGroup( groupId );
				group.addNetwork( networkId );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	private static SpecialRights[] loaddbSpecials( SpecialRights[] vars , boolean[] values ) {
		int n = 0;
		for( int k = 0; k < values.length; k++ ) {
			if( values[ k ] == true )
				n++;
		}
		
		SpecialRights[] sr = new SpecialRights[ n ];
		n = 0;
		for( int k = 0; k < values.length; k++ ) {
			if( values[ k ] == true ) {
				sr[ n ] = vars[ n ];
				n++;
			}
		}
		return( sr );
	}
	
	private static void loaddbGroupsUsers( EngineLoader loader , EngineAuth auth ) throws Exception {
		DBConnection c = loader.getConnection();
		
		ResultSet rs = c.query( DBQueries.QUERY_AUTH_GROUPUSERS0 );
		try {
			while( rs.next() ) {
				int groupId = rs.getInt( 1 );
				int userId = rs.getInt( 2 );
				AuthGroup group = auth.getGroup( groupId );
				AuthUser user = auth.getUser( userId );
				if( user.LOCAL )
					group.addLocalUser( userId );
				else
					group.addLdapUser( userId );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static void exportxml( EngineLoader loader , EngineAuth auth , Document doc , Element root ) throws Exception {
		ObjectProperties ops = auth.getLdapSettings();
		Element node = Common.xmlCreateElement( doc , root , ELEMENT_LDAP );
		DBSettings.exportxml( loader , doc , node , ops , false );
		
		node = Common.xmlCreateElement( doc , root , ELEMENT_LOCALUSERS );
		exportxmlLocalUsers( loader , auth , doc , node );

		node = Common.xmlCreateElement( doc , root , ELEMENT_GROUPS );
		exportxmlGroups( loader , auth , doc , node );
	}

	public static void exportxmlLocalUsers( EngineLoader loader , EngineAuth auth , Document doc , Element root ) throws Exception {
		for( String name : auth.getLocalUserNames() ) {
			AuthUser user = auth.findLocalUser( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_LOCALUSER );
			exportxmlLocalUser( loader , auth , user , doc , node );
		}
	}

	public static void exportxmlLocalUser( EngineLoader loader , EngineAuth auth , AuthUser user , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppAuthUser;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( user.NAME ) , 
				entity.exportxmlString( user.DESC ) ,
				entity.exportxmlString( user.FULLNAME ) ,
				entity.exportxmlString( user.EMAIL ) ,
				entity.exportxmlBoolean( user.ADMIN )
		} , true );
	}

	public static void exportxmlGroups( EngineLoader loader , EngineAuth auth , Document doc , Element root ) throws Exception {
		for( String name : auth.getGroupNames() ) {
			AuthGroup group = auth.findGroup( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_GROUP );
			exportxmlGroup( loader , auth , group , doc , node );
		}
	}

	public static void exportxmlGroup( EngineLoader loader , EngineAuth auth , AuthGroup group , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppAuthGroup;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( group.NAME ) , 
				entity.exportxmlString( group.DESC )
		} , true );
		
		Element node = Common.xmlCreateElement( doc , root , ELEMENT_PERMISSIONS );
		exportxmlGroupPermissions( loader , auth , group , doc , node );
		
		for( String name : group.getUsers( SourceType.SOURCE_LOCAL ) ) {
			node = Common.xmlCreateElement( doc , root , ELEMENT_GROUP_LOCALUSER );
			Common.xmlSetNameAttr( doc , node , name );
		}
		
		for( String name : group.getUsers( SourceType.SOURCE_LDAP ) ) {
			node = Common.xmlCreateElement( doc , root , ELEMENT_GROUP_LDAPUSER );
			Common.xmlSetNameAttr( doc , node , name );
		}
	}

	public static void exportxmlGroupPermissions( EngineLoader loader , EngineAuth auth , AuthGroup group , Document doc , Element root ) throws Exception {
		Common.xmlSetElementBooleanAttr( doc , root , XMLPROP_ANY_RESOURCES , group.anyResources );
		Common.xmlSetElementBooleanAttr( doc , root , XMLPROP_ANY_NETWORKS , group.anyNetworks );
		Common.xmlSetElementBooleanAttr( doc , root , XMLPROP_ANY_PRODUCTS , group.anyProducts );
		Common.xmlSetElementBooleanAttr( doc , root , XMLPROP_ROLEDEV , group.roles.secDev );
		Common.xmlSetElementBooleanAttr( doc , root , XMLPROP_ROLEREL , group.roles.secRel );
		Common.xmlSetElementBooleanAttr( doc , root , XMLPROP_ROLETEST , group.roles.secTest );
		Common.xmlSetElementBooleanAttr( doc , root , XMLPROP_ROLEOPR , group.roles.secOpr );
		Common.xmlSetElementBooleanAttr( doc , root , XMLPROP_ROLEINFRA , group.roles.secInfra );
		
		EngineResources resources = loader.getResources();
		for( Integer item : group.getPermissionResources() ) {
			AuthResource resource = resources.getResource( item );
			Common.xmlCreateNamedElement( doc , root , ELEMENT_PERMISSIONS_RESOURCE , resource.NAME );
		}
		
		EngineDirectory directory = loader.getDirectory();
		for( Integer item : group.getPermissionProducts() ) {
			AppProduct product = directory.getProduct( item );
			Common.xmlCreateNamedElement( doc , root , ELEMENT_PERMISSIONS_PRODUCT , product.NAME );
		}
		
		EngineInfrastructure infra = loader.getInfrastructure();
		for( Integer item : group.getPermissionNetworks() ) {
			Network network = infra.getNetwork( item );
			Common.xmlCreateNamedElement( doc , root , ELEMENT_PERMISSIONS_NETWORK , network.NAME );
		}
		
		for( SpecialRights r : group.getPermissionSpecial() )
			Common.xmlCreateNamedElement( doc , root , ELEMENT_PERMISSIONS_SPECIAL , Common.getEnumLower( r ) );
	}

	private static Integer[] resolveResources( EngineResources resources , String[] names ) throws Exception {
		if( names == null )
			return( new Integer[0] );
		
		Integer[] resourcesList = new Integer[ names.length ];
		for( int k = 0; k < names.length; k++ ) {
			AuthResource rc = resources.getResource( names[ k ] );
			resourcesList[ k ] = rc.ID;
		}
		return( resourcesList );
	}
	
	private static Integer[] resolveProducts( EngineDirectory directory , String[] names ) throws Exception {
		if( names == null )
			return( new Integer[0] );
		
		Integer[] productList = new Integer[ names.length ];
		for( int k = 0; k < names.length; k++ ) {
			AppProduct product = directory.getProduct( names[ k ] );
			productList[ k ] = product.ID;
		}
		return( productList );
	}
	
	private static Integer[] resolveNetworks( EngineInfrastructure infra , String[] names ) throws Exception {
		if( names == null )
			return( new Integer[0] );
		
		Integer[] networkList = new Integer[ names.length ];
		for( int k = 0; k < names.length; k++ ) {
			Network network = infra.getNetwork( names[ k ] );
			networkList[ k ] = network.ID;
		}
		return( networkList );
	}
	
	private static SpecialRights[] resolveSpecialRights( String[] names ) throws Exception {
		if( names == null )
			return( new SpecialRights[0] );
		
		SpecialRights[] specialList = new SpecialRights[ names.length ];
		for( int k = 0; k < names.length; k++ )
			specialList[ k ] = SpecialRights.valueOf( Common.xmlToEnumValue( names[ k ] ) );
		return( specialList );
	}
	
	public static void deleteDatacenterAccess( EngineTransaction transaction , EngineAuth auth , Datacenter datacenter ) throws Exception {
		DBConnection c = transaction.getConnection();
		if( !c.modify( DBQueries.MODIFY_AUTH_DROP_DATACENTERACCESS1 , new String[] {
			EngineDB.getInteger( datacenter.ID )
			}))
			Common.exitUnexpected();
		
		for( AuthGroup group : auth.getGroups() )
			deleteDatacenterAccess( transaction , auth , datacenter , group );
	}

	private static void deleteDatacenterAccess( EngineTransaction transaction , EngineAuth auth , Datacenter datacenter , AuthGroup group ) throws Exception {
		for( Integer networkId : group.getPermissionNetworks() ) {
			if( datacenter.findNetwork( networkId ) != null )
				group.removeNetwork( networkId );
		}
	}
	
	public static void deleteNetworkAccess( EngineTransaction transaction , EngineAuth auth , Network network ) throws Exception {
		DBConnection c = transaction.getConnection();
		if( !c.modify( DBQueries.MODIFY_AUTH_DROP_NETWORKACCESS1 , new String[] {
			EngineDB.getInteger( network.ID )
			}))
			Common.exitUnexpected();
		
		for( AuthGroup group : auth.getGroups() ) {
			for( Integer networkId : group.getPermissionNetworks() ) {
				if( networkId == network.ID )
					group.removeNetwork( networkId );
			}			
		}
	}

	public static void deleteProductAccess( DBConnection c , EngineAuth auth , AppProduct product ) throws Exception {
		if( !c.modify( DBQueries.MODIFY_AUTH_DROP_PRODUCTACCESS1 , new String[] {
			EngineDB.getInteger( product.ID )
			}))
			Common.exitUnexpected();
		
		for( AuthGroup group : auth.getGroups() ) {
			for( Integer productId : group.getPermissionProducts() ) {
				if( productId == product.ID )
					group.removeProduct( productId );
			}			
		}
	}

	public static void deleteResourceAccess( EngineTransaction transaction , EngineAuth auth , AuthResource resource ) throws Exception {
		DBConnection c = transaction.getConnection();
		if( !c.modify( DBQueries.MODIFY_AUTH_DROP_RESOURCEACCESS1 , new String[] {
			EngineDB.getInteger( resource.ID )
			}))
			Common.exitUnexpected();
		
		for( AuthGroup group : auth.getGroups() ) {
			for( Integer resourceId : group.getPermissionResources() ) {
				if( resourceId == resource.ID )
					group.removeResource( resourceId );
			}			
		}
	}

	public static void disableLdap( EngineTransaction transaction , EngineAuth auth ) throws Exception {
		DBConnection c = transaction.getConnection();
		AuthLdap ldap = auth.getAuthLdap();
		ldap.setNotUse();
		
		int version = c.getNextLocalVersion();
		DBSettings.savedbPropertyValues( transaction , ldap.getLdapSettings() , true , false , version );
	}
	
	public static void enableLdap( EngineTransaction transaction , EngineAuth auth , ObjectProperties ops ) throws Exception {
		DBConnection c = transaction.getConnection();
		AuthLdap ldap = auth.getAuthLdap();
		ldap.setLdapSettings( ops );
		
		int version = c.getNextLocalVersion();
		DBSettings.savedbPropertyValues( transaction , ldap.getLdapSettings() , true , false , version );
		
		ldap.start();
	}
	
	public static AuthGroup createGroup( EngineTransaction transaction , EngineAuth auth , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		AuthGroup group = new AuthGroup( auth );
		group.createGroup( name , desc );
		modifyGroup( c , group , true );
		auth.addGroup( group );
		return( group );
	}

	public static void modifyGroup( EngineTransaction transaction , EngineAuth auth , AuthGroup group , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		group.modifyGroup( name , desc );
		modifyGroup( c , group , false );
		
		auth.updateGroup( group );
	}

	public static void deleteGroup( EngineTransaction transaction , EngineAuth auth , AuthGroup group ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		if( !c.modify( DBQueries.MODIFY_AUTH_DROPGROUP_RESOURCEACCESS1 , new String[] {
				EngineDB.getInteger( group.ID )
				}))
				Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_AUTH_DROPGROUP_PRODUCTACCESS1 , new String[] {
				EngineDB.getInteger( group.ID )
				}))
				Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_AUTH_DROPGROUP_NETWORKACCESS1 , new String[] {
				EngineDB.getInteger( group.ID )
				}))
				Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_AUTH_DROPGROUP_USERS1 , new String[] {
				EngineDB.getInteger( group.ID )
				}))
				Common.exitUnexpected();
		DBEngineEntities.deleteAppObject( c , entities.entityAppAuthGroup , group.ID , c.getNextLocalVersion() );
		auth.removeGroup( group );

		// change effective user permissions 
		Engine engine = transaction.engine;
		ActionBase action = transaction.getAction();
		for( String user : group.getUsers( null ) )
			engine.updatePermissions( action , user );
	}

	private static AuthUser createLdapUser( DBConnection c , EngineAuth auth , String name ) throws Exception {
		AuthUser user = new AuthUser( auth );
		user.createUser(
				name ,
				null ,
				null ,
				null ,
				false ,
				false
				);
		
		modifyUser( c , user , true );
		auth.addLdapUser( user );
	
		return( user );
	}
	
	public static AuthUser createLocalUser( EngineTransaction transaction , EngineAuth auth , String name , String desc , String full , String email , boolean admin ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		AuthUser user = new AuthUser( auth );
		user.createUser( name , desc , full , email , admin , true );
		modifyUser( c , user , true );
		auth.addLocalUser( user );
		return( user );
	}
	
	public static void modifyLocalUser( EngineTransaction transaction , EngineAuth auth , AuthUser user , String name , String desc , String full , String email , boolean admin ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		user.modifyUser( name , desc , full , email , admin , true );
		modifyUser( c , user , false );
		
		auth.updateUser( user );
	}
	
	public static void deleteLocalUser( EngineTransaction transaction , EngineAuth auth , AuthUser user ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		if( !c.modify( DBQueries.MODIFY_AUTH_DROPUSER_GROUPS1 , new String[] {
				EngineDB.getInteger( user.ID )
				}))
				Common.exitUnexpected();
		DBEngineEntities.deleteAppObject( c , entities.entityAppAuthUser , user.ID , c.getNextLocalVersion() );
		auth.removeLocalUser( user );
		
		Engine engine = transaction.engine;
		ActionBase action = transaction.getAction();
		engine.updatePermissions( action , user.NAME );
	}

	public static void addGroupLocalUsers( EngineTransaction transaction , EngineAuth auth , AuthGroup group , String[] users ) throws Exception {
		DBConnection c = transaction.getConnection();
		Engine engine = transaction.engine;
		ActionBase action = transaction.getAction();
		
		for( String name : users ) {
			AuthUser user = auth.getLocalUser( name );
			modifyGroupUser( c , group , user , true );
			group.addLocalUser( user.ID );
			engine.updatePermissions( action , user.NAME );
		}
	}
	
	public static void addGroupLdapUsers( EngineTransaction transaction , EngineAuth auth , AuthGroup group , String[] users ) throws Exception {
		DBConnection c = transaction.getConnection();
		Engine engine = transaction.engine;
		ActionBase action = transaction.getAction();
		
		for( String name : users ) {
			AuthUser user = auth.findLdapUser( name );
			if( user == null )
				user = createLdapUser( c , auth , name );
				
			modifyGroupUser( c , group , user , true );
			group.addLdapUser( user.ID );
			engine.updatePermissions( action , user.NAME );
		}
	}
	
	public static void deleteGroupUsers( EngineTransaction transaction , EngineAuth auth , AuthGroup group , String[] users ) throws Exception {
		DBConnection c = transaction.getConnection();
		Engine engine = transaction.engine;
		ActionBase action = transaction.getAction();
		
		for( String name : users ) {
			AuthUser user = auth.getUser( name );
			if( group.hasUser( user ) ) {
				modifyGroupUser( c , group , user , false );
				group.removeUser( user );
				engine.updatePermissions( action , name );
			}
		}
	}

	public static void setGroupPermissions( EngineTransaction transaction , EngineAuth auth , AuthGroup group , AuthRoleSet roles , boolean allResources , String[] resources , boolean allProd , String[] products , boolean allNet , String[] networks , boolean allSpecial , SpecialRights[] specials ) throws Exception {
		DBConnection c = transaction.getConnection();
		Engine engine = transaction.engine;
		ActionBase action = transaction.getAction();
		
		Integer[] resourcesList = resolveResources( transaction.getResources() , resources );
		Integer[] productList = resolveProducts( transaction.getDirectory() , products );
		Integer[] networkList = resolveNetworks( transaction.getInfrastructure() , networks );
		
		group.setGroupPermissions( roles , allResources , resourcesList , allProd , productList , allNet , networkList , allSpecial , specials );
		modifyGroup( c , group , false );
		
		modifyGroupResources( c , group , null , false );
		modifyGroupProducts( c , group , null , false );
		modifyGroupNetworks( c , group , null , false );
		for( Integer resource : group.getPermissionResources() )
			modifyGroupResources( c , group , resource , true );
		for( Integer product : group.getPermissionProducts() )
			modifyGroupProducts( c , group , product , true );
		for( Integer network : group.getPermissionNetworks() )
			modifyGroupNetworks( c , group , network , true );
		
		for( String user : group.getUsers( null ) )
			engine.updatePermissions( action , user );
	}
	
}
