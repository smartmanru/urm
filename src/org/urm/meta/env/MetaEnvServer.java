package org.urm.meta.env;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.EngineTransaction;
import org.urm.engine.dist.Release;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.engine.properties.PropertyController;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.shell.Account;
import org.urm.meta.engine.AccountReference;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product._Error;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServer extends PropertyController {

	public Meta meta;
	public MetaEnvSegment sg;
	
	public String NAME = "";
	public String DESC = "";
	private VarSERVERRUNTYPE serverRunType;
	private DBEnumServerAccessType serverAccessType;
	public DBEnumOSType osType;
	
	public String BASELINE = "";
	public String XDOC = "";
	public boolean OFFLINE = false;
	
	public String ROOTPATH = "";
	public String BINPATH = "";
	public String SYSNAME = "";
	public int PORT = 0;
	private String NLBSERVER = "";
	public MetaEnvServer nlbServer;
	private String PROXYSERVER = "";
	public MetaEnvServer proxyServer;
	private String STATICSERVER = "";
	public MetaEnvServer staticServer;
	private String SUBORDINATESERVERS = "";
	public MetaEnvServer[] subordinateServers;
	public int STARTTIME = 0;
	public int STOPTIME = 0;
	public String DEPLOYPATH = "";
	public String LINKFROMPATH = "";
	public String DEPLOYSCRIPT = "";
	public String HOTDEPLOYPATH = "";
	public String HOTDEPLOYDATA = "";
	public String WEBSERVICEURL = "";
	public String WEBMAINURL = "";
	public String LOGPATH = "";
	public String LOGFILEPATH = "";
	public boolean NOPIDS = false;

	public DBEnumDbmsType dbType;
	public String DBMSADDR = "";
	public String ADMSCHEMA = "";
	public MetaDatabaseSchema admSchema;
	public String ALIGNED = "";
	public String REGIONS = "";
	
	public MetaEnvServerBase basesw;
	
	Map<String,MetaEnvServerDeployment> deployMap;
	List<MetaEnvServerDeployment> deployments;
	List<MetaEnvServerNode> nodes;

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_BASELINE = "baseserver";
	public static String PROPERTY_OSTYPE = "ostype";
	public static String PROPERTY_SERVERRUNTYPE = "runtype";
	public static String PROPERTY_SERVERACCESSTYPE = "accesstype";
	public static String PROPERTY_SYSNAME = "sysname";
	public static String PROPERTY_OFFLINE = "offline";
	
	public static String PROPERTY_ROOTPATH = "rootpath";
	public static String PROPERTY_BINPATH = "binpath";
	public static String PROPERTY_STARTTIME = "starttime";
	public static String PROPERTY_STOPTIME = "stoptime";
	public static String PROPERTY_NOPIDS = "nopids";
	
	public static String PROPERTY_PORT = "port";
	public static String PROPERTY_WEBMAINURL = "webmainurl";
	public static String PROPERTY_WEBSERVICEURL = "webserviceurl";

	public static String PROPERTY_DBMSTYPE = "dbmstype";
	public static String PROPERTY_DBMSADDR = "dbmsaddr";
	public static String PROPERTY_ADMSCHEMA = "admschema";
	public static String PROPERTY_DATAGROUPS = "datagroups";
	public static String PROPERTY_ALIGNED = "aligned";
	public static String PROPERTY_REGIONS = "regions";
	
	public static String PROPERTY_LOGPATH = "logpath";
	public static String PROPERTY_LOGFILEPATH = "logfilepath";
	public static String PROPERTY_DEPLOYPATH = "deploypath";
	public static String PROPERTY_LINKFROMPATH = "linkfrompath";
	public static String PROPERTY_HOTDEPLOYPATH = "hotdeploypath";
	public static String PROPERTY_HOTDEPLOYDATA = "hotdeploydata";
	public static String PROPERTY_DEPLOYSCRIPT = "deployscript";
	
	public static String PROPERTY_NLBSERVER = "nlbserver";
	public static String PROPERTY_PROXYSERVER = "proxy-server";
	public static String PROPERTY_STATICSERVER = "static-server";
	public static String PROPERTY_SUBORDINATESERVERS = "subordinate-servers";

	public static String PROPERTY_XDOC = "xdoc";
	
	public static String ELEMENT_NODE = "node";
	public static String ELEMENT_PLATFORM = "platform";
	public static String ELEMENT_DEPLOY = "deploy";

	public MetaEnvStartGroup startGroup;
	
	public MetaEnvServer( Meta meta , MetaEnvSegment sg ) {
		super( sg , "server" );
		this.meta = meta;
		this.sg = sg;
		
		deployments = new LinkedList<MetaEnvServerDeployment>(); 
		deployMap = new HashMap<String,MetaEnvServerDeployment>();
		nodes = new LinkedList<MetaEnvServerNode>();
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		NAME = super.getStringPropertyRequired( action , PROPERTY_NAME );
		action.trace( "load properties of server=" + NAME );
		
		DESC = super.getStringProperty( action , PROPERTY_DESC );
		BASELINE = super.getStringProperty( action , PROPERTY_BASELINE ); 
		if( BASELINE.equals( "default" ) )
			BASELINE = NAME;
		
		String SERVERRUNTYPE = super.getStringPropertyRequired( action , PROPERTY_SERVERRUNTYPE );
		serverRunType = Types.getServerRunType( SERVERRUNTYPE , false );
		String SERVERACCESSTYPE = super.getStringPropertyRequired( action , PROPERTY_SERVERACCESSTYPE );
		serverAccessType = DBEnumServerAccessType.getValue( SERVERACCESSTYPE , false );
		osType = DBEnumOSType.getValue( super.getStringProperty( action , PROPERTY_OSTYPE , "linux" ) , false );
		OFFLINE = super.getBooleanProperty( action , PROPERTY_OFFLINE );
		XDOC = super.getPathProperty( action , PROPERTY_XDOC , NAME + ".xml" );
		SYSNAME = super.getStringProperty( action , PROPERTY_SYSNAME );
		
		if( isStartable() || isDeployPossible() ) {
			ROOTPATH = super.getPathProperty( action , PROPERTY_ROOTPATH );
		}
		
		if( isStartable() ) {
			BINPATH = super.getPathProperty( action , PROPERTY_BINPATH );
			PORT = super.getIntProperty( action , PROPERTY_PORT , 0 );
			NLBSERVER = super.getStringProperty( action , PROPERTY_NLBSERVER );
			PROXYSERVER = super.getStringProperty( action , PROPERTY_PROXYSERVER , "" );
			STATICSERVER = super.getStringProperty( action , PROPERTY_STATICSERVER , "" );
			SUBORDINATESERVERS = super.getStringProperty( action , PROPERTY_SUBORDINATESERVERS , "" );
			STARTTIME = super.getIntProperty( action , PROPERTY_STARTTIME , 0 );
			STOPTIME = super.getIntProperty( action , PROPERTY_STOPTIME , 0 );
			HOTDEPLOYPATH = super.getPathProperty( action , PROPERTY_HOTDEPLOYPATH );
			HOTDEPLOYDATA = super.getStringProperty( action , PROPERTY_HOTDEPLOYDATA );
			WEBSERVICEURL = super.getStringProperty( action , PROPERTY_WEBSERVICEURL );
			WEBMAINURL = super.getStringProperty( action , PROPERTY_WEBMAINURL );
			LOGPATH = super.getPathProperty( action , PROPERTY_LOGPATH );
			LOGFILEPATH = super.getPathProperty( action , PROPERTY_LOGFILEPATH );
			NOPIDS = super.getBooleanProperty( action , PROPERTY_NOPIDS );
		}

		if( isDeployPossible() ) {
			DEPLOYPATH = super.getPathProperty( action , PROPERTY_DEPLOYPATH );
			LINKFROMPATH = super.getPathProperty( action , PROPERTY_LINKFROMPATH );
			DEPLOYSCRIPT = super.getPathProperty( action , PROPERTY_DEPLOYSCRIPT );
		}
		
		if( isDatabase() ) {
			dbType = DBEnumDbmsType.getValue( super.getStringProperty( action , PROPERTY_DBMSTYPE ) , true );
			DBMSADDR = super.getStringProperty( action , PROPERTY_DBMSADDR );
			ALIGNED = super.getStringProperty( action , PROPERTY_ALIGNED );
			REGIONS = super.getStringProperty( action , PROPERTY_REGIONS );
			ADMSCHEMA = super.getStringProperty( action , PROPERTY_ADMSCHEMA );
			
			MetaDatabase database = meta.getDatabase();
			if( !ADMSCHEMA.isEmpty() )
				admSchema = database.getSchema( ADMSCHEMA );
		}

		super.finishRawProperties();
	}
	
	public boolean isBroken() {
		return( super.isLoadFailed() );
	}
	
	public VarSERVERRUNTYPE getServerRunType() {
		return( serverRunType );
	}
	
	public DBEnumServerAccessType getServerAccessType() {
		return( serverAccessType );
	}
	
	public String getServerTypeName( ActionBase action ) throws Exception {
		return( Common.getEnumLower( serverRunType ) + "/" + Common.getEnumLower( serverAccessType ) );
	}
	
	public String getFullId( ActionBase action ) throws Exception {
		return( sg.getFullId( action ) + "-" + NAME );
	}
	
	public boolean hasBaseline( ActionBase action ) throws Exception {
		if( BASELINE.isEmpty() )
			return( false );
		return( true );
	}
	
	public String getBaselineServer( ActionBase action ) throws Exception {
		return( BASELINE );
	}

	public MetaEnvServer copy( ActionBase action , Meta meta , MetaEnvSegment sg ) throws Exception {
		MetaEnvServer r = new MetaEnvServer( meta , sg );
		r.initCopyStarted( this , sg.getProperties() );
		r.scatterProperties( action );
		
		if( basesw != null )
			r.basesw = basesw.copy( action , meta , r );
		for( MetaEnvServerDeployment deployment : deployments ) {
			MetaEnvServerDeployment rdeployment = deployment.copy( action , meta , r );
			r.addDeployment( rdeployment );
		}
		for( MetaEnvServerNode node : nodes ) {
			MetaEnvServerNode rnode = node.copy( action , meta , r );
			r.addNode( rnode );
		}
		
		r.resolveLinks( action );
		r.initFinished();
		return( r );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( sg.getProperties() ) )
			return;

		loadDeployments( action , node );
		
		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		
		super.loadFromNodeElements( action , node , true );
		super.resolveRawProperties();

		loadNodes( action , node );
		loadBase( action , node );
		
		super.initFinished();
	}

	public void resolveLinks( ActionBase action ) throws Exception {
		if( NLBSERVER != null && !NLBSERVER.isEmpty() )
			nlbServer = sg.getServer( action , NLBSERVER );
		if( PROXYSERVER != null && !PROXYSERVER.isEmpty() )
			proxyServer = sg.getServer( action , PROXYSERVER );
		if( STATICSERVER != null && !STATICSERVER.isEmpty() )
			staticServer = sg.getServer( action , STATICSERVER );
		if( SUBORDINATESERVERS != null && !SUBORDINATESERVERS.isEmpty() ) {
			String[] SERVERS = Common.splitSpaced( SUBORDINATESERVERS );
			subordinateServers = new MetaEnvServer[ SERVERS.length ];
			for( int k = 0; k < SERVERS.length; k++ )
				subordinateServers[ k ] = sg.getServer( action , SERVERS[ k ] );
		}
		
		// verify aligned
		if( isDatabase() ) {
			MetaDatabase database = meta.getDatabase();
			for( String id : Common.splitSpaced( ALIGNED ) )
				database.checkAligned( id );
		}
		
		if( basesw != null )
			basesw.resolveLinks( action );
		for( MetaEnvServerDeployment deploy : deployments )
			deploy.resolveLinks( action );
		for( MetaEnvServerNode node : nodes )
			node.resolveLinks( action );
	}
	
	public Map<String,MetaEnvServer> getAssociatedServers( ActionBase action ) throws Exception {
		Map<String,MetaEnvServer> servers = new HashMap<String,MetaEnvServer>();
		if( nlbServer != null )
			servers.put( nlbServer.NAME , nlbServer );
		if( proxyServer != null )
			servers.put( proxyServer.NAME , proxyServer );
		if( staticServer != null )
			servers.put( staticServer.NAME , staticServer );
		if( subordinateServers != null ) {
			for( MetaEnvServer server : subordinateServers )
				servers.put( server.NAME , server );
		}
		return( servers );
	}
	
	public void setStartGroup( ActionBase action , MetaEnvStartGroup group ) {
		this.startGroup = group;
	}
	
	private void loadNodes( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_NODE );
		if( items == null )
			return;
		
		int pos = 1;
		for( Node snnode : items ) {
			MetaEnvServerNode sn = new MetaEnvServerNode( meta , this , pos );
			sn.load( action , snnode );
			addNode( sn );
			pos++;
		}
	}

	private void addNode( MetaEnvServerNode sn ) {
		nodes.add( sn );
	}

	private void loadBase( ActionBase action , Node node ) throws Exception {
		Node item = ConfReader.xmlGetFirstChild( node , ELEMENT_PLATFORM );
		if( item == null )
			return;
		
		basesw = new MetaEnvServerBase( meta , this );
		basesw.load( action , item );
	}
		
	private void loadDeployments( ActionBase action , Node node ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_DEPLOY );
		if( items == null )
			return;
		
		for( Node dpnode : items ) {
			MetaEnvServerDeployment dp = new MetaEnvServerDeployment( meta , this );
			dp.load( action , dpnode );
			addDeployment( dp );
		}
	}

	private void addDeployment( MetaEnvServerDeployment dp ) {
		String name = dp.getName();
		deployments.add( dp );
		deployMap.put( name , dp );
	}
	
	public String[] getDeploymentNames() {
		return( Common.getSortedKeys( deployMap ) );
	}
	
	public MetaEnvServerDeployment findDeployment( String name ) {
		return( deployMap.get( name ) );
	}
	
	public MetaEnvServerNode[] getNodes() {
		return( nodes.toArray( new MetaEnvServerNode[0] ) );
	}

	public MetaEnvServerNode findNode( int POS ) {
		if( POS < 1 || POS > nodes.size() )
			return( null );
		return( nodes.get( POS - 1 ) );
	}
	
	public MetaEnvServerNode getNode( ActionBase action , int POS ) throws Exception {
		if( POS < 1 || POS > nodes.size() )
			action.exit2( _Error.InvalidServerNode2 , "invalid node=" + POS + ", server=" + NAME , NAME , "" + POS );
		return( nodes.get( POS - 1 ) );
	}

	public boolean hasWebServices() {
		for( MetaEnvServerDeployment deploy : deployments ) {
			if( deploy.comp != null )
				if( deploy.comp.hasWebServices() )
					return( true );
		}
		return( false );
	}

	public MetaEnvServerDeployment[] getDeployments() {
		return( deployments.toArray( new MetaEnvServerDeployment[0] ) );
	}

	public String getFullBinPath( ActionBase action ) throws Exception {
		action.checkRequired( !ROOTPATH.isEmpty() , PROPERTY_ROOTPATH );
		action.checkRequired( !BINPATH.isEmpty() , PROPERTY_BINPATH );
		return( Common.getPath( ROOTPATH , BINPATH ) );
	}
	
	public boolean checkDeployBinaries( ActionBase action ) throws Exception {
		for( MetaEnvServerDeployment deploy : deployments )
			if( deploy.binaryItem != null )
				return( true );
		return( false );
	}

	public String getNodesAsStringByHost( ActionBase action , String host ) throws Exception {
		String s = "";
		for( MetaEnvServerNode node : nodes ) {
			Account account = action.getNodeAccount( node );
			if( account.HOST.equals( host ) ) {
				if( !s.isEmpty() )
					s += " ";
				s += node.POS;
			}
		}
		return( s );
	}

	public String getNodesAsStringByAccount( ActionBase action , Account account ) throws Exception {
		String s = "";
		for( MetaEnvServerNode node : nodes ) {
			Account nodeAccount = action.getNodeAccount( node );
			if( nodeAccount.getFullName().equals( account.getFullName() ) ) {
				if( !s.isEmpty() )
					s += " ";
				s += node.POS;
			}
		}
		return( s );
	}

	public MetaEnvServerNode[] getNodes( ActionBase action , String[] NODES ) throws Exception {
		List<MetaEnvServerNode> list = new LinkedList<MetaEnvServerNode>();
		for( String NODE : NODES ) {
			MetaEnvServerNode node = getNode( action , Integer.parseInt( NODE ) );
			list.add( node );
		}
		return( list.toArray( new MetaEnvServerNode[0] ) );
	}
	
	public boolean isConfigurable() {
		if( serverAccessType == DBEnumServerAccessType.MANUAL || serverAccessType == DBEnumServerAccessType.UNKNOWN )
			return( false );
		if( serverRunType == VarSERVERRUNTYPE.DATABASE || serverRunType == VarSERVERRUNTYPE.UNKNOWN ) 
			return( false );
		return( true );
	}
	
	public boolean hasConfiguration() {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.confItem != null )
				return( true );
			if( deployment.comp != null )
				if( deployment.comp.hasConfItems() )
					return( true );
		}
		return( false );
	}

	public boolean hasConfItemDeployment( MetaDistrConfItem confItem ) {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.hasConfItemDeployment( confItem ) )
				return( true );
		}
		return( false );
	}

	public boolean hasBinaryItemDeployment( MetaDistrBinaryItem binaryItem ) {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.hasBinaryItemDeployment( binaryItem ) )
				return( true );
		}
		return( false );
	}

	public boolean hasDatabaseItemDeployment( MetaDistrDelivery delivery ) {
		for( MetaDatabaseSchema schema : delivery.getDatabaseSchemes() ) {
			if( hasDatabaseItemDeployment( schema ) )
				return( true );
		}
		return( false );
	}

	public boolean hasDatabaseItemDeployment( MetaDatabaseSchema schema ) {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.hasDatabaseItemDeployment( schema ) ) 
				return( true );
		}
		return( false );
	}
	
	public Map<String, MetaDistrConfItem> getConfItems() throws Exception {
		Map<String, MetaDistrConfItem> confs = new HashMap<String, MetaDistrConfItem>(); 
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.confItem != null ) {
				MetaDistrConfItem conf = deployment.confItem; 
				confs.put( conf.NAME , conf );
			}
			else
			if( deployment.comp != null ) {
				for( MetaDistrComponentItem item : deployment.comp.getConfItems() ) {
					if( item.confItem != null ) {
						MetaDistrConfItem conf = item.confItem; 
						confs.put( conf.NAME , conf );
					}
				}
			}
		}
		return( confs );
	}

	public MetaEnvServerNode getPrimaryNode( ActionBase action ) throws Exception {
		for( MetaEnvServerNode node : nodes ) {
			if( !node.OFFLINE )
				return( node );
		}
		
		action.exit1( _Error.MissingServerPrimaryNode1 , "unable to find primary node of server=" + NAME , NAME );
		return( null );
	}

	public MetaEnvServerLocation[] getLocations( ActionBase action , boolean binary , boolean conf ) throws Exception {
		Map<String,MetaEnvServerLocation> locations = new HashMap<String,MetaEnvServerLocation>();
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( !deployment.hasFileDeployments() )
				continue;
			
			String deployPath = deployment.getDeployPath( action );
			VarDEPLOYMODE deployType = deployment.getDeployType( action );
			String key = Common.getEnumLower( deployType ) + "-" + deployPath;
			
			if( deployment.binaryItem != null ) {
				if( binary ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( meta , this , deployType , deployPath );
						locations.put( key , location );
					}
					location.addBinaryItem( action , deployment , deployment.binaryItem , "" );
				}
			}
			else
			if( deployment.confItem != null ) {
				if( conf ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( meta , this , deployType , deployPath );
						locations.put( key , location );
					}
					location.addConfItem( action , deployment , deployment.confItem );
				}
			}
			else
			if( deployment.comp != null ) {
				if( binary && deployment.comp.hasBinaryItems() ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( meta , this , deployType , deployPath );
						locations.put( key , location );
					}
					
					for( MetaDistrComponentItem item : deployment.comp.getBinaryItems() ) {
						if( item.binaryItem != null )
							location.addBinaryItem( action , deployment , item.binaryItem , item.DEPLOY_NAME );
					}
				}
				
				if( conf && deployment.comp.hasConfItems() ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( meta , this , deployType , deployPath );
						locations.put( key , location );
					}
					
					for( MetaDistrComponentItem item : deployment.comp.getConfItems() ) {
						if( item.confItem != null )
							location.addConfItem( action , deployment , item.confItem );
					}
				}
			}
		}
		
		return( locations.values().toArray( new MetaEnvServerLocation[0] ) );
	}

	public boolean isDeployPossible() {
		// check deploy items
		if( deployments != null ) {
			for( MetaEnvServerDeployment item : deployments ) {
				if( !item.isManual() )
					return( true );
			}
		}
		
		return( false );
	}

	public Map<String,MetaDatabaseSchema> getSchemaSet( ActionBase action ) throws Exception {
		Map<String,MetaDatabaseSchema> schemaMap = new HashMap<String,MetaDatabaseSchema>();
		for( MetaEnvServerDeployment item : deployments ) {
			if( item.isDatabase() )
				schemaMap.put( item.schema.NAME , item.schema );
			else
			if( item.isComponent() ) {
				for( MetaDistrComponentItem compItem : item.comp.getSchemaItems() )
					schemaMap.put( compItem.schema.NAME , compItem.schema );
			}
		}
		return( schemaMap );
	}

	public MetaEnvServerNode getStandbyNode( ActionBase action ) throws Exception {
		for( MetaEnvServerNode node : getNodes() ) {
			if( node.DBSTANDBY )
				return( node );
		}
		
		action.exit0( _Error.MissingStandbyNode0 , "unable to find standby node" );
		return( null );
	}

	public MetaEnvServerNode getMasterNode( ActionBase action ) throws Exception {
		for( MetaEnvServerNode node : getNodes() ) {
			if( node.OFFLINE )
				continue;
			
			if( node.nodeType == VarNODETYPE.ADMIN )
				return( node );
		}
		
		action.exit0( _Error.MissingActiveNode0 , "unable to find active node" );
		return( null );
	}

	public boolean isWindows() {
		return( osType == DBEnumOSType.WINDOWS );
	}

	public boolean isLinux() {
		return( osType == DBEnumOSType.LINUX );
	}

	public boolean isDatabase() {
		return( serverRunType == VarSERVERRUNTYPE.DATABASE );
	}

	public boolean isService() {
		return( serverAccessType == DBEnumServerAccessType.SERVICE );
	}

	public boolean isDocker() {
		return( serverAccessType == DBEnumServerAccessType.DOCKER );
	}

	public boolean isPacemaker() {
		return( serverAccessType == DBEnumServerAccessType.PACEMAKER );
	}

	public boolean isCommand() {
		return( serverRunType == VarSERVERRUNTYPE.COMMAND );
	}
	
	public boolean isGeneric() {
		return( serverAccessType == DBEnumServerAccessType.GENERIC );
	}

	public boolean isManual() {
		return( serverAccessType == DBEnumServerAccessType.MANUAL );
	}

	public boolean isWebUser() {
		return( serverRunType == VarSERVERRUNTYPE.WEBUI );
	}

	public boolean isWebApp() {
		return( serverRunType == VarSERVERRUNTYPE.WEBAPP );
	}

	public boolean isCallable() {
		if( serverRunType == VarSERVERRUNTYPE.DATABASE ||
			serverRunType == VarSERVERRUNTYPE.APP ) 
			return( false );
		return( true );
			
	}

	public boolean isStartable() {
		if( serverAccessType == DBEnumServerAccessType.MANUAL )
			return( false );
		return( true );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveSplit( doc , root );
		
		if( basesw != null ) {
			Element baseElement = Common.xmlCreateElement( doc , root , ELEMENT_PLATFORM );
			basesw.save( action , doc , baseElement );
		}

		for( MetaEnvServerDeployment deploy : deployments ) {
			Element deployElement = Common.xmlCreateElement( doc , root , ELEMENT_DEPLOY );
			deploy.save( action , doc , deployElement );
		}
		
		for( MetaEnvServerNode node : nodes ) {
			Element nodeElement = Common.xmlCreateElement( doc , root , ELEMENT_NODE );
			node.save( action , doc , nodeElement );
		}
	}
	
	public void createServer( ActionBase action , String NAME , String DESC , DBEnumOSType osType , VarSERVERRUNTYPE runType , DBEnumServerAccessType accessType , String sysname ) throws Exception {
		if( !super.initCreateStarted( sg.getProperties() ) )
			return;

		OFFLINE = false;
		super.setStringProperty( PROPERTY_NAME , NAME );
		super.setStringProperty( PROPERTY_DESC , DESC );
		super.setStringProperty( PROPERTY_OSTYPE , Common.getEnumLower( osType ) );
		super.setStringProperty( PROPERTY_SERVERRUNTYPE , Common.getEnumLower( runType ) );
		super.setStringProperty( PROPERTY_SERVERACCESSTYPE , Common.getEnumLower( accessType ) );
		super.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
		super.setStringProperty( PROPERTY_SYSNAME , sysname );
		super.finishProperties( action );
		super.initFinished();
		
		scatterProperties( action );
	}

	public void modifyServer( ActionBase action , String NAME , String DESC , DBEnumOSType osType , VarSERVERRUNTYPE runType , DBEnumServerAccessType accessType , String sysname ) throws Exception {
		super.setStringProperty( PROPERTY_NAME , NAME );
		super.setStringProperty( PROPERTY_DESC , DESC );
		super.setStringProperty( PROPERTY_OSTYPE , Common.getEnumLower( osType ) );
		super.setStringProperty( PROPERTY_SERVERRUNTYPE , Common.getEnumLower( runType ) );
		super.setStringProperty( PROPERTY_SERVERACCESSTYPE , Common.getEnumLower( accessType ) );
		super.setStringProperty( PROPERTY_SYSNAME , sysname );
		super.finishProperties( action );
		
		scatterProperties( action );
	}

	public void setBaseline( EngineTransaction transaction , String baselineServer ) throws Exception {
		super.setSystemStringProperty( PROPERTY_BASELINE , baselineServer );
	}
	
	public void setPlatform( EngineTransaction transaction , BaseItem item ) throws Exception {
		if( basesw == null ) {
			basesw = new MetaEnvServerBase( meta , this );
			basesw.createBase( transaction.action , item );
		}
		else
			basesw.setItem( transaction , item );
	}
	
	public void setOffline( EngineTransaction transaction , boolean offline ) throws Exception {
		// check props
		if( !offline ) {
			ActionBase action = transaction.getAction();
			if( ROOTPATH == null || ROOTPATH.isEmpty() ) {
				if( deployments != null && !deployments.isEmpty() )
					action.exit1( _Error.RootpathEmptyRequiredForDeployments1 , "rootpath is empty, required for deployments server=" + NAME , NAME );
				if( isGeneric() )
					action.exit1( _Error.RootpathEmptyRequiredForGeneric1 , "rootpath is empty, required for generic server=" + NAME , NAME );
			}
		}
		
		super.setSystemBooleanProperty( PROPERTY_OFFLINE , offline );
	}

	public void createNode( EngineTransaction transaction , MetaEnvServerNode node ) {
		addNode( transaction , node );
	}
	
	private void addNode( EngineTransaction transaction , MetaEnvServerNode node ) {
		int index = nodes.size();
		if( node.POS > 0 )
			index = node.POS - 1;
		else
			node.setPos( transaction , index + 1 );
		
		if( index >= nodes.size() ) {
			addNode( node );
			return;
		}
		
		// add and shift tail nodes
		nodes.add( index , node );
		for( int k = index + 1; k < nodes.size(); k++ ) {
			MetaEnvServerNode item = nodes.get( k );
			item.setPos( transaction , k + 1 );
		}
	}
	
	public void deleteNode( EngineTransaction transaction , MetaEnvServerNode node ) {
		int index = nodes.indexOf( node );
		if( index < 0 )
			return;
		
		// remove and shift tail nodes
		nodes.remove( index );
		for( int k = index; k < nodes.size(); k++ ) {
			MetaEnvServerNode item = nodes.get( k );
			item.setPos( transaction , k + 1 );
		}
	}

	public void modifyNode( EngineTransaction transaction , MetaEnvServerNode node ) {
		int index = nodes.indexOf( node );
		if( index < 0 )
			return;
		
		if( index + 1 == node.POS )
			return;
		
		nodes.remove( index );
		addNode( transaction , node );
	}

	public void getApplicationReferences( HostAccount account , List<AccountReference> refs ) {
		for( MetaEnvServerNode node : nodes )
			node.getApplicationReferences( account , refs );
	}

	public void deleteHostAccount( EngineTransaction transaction , HostAccount account ) throws Exception {
		super.deleteObject();
	}
	
	public void setProperties( EngineTransaction transaction , PropertySet props , boolean system ) throws Exception {
		super.updateProperties( transaction , props , system );
		scatterProperties( transaction.getAction() );
	}

	public void setDeployments( EngineTransaction transaction , List<MetaEnvServerDeployment> deploymentsNew ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments )
			deployment.deleteObject();
		deployments.clear();
		deployMap.clear();
		
		for( MetaEnvServerDeployment deployment : deploymentsNew )
			addDeployment( deployment );
	}

	public void reflectDeleteBinaryItem( EngineTransaction transaction , MetaDistrBinaryItem item ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.isBinaryItem() && deployment.binaryItem == item ) {
				deployMap.remove( deployment.getName() );
				deployments.remove( deployment );
				deployment.deleteObject();
			}
		}
	}
	
	public void reflectDeleteConfItem( EngineTransaction transaction , MetaDistrConfItem item ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.isConfItem() && deployment.confItem == item ) {
				deployMap.remove( deployment.getName() );
				deployments.remove( deployment );
				deployment.deleteObject();
			}
		}
	}
	
	public void reflectDeleteComponent( EngineTransaction transaction , MetaDistrComponent item ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.isComponent() && deployment.comp == item ) {
				deployMap.remove( deployment.getName() );
				deployments.remove( deployment );
				deployment.deleteObject();
			}
		}
	}
	
	public void reflectDeleteSchema( EngineTransaction transaction , MetaDatabaseSchema schema ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.isDatabase() && deployment.schema == schema ) {
				deployMap.remove( deployment.getName() );
				deployments.remove( deployment );
				deployment.deleteObject();
			}
		}
	}

	public boolean isReleaseApplicable( Release release ) {
		for( ReleaseDelivery delivery : release.getDeliveries() ) {
			if( isDatabase() ) {
				for( ReleaseTargetItem item : delivery.getDatabaseItems() ) {
					if( hasDatabaseItemDeployment( item.schema ) )
						return( true );
				}
			}
			
			for( ReleaseTargetItem item : delivery.getProjectItems() ) {
				if( hasBinaryItemDeployment( item.distItem ) )
					return( true );
			}
			
			for( ReleaseTarget item : delivery.getManualItems() ) {
				if( hasBinaryItemDeployment( item.distManualItem ) )
					return( true );
			}
			
			for( ReleaseTarget item : delivery.getDerivedItems() ) {
				if( hasBinaryItemDeployment( item.distDerivedItem ) )
					return( true );
			}
			
			for( ReleaseTarget item : delivery.getConfItems() ) {
				if( hasConfItemDeployment( item.distConfItem ) )
					return( true );
			}
		}
		return( false );
	}

	public String getSchemaDBName( MetaDatabaseSchema schema ) {
		for( MetaEnvServerDeployment d : deployments ) {
			if( d.isDatabase() ) {
				if( d.schema == schema )
					return( d.DBNAME );
			}
			else {
				MetaDistrComponentItem item = d.comp.findSchemaItem( schema.NAME );
				if( item != null )
					return( schema.DBNAMEDEF );
			}
		}
		
		return( null );
	}
	
	public String getSchemaDBUser( MetaDatabaseSchema schema ) {
		for( MetaEnvServerDeployment d : deployments ) {
			if( d.isDatabase() ) {
				if( d.schema == schema )
					return( d.DBUSER );
			}
			else {
				MetaDistrComponentItem item = d.comp.findSchemaItem( schema.NAME );
				if( item != null )
					return( schema.DBUSERDEF );
			}
		}
		
		return( null );
	}
	
}
