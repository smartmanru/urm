package org.urm.db.engine;

import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.EngineDB;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.engine.Network;
import org.urm.meta.engine.NetworkHost;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBEngineInfrastructure {

	public static String ELEMENT_DATACENTER = "datacenter";
	public static String ELEMENT_NETWORK = "network";
	public static String ELEMENT_HOST = "host";
	public static String ELEMENT_ACCOUNT = "account";
	public static String TABLE_DATACENTER = "urm_datacenter";
	public static String TABLE_NETWORK = "urm_network";
	public static String TABLE_HOST = "urm_host";
	public static String TABLE_ACCOUNT = "account";
	public static String FIELD_DATACENTER_ID = "datacenter_id";
	public static String FIELD_DATACENTER_DESC = "xdesc";
	public static String FIELD_NETWORK_ID = "network_id";
	public static String FIELD_NETWORK_DATACENTER = "datacenter_id";
	public static String FIELD_NETWORK_DESC = "xdesc";
	public static String FIELD_HOST_ID = "host_id";
	public static String FIELD_HOST_NETWORK = "network_id";
	public static String FIELD_HOST_DESC = "xdesc";
	public static String FIELD_ACCOUNT_ID = "account_id";
	public static String FIELD_ACCOUNT_HOST = "host_id";
	public static String FIELD_ACCOUNT_DESC = "xdesc";
	public static String XMLPROP_DATACENTER_NAME = "id";
	public static String XMLPROP_NETWORK_NAME = "id";
	public static String XMLPROP_HOST_NAME = "id";
	public static String XMLPROP_ACCOUNT_NAME = "id";
	
	public static PropertyEntity upgradeEntityDatacenter( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.DATACENTER , DBEnumParamEntityType.DATACENTER , DBEnumObjectVersionType.CORE , TABLE_DATACENTER , FIELD_DATACENTER_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaStringVar( Datacenter.PROPERTY_NAME , Datacenter.PROPERTY_NAME , XMLPROP_DATACENTER_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( Datacenter.PROPERTY_DESC , FIELD_DATACENTER_DESC , Datacenter.PROPERTY_DESC , "Description" , false , null )
		} ) );
	}

	public static PropertyEntity upgradeEntityNetwork( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.NETWORK , DBEnumParamEntityType.NETWORK , DBEnumObjectVersionType.CORE , TABLE_NETWORK , FIELD_NETWORK_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaStringDatabaseOnly( FIELD_NETWORK_DATACENTER , "Datacenter" , true , null ) ,
				EntityVar.metaStringVar( Network.PROPERTY_NAME , Network.PROPERTY_NAME , XMLPROP_NETWORK_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( Network.PROPERTY_DESC , FIELD_NETWORK_DESC , Network.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaString( Network.PROPERTY_MASK , "Address mask" , true , null )
		} ) );
	}

	public static PropertyEntity upgradeEntityNetworkHost( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.HOST , DBEnumParamEntityType.HOST , DBEnumObjectVersionType.CORE , TABLE_HOST , FIELD_HOST_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaStringDatabaseOnly( FIELD_HOST_NETWORK , "Network" , true , null ) ,
				EntityVar.metaStringVar( NetworkHost.PROPERTY_NAME , Network.PROPERTY_NAME , XMLPROP_NETWORK_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( NetworkHost.PROPERTY_DESC , FIELD_NETWORK_DESC , Network.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaString( NetworkHost.PROPERTY_IP , "IP address" , true , null ) ,
				EntityVar.metaInteger( NetworkHost.PROPERTY_PORT , "Port" , true , 22 ) ,
				EntityVar.metaEnum( NetworkHost.PROPERTY_OSTYPE , "Operating system" , true , DBEnumOSType.UNKNOWN )
		} ) );
	}

	public static PropertyEntity upgradeEntityHostAccount( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ACCOUNT , DBEnumParamEntityType.ACCOUNT , DBEnumObjectVersionType.CORE , TABLE_ACCOUNT , FIELD_ACCOUNT_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaStringDatabaseOnly( FIELD_ACCOUNT_HOST , "Host" , true , null ) ,
				EntityVar.metaStringVar( HostAccount.PROPERTY_NAME , Network.PROPERTY_NAME , XMLPROP_NETWORK_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( NetworkHost.PROPERTY_DESC , FIELD_NETWORK_DESC , Network.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaString( NetworkHost.PROPERTY_IP , "IP address" , true , null ) ,
				EntityVar.metaInteger( NetworkHost.PROPERTY_PORT , "Port" , true , 22 ) ,
				EntityVar.metaEnum( NetworkHost.PROPERTY_OSTYPE , "Operating system" , true , DBEnumOSType.UNKNOWN )
		} ) );
	}

	public static PropertyEntity loaddbEntityDatacenter( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.DATACENTER , DBEnumParamEntityType.DATACENTER , DBEnumObjectVersionType.CORE , TABLE_DATACENTER , FIELD_DATACENTER_ID );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static PropertyEntity loaddbEntityNetwork( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.NETWORK , DBEnumParamEntityType.NETWORK , DBEnumObjectVersionType.CORE , TABLE_NETWORK , FIELD_NETWORK_ID );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static PropertyEntity loaddbEntityNetworkHost( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.HOST , DBEnumParamEntityType.HOST , DBEnumObjectVersionType.CORE , TABLE_HOST , FIELD_HOST_ID );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static PropertyEntity loaddbEntityHostAccount( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.ACCOUNT , DBEnumParamEntityType.ACCOUNT , DBEnumObjectVersionType.CORE , TABLE_ACCOUNT , FIELD_ACCOUNT_ID );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static void importxml( EngineLoader loader , EngineInfrastructure infra , Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_DATACENTER );
		if( list != null ) {
			for( Node node : list ) {
				Datacenter dc = importxmlDatacenter( loader , infra , node );
				infra.addDatacenter( dc );
			}
		}
	}

	private static Datacenter importxmlDatacenter( EngineLoader loader , EngineInfrastructure infra , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppDatacenter;
		
		Datacenter datacenter = new Datacenter( infra );
		datacenter.NAME = entity.getAttrValue( root , Datacenter.PROPERTY_NAME );
		datacenter.DESC = entity.getAttrValue( root , Datacenter.PROPERTY_DESC );
		modifyDatacenter( c , datacenter , true );
		
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_NETWORK );
		if( list != null ) {
			for( Node node : list ) {
				Network network = importxmlNetwork( loader , infra , datacenter , node );
				infra.addNetwork( network );
			}
		}
		
		return( datacenter );
	}
	
	private static Network importxmlNetwork( EngineLoader loader , EngineInfrastructure infra , Datacenter datacenter , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppNetwork;
		
		Network network = new Network( datacenter );
		network.NAME = entity.getAttrValue( root , Network.PROPERTY_NAME );
		network.DESC = entity.getAttrValue( root , Network.PROPERTY_DESC );
		network.MASK = entity.getAttrValue( root , Network.PROPERTY_MASK );
		modifyNetwork( c , network , true );
		
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_HOST );
		if( list != null ) {
			for( Node node : list ) {
				NetworkHost host = importxmlHost( loader , infra , network , node );
				infra.addHost( host );
			}
		}
		
		return( network );
	}
	
	private static NetworkHost importxmlHost( EngineLoader loader , EngineInfrastructure infra , Network network , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppNetworkHost;
		
		NetworkHost host = new NetworkHost( network );
		host.NAME = entity.getAttrValue( root , NetworkHost.PROPERTY_NAME );
		host.DESC = entity.getAttrValue( root , NetworkHost.PROPERTY_DESC );
		host.IP = entity.getAttrValue( root , NetworkHost.PROPERTY_IP );
		host.PORT = entity.getIntAttrValue( root , NetworkHost.PROPERTY_PORT );
		modifyHost( c , host , true );
		
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_ACCOUNT );
		if( list != null ) {
			for( Node node : list ) {
				HostAccount account = importxmlAccount( loader , infra , host , node );
				infra.addAccount( account );
			}
		}
		
		return( host );
	}
	
	private static HostAccount importxmlAccount( EngineLoader loader , EngineInfrastructure infra , NetworkHost host , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppHostAccount;
		
		HostAccount account = new HostAccount( host );
		account.NAME = entity.getAttrValue( root , HostAccount.PROPERTY_NAME );
		account.DESC = entity.getAttrValue( root , HostAccount.PROPERTY_DESC );
		account.ADMIN = entity.getBooleanAttrValue( root , HostAccount.PROPERTY_ADMIN , false );
		account.RESOURCE = entity.getAttrValue( root , HostAccount.PROPERTY_RESOURCE );
		modifyAccount( c , account , true );
		
		return( account );
	}
	
	private static void modifyDatacenter( DBConnection c , Datacenter datacenter , boolean insert ) throws Exception {
		if( insert )
			datacenter.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , datacenter.NAME , DBEnumObjectType.DATACENTER );
		datacenter.CV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppDatacenter , datacenter.ID , datacenter.CV , new String[] {
				EngineDB.getString( datacenter.NAME ) , 
				EngineDB.getString( datacenter.DESC )
				} , insert );
	}

	private static void modifyNetwork( DBConnection c , Network network , boolean insert ) throws Exception {
		if( insert )
			network.ID = DBNames.getNameIndex( c , network.datacenter.ID , network.NAME , DBEnumObjectType.NETWORK );
		network.CV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppNetwork , network.ID , network.CV , new String[] {
				EngineDB.getInteger( network.datacenter.ID ) , 
				EngineDB.getString( network.NAME ) , 
				EngineDB.getString( network.DESC ) ,
				EngineDB.getString( network.MASK )
				} , insert );
	}

	private static void modifyHost( DBConnection c , NetworkHost host , boolean insert ) throws Exception {
		if( insert )
			host.ID = DBNames.getNameIndex( c , host.network.ID , host.NAME , DBEnumObjectType.HOST );
		host.CV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppNetworkHost , host.ID , host.CV , new String[] {
				EngineDB.getInteger( host.network.ID ) , 
				EngineDB.getString( host.NAME ) , 
				EngineDB.getString( host.DESC ) ,
				EngineDB.getString( host.IP ) ,
				EngineDB.getInteger( host.PORT )
				} , insert );
	}

	private static void modifyAccount( DBConnection c , HostAccount account , boolean insert ) throws Exception {
		if( insert )
			account.ID = DBNames.getNameIndex( c , account.host.ID , account.NAME , DBEnumObjectType.ACCOUNT );
		account.CV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppHostAccount , account.ID , account.CV , new String[] {
				EngineDB.getInteger( account.host.ID ) , 
				EngineDB.getString( account.NAME ) , 
				EngineDB.getString( account.DESC ) ,
				EngineDB.getBoolean( account.ADMIN ) ,
				EngineDB.getString( account.RESOURCE )
				} , insert );
	}

	public static Datacenter createDatacenter( EngineTransaction transaction , EngineInfrastructure infra , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( infra.findDatacenter( name ) != null )
			transaction.exitUnexpectedState();
		
		Datacenter datacenter = new Datacenter( infra );
		datacenter.createDatacenter( name , desc );
		modifyDatacenter( c , datacenter , true );
		
		infra.addDatacenter( datacenter );
		return( datacenter );
	}
	
	public static void modifyDatacenter( EngineTransaction transaction , EngineInfrastructure infra , Datacenter datacenter , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		datacenter.modifyDatacenter( name , desc );
		modifyDatacenter( c , datacenter , false );
		infra.updateDatacenter( datacenter );
	}
	
	public static void deleteDatacenter( EngineTransaction transaction , EngineInfrastructure infra , Datacenter datacenter ) throws Exception {
		if( !datacenter.isEmpty() )
			transaction.exit0( _Error.DatacenterNotEmpty0 , "Datacenter is not empty, unable to delete" );
		
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.deleteAppObject( c , entities.entityAppDatacenter , datacenter.ID , c.getNextCoreVersion() );
		infra.removeDatacenter( datacenter );
		datacenter.deleteObject();
	}
	
	public static Network createNetwork( EngineTransaction transaction , EngineInfrastructure infra , Datacenter datacenter , String name , String desc , String mask ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( datacenter.findNetwork( name ) != null )
			transaction.exitUnexpectedState();
		
		Network network = new Network( datacenter );
		network.createNetwork( name , desc , mask );
		modifyNetwork( c , network , true );
		
		infra.addNetwork( network );
		return( network );
	}

	public static void modifyNetwork( EngineTransaction transaction , EngineInfrastructure infra , Network network , String name , String desc , String mask ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		network.modifyNetwork( name , desc , mask );
		modifyNetwork( c , network , false );
		infra.updateNetwork( network );
	}
	
	public static void deleteNetwork( EngineTransaction transaction , EngineInfrastructure infra , Network network ) throws Exception {
		if( !network.isEmpty() )
			transaction.exit0( _Error.NetworkNotEmpty0 , "Network is not empty, unable to delete" );
		
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.deleteAppObject( c , entities.entityAppNetwork , network.ID , c.getNextCoreVersion() );
		infra.removeNetwork( network );
		network.deleteObject();
	}
	
	public static NetworkHost createHost( EngineTransaction transaction , EngineInfrastructure infra , Network network , String name , String desc , DBEnumOSType osType , String ip , int port ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( network.findHost( name ) != null )
			transaction.exitUnexpectedState();
		
		NetworkHost host = new NetworkHost( network );
		host.createHost( name , desc , osType , ip , port );
		modifyHost( c , host , true );
		
		infra.addHost( host );
		return( host );
	}

	public static void modifyHost( EngineTransaction transaction , EngineInfrastructure infra , NetworkHost host , String name , String desc , DBEnumOSType osType , String ip , int port ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		host.modifyHost( name , desc , osType , ip , port );
		modifyHost( c , host , false );
		infra.updateHost( host );
	}
	
	public static void deleteHost( EngineTransaction transaction , EngineInfrastructure infra , NetworkHost host ) throws Exception {
		if( !host.isEmpty() )
			transaction.exit0( _Error.HostNotEmpty0 , "Host is not empty, unable to delete" );
		
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.deleteAppObject( c , entities.entityAppNetworkHost , host.ID , c.getNextCoreVersion() );
		infra.removeHost( host );
		host.deleteObject();
	}
	
	public static HostAccount createAccount( EngineTransaction transaction , EngineInfrastructure infra , NetworkHost host , String user , boolean admin , String resource ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( host.findAccount( user ) != null )
			transaction.exitUnexpectedState();
		
		HostAccount account = new HostAccount( host );
		account.createAccount( user , admin , resource );
		modifyAccount( c , account , true );
		
		infra.addAccount( account );
		return( account );
	}
	
	public static void modifyAccount( EngineTransaction transaction , EngineInfrastructure infra , HostAccount account , String user , boolean admin , String resource ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		account.modifyAccount( user , admin , resource );
		modifyAccount( c , account , false );
		infra.updateAccount( account );
	}
	
	public static void deleteAccount( EngineTransaction transaction , EngineInfrastructure infra , HostAccount account ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.deleteAppObject( c , entities.entityAppHostAccount , account.ID , c.getNextCoreVersion() );
		infra.removeAccount( account );
		account.deleteObject();
	}

	public static void loaddb( EngineLoader loader , EngineInfrastructure infra ) throws Exception {
	}

	public static void exportxml( EngineLoader loader , EngineInfrastructure infra , Document doc , Element root ) throws Exception {
	}
	
}
