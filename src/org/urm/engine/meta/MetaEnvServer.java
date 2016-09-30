package org.urm.engine.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.ServerTransaction;
import org.urm.engine.meta.Meta.VarDBMSTYPE;
import org.urm.engine.meta.Meta.VarDEPLOYTYPE;
import org.urm.engine.meta.Meta.VarSERVERACCESSTYPE;
import org.urm.engine.meta.Meta.VarSERVERRUNTYPE;
import org.urm.engine.shell.Account;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServer extends PropertyController {

	public Meta meta;
	public MetaEnvDC dc;
	
	public String NAME;
	private VarSERVERRUNTYPE serverRunType;
	private VarSERVERACCESSTYPE serverAccessType;
	public VarOSTYPE osType;
	
	public String BASELINE;
	public String XDOC;
	public boolean OFFLINE;
	
	public String ROOTPATH;
	public String BINPATH;
	public String SERVICENAME;
	public int PORT;
	private String NLBSERVER;
	public MetaEnvServer nlbServer;
	private String PROXYSERVER;
	public MetaEnvServer proxyServer;
	private String STATICSERVER;
	public MetaEnvServer staticServer;
	private String SUBORDINATESERVERS;
	public MetaEnvServer[] subordinateServers;
	public int STARTTIME;
	public int STOPTIME;
	public String DEPLOYPATH;
	public String LINKFROMPATH;
	public String DEPLOYSCRIPT;
	public String HOTDEPLOYPATH;
	public String HOTDEPLOYDATA;
	public String WEBDOMAIN;
	public String WEBMAINURL;
	public String APPSERVER;
	public String APPSERVERVERSION;
	public String LOGPATH;
	public String LOGFILEPATH;
	public boolean NOPIDS;

	public VarDBMSTYPE dbType;
	public String DBMSADDR;
	public String DATAGROUPS;
	public Map<String,MetaDatabaseDatagroup> datagroupMap;
	public String ADMSCHEMA;
	public MetaDatabaseSchema admSchema;
	public String ALIGNED;
	public String REGIONS;
	
	public MetaEnvServerBase basesw;
	public MetaEnvStartGroup startGroup;
	
	List<MetaEnvServerDeployment> deployments;
	List<MetaEnvServerNode> nodes;

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_BASELINE = "baseserver";
	public static String PROPERTY_OSTYPE = "ostype";
	public static String PROPERTY_SERVERRUNTYPE = "runtype";
	public static String PROPERTY_SERVERACCESSTYPE = "accesstype";
	
	public static String PROPERTY_XDOC = "xdoc";
	public static String PROPERTY_OFFLINE = "offline";
	
	public static String PROPERTY_ROOTPATH = "rootpath";
	public static String PROPERTY_BINPATH = "binpath";
	public static String PROPERTY_SERVICENAME = "servicename";
	public static String PROPERTY_PORT = "port";
	public static String PROPERTY_NLBSERVER = "nlbserver";
	public static String PROPERTY_PROXYSERVER = "proxy-server";
	public static String PROPERTY_STATICSERVER = "static-server";
	public static String PROPERTY_SUBORDINATESERVERS = "subordinate-servers";
	public static String PROPERTY_STARTTIME = "starttime";
	public static String PROPERTY_STOPTIME = "stoptime";
	public static String PROPERTY_DEPLOYPATH = "deploypath";
	public static String PROPERTY_LINKFROMPATH = "linkfrompath";
	public static String PROPERTY_DEPLOYSCRIPT = "deployscript";
	public static String PROPERTY_HOTDEPLOYPATH = "hotdeploypath";
	public static String PROPERTY_HOTDEPLOYDATA = "hotdeploydata";
	public static String PROPERTY_WEBDOMAIN = "webdomain";
	public static String PROPERTY_WEBMAINURL = "webmainurl";
	public static String PROPERTY_LOGPATH = "logpath";
	public static String PROPERTY_LOGFILEPATH = "logfilepath";
	public static String PROPERTY_NOPIDS = "nopids";

	public static String PROPERTY_DBMSTYPE = "dbmstype";
	public static String PROPERTY_DBMSADDR = "dbmsaddr";
	public static String PROPERTY_DATAGROUPS = "datagroups";
	public static String PROPERTY_ADMSCHEMA = "admschema";
	public static String PROPERTY_ALIGNED = "aligned";
	public static String PROPERTY_REGIONS = "regions";
	
	public static String ELEMENT_NODE = "node";
	public static String ELEMENT_BASE = "base";
	public static String ELEMENT_DEPLOY = "deploy";
	
	public MetaEnvServer( Meta meta , MetaEnvDC dc ) {
		super( dc , "server" );
		this.meta = meta;
		this.dc = dc;
		
		deployments = new LinkedList<MetaEnvServerDeployment>();
		nodes = new LinkedList<MetaEnvServerNode>();
	}

	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		datagroupMap = new HashMap<String,MetaDatabaseDatagroup>(); 
		
		NAME = properties.getSystemRequiredStringProperty( PROPERTY_NAME );
		action.trace( "load properties of server=" + NAME );
		
		BASELINE = super.getStringProperty( action , PROPERTY_BASELINE ); 
		if( BASELINE.equals( "default" ) )
			BASELINE = NAME;
		
		String SERVERRUNTYPE = super.getStringPropertyRequired( action , PROPERTY_SERVERRUNTYPE );
		serverRunType = Meta.getServerRunType( SERVERRUNTYPE );
		String SERVERACCESSTYPE = super.getStringPropertyRequired( action , PROPERTY_SERVERACCESSTYPE );
		serverAccessType = Meta.getServerAccessType( SERVERACCESSTYPE );
		osType = Meta.getOSType( super.getStringPropertyRequired( action , PROPERTY_OSTYPE , "linux" ) );
		OFFLINE = super.getBooleanProperty( action , PROPERTY_OFFLINE );
		XDOC = super.getPathProperty( action , PROPERTY_XDOC , NAME + ".xml" );
		
		if( isStartable( action ) || isDeployPossible( action ) ) {
			ROOTPATH = super.getPathProperty( action , PROPERTY_ROOTPATH );
		}
		
		if( isStartable( action ) ) {
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
			WEBDOMAIN = super.getStringProperty( action , PROPERTY_WEBDOMAIN );
			WEBMAINURL = super.getStringProperty( action , PROPERTY_WEBMAINURL );
			LOGPATH = super.getPathProperty( action , PROPERTY_LOGPATH );
			LOGFILEPATH = super.getPathProperty( action , PROPERTY_LOGFILEPATH );
			NOPIDS = super.getBooleanProperty( action , PROPERTY_NOPIDS );
		}

		if( isService( action ) )
			SERVICENAME = super.getStringPropertyRequired( action , PROPERTY_SERVICENAME );

		if( isDeployPossible( action ) ) {
			DEPLOYPATH = super.getPathProperty( action , PROPERTY_DEPLOYPATH );
			LINKFROMPATH = super.getPathProperty( action , PROPERTY_LINKFROMPATH );
			DEPLOYSCRIPT = super.getPathProperty( action , PROPERTY_DEPLOYSCRIPT );
		}
		
		if( isDatabase( action ) ) {
			dbType = Meta.getDbmsType( super.getStringPropertyRequired( action , PROPERTY_DBMSTYPE ) );
			DBMSADDR = super.getStringPropertyRequired( action , PROPERTY_DBMSADDR );
			DATAGROUPS = super.getStringPropertyRequired( action , PROPERTY_DATAGROUPS );
			ALIGNED = super.getStringProperty( action , PROPERTY_ALIGNED );
			REGIONS = super.getStringProperty( action , PROPERTY_REGIONS );
			ADMSCHEMA = super.getStringProperty( action , PROPERTY_ADMSCHEMA );
			
			MetaDatabase database = meta.getDatabase( action );
			for( String dg : Common.splitSpaced( DATAGROUPS ) ) {
				MetaDatabaseDatagroup datagroup = database.getDatagroup( action , dg );
				datagroupMap.put( datagroup.NAME , datagroup );
			}
			
			admSchema = database.getSchema( action , ADMSCHEMA );
		}

		properties.finishRawProperties();
	}
	
	public VarSERVERRUNTYPE getServerRunType( ActionBase action ) throws Exception {
		return( serverRunType );
	}
	
	public VarSERVERACCESSTYPE getServerAccessType( ActionBase action ) throws Exception {
		return( serverAccessType );
	}
	
	public String getServerTypeName( ActionBase action ) throws Exception {
		return( Common.getEnumLower( serverRunType ) + "/" + Common.getEnumLower( serverAccessType ) );
	}
	
	public String getFullId( ActionBase action ) throws Exception {
		return( dc.getFullId( action ) + "-" + NAME );
	}
	
	public boolean hasBaseline( ActionBase action ) throws Exception {
		if( BASELINE.isEmpty() )
			return( false );
		return( true );
	}
	
	public String getBaselineServer( ActionBase action ) throws Exception {
		return( BASELINE );
	}

	public MetaEnvServer copy( ActionBase action , Meta meta , MetaEnvDC dc ) throws Exception {
		MetaEnvServer r = new MetaEnvServer( meta , dc );
		r.initCopyStarted( this , dc.getProperties() );
		r.scatterProperties( action );
		r.initFinished();
		return( r );
	}
	
	public void load( ActionBase action , Node node , boolean loadProps ) throws Exception {
		loadDeployments( action , node );
		
		properties = new PropertySet( "server" , dc.getProperties() );
		properties.loadFromNodeAttributes( node );
		scatterProperties( action );
		
		if( loadProps ) {
			properties.loadFromNodeElements( node );
			properties.resolveRawProperties();
		}

		loadNodes( action , node , loadProps );
		loadBase( action , node );
	}

	public String[] getPropertyList( ActionBase action ) throws Exception {
		return( properties.getRunningProperties() );
	}
	
	public String getPropertyValue( ActionBase action , String var ) throws Exception {
		return( properties.getPropertyAny( var ) );
	}
	
	public void resolveLinks( ActionBase action ) throws Exception {
		// check props
		if( ROOTPATH == null || ROOTPATH.isEmpty() ) {
			if( deployments != null && !deployments.isEmpty() )
				action.exit1( _Error.RootpathEmptyRequiredForDeployments1 , "rootpath is empty, required for deployments server=" + NAME , NAME );
			if( isGeneric( action ) )
				action.exit1( _Error.RootpathEmptyRequiredForGeneric1 , "rootpath is empty, required for generic server=" + NAME , NAME );
		}
		
		if( NLBSERVER != null && !NLBSERVER.isEmpty() )
			nlbServer = dc.getServer( action , NLBSERVER );
		if( PROXYSERVER != null && !PROXYSERVER.isEmpty() )
			proxyServer = dc.getServer( action , PROXYSERVER );
		if( STATICSERVER != null && !STATICSERVER.isEmpty() )
			staticServer = dc.getServer( action , STATICSERVER );
		if( SUBORDINATESERVERS != null && !SUBORDINATESERVERS.isEmpty() ) {
			String[] SERVERS = Common.splitSpaced( SUBORDINATESERVERS );
			subordinateServers = new MetaEnvServer[ SERVERS.length ];
			for( int k = 0; k < SERVERS.length; k++ )
				subordinateServers[ k ] = dc.getServer( action , SERVERS[ k ] );
		}
		
		// verify aligned
		if( isDatabase( action ) ) {
			MetaDatabase database = meta.getDatabase( action );
			for( String id : Common.splitSpaced( ALIGNED ) )
				database.checkAligned( action , id );
		}
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
	
	private void loadNodes( ActionBase action , Node node , boolean loadProps ) throws Exception {
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_NODE );
		if( items == null )
			return;
		
		int pos = 1;
		for( Node snnode : items ) {
			MetaEnvServerNode sn = new MetaEnvServerNode( meta , this , pos );
			sn.load( action , snnode , loadProps );
			nodes.add( sn );
			pos++;
		}
	}
	
	private void loadBase( ActionBase action , Node node ) throws Exception {
		Node item = ConfReader.xmlGetFirstChild( node , ELEMENT_BASE );
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
			deployments.add( dp );
		}
	}

	public List<MetaEnvServerNode> getNodes( ActionBase action ) throws Exception {
		return( nodes );
	}

	public MetaEnvServerNode getNode( ActionBase action , int node ) throws Exception {
		if( node < 1 || node > nodes.size() )
			action.exit2( _Error.InvalidServerNode2 , "invalid node=" + node + ", server=" + NAME , NAME , "" + node );
		return( nodes.get( node - 1 ) );
	}

	public boolean hasWebServices( ActionBase action ) throws Exception {
		for( MetaEnvServerDeployment deploy : deployments ) {
			if( deploy.comp != null )
				if( deploy.comp.hasWebServices( action ) )
					return( true );
		}
		return( false );
	}

	public List<MetaEnvServerDeployment> getDeployments( ActionBase action ) throws Exception {
		return( deployments );
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

	public List<MetaEnvServerNode> getNodes( ActionBase action , String[] NODES ) throws Exception {
		List<MetaEnvServerNode> list = new LinkedList<MetaEnvServerNode>();
		for( String NODE : NODES ) {
			MetaEnvServerNode node = getNode( action , Integer.parseInt( NODE ) );
			list.add( node );
		}
		return( list );
	}
	
	public boolean isConfigurable( ActionBase action ) throws Exception {
		if( serverAccessType == VarSERVERACCESSTYPE.MANUAL || serverAccessType == VarSERVERACCESSTYPE.UNKNOWN )
			return( false );
		if( serverRunType == VarSERVERRUNTYPE.DATABASE || serverRunType == VarSERVERRUNTYPE.UNKNOWN ) 
			return( false );
		return( true );
	}
	
	public boolean hasConfiguration( ActionBase action ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.confItem != null )
				return( true );
			if( deployment.comp != null )
				if( deployment.comp.hasConfItems( action ) )
					return( true );
		}
		return( false );
	}

	public boolean hasConfItemDeployment( ActionBase action , MetaDistrConfItem confItem ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.hasConfItemDeployment( action , confItem ) )
				return( true );
		}
		return( false );
	}

	public boolean hasBinaryItemDeployment( ActionBase action , MetaDistrBinaryItem binaryItem ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.hasBinaryItemDeployment( action , binaryItem ) )
				return( true );
		}
		return( false );
	}

	public boolean hasDatabaseItemDeployment( ActionBase action , MetaDatabaseDatagroup datagroup ) throws Exception {
		if( datagroupMap.containsKey( datagroup.NAME ) )
			return( true );
		return( false );
	}
	
	public boolean hasDatabaseItemDeployment( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		for( MetaDatabaseDatagroup datagroup : delivery.getDatabaseDatagroups( action ).values() ) {
			if( hasDatabaseItemDeployment( action , datagroup ) ) 
				return( true );
		}
		return( false );
	}
	
	public Map<String, MetaDistrConfItem> getConfItems( ActionBase action ) throws Exception {
		Map<String, MetaDistrConfItem> confs = new HashMap<String, MetaDistrConfItem>(); 
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.confItem != null ) {
				MetaDistrConfItem conf = deployment.confItem; 
				confs.put( conf.KEY , conf );
			}
			else
			if( deployment.comp != null ) {
				for( MetaDistrComponentItem item : deployment.comp.getConfItems( action ).values() ) {
					if( item.confItem != null ) {
						MetaDistrConfItem conf = item.confItem; 
						confs.put( conf.KEY , conf );
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
			String deployPath = deployment.getDeployPath( action );
			VarDEPLOYTYPE deployType = deployment.getDeployType( action );
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
				if( binary && deployment.comp.hasBinaryItems( action ) ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( meta , this , deployType , deployPath );
						locations.put( key , location );
					}
					
					for( MetaDistrComponentItem item : deployment.comp.mapBinaryItems.values() ) {
						if( item.binaryItem != null )
							location.addBinaryItem( action , deployment , item.binaryItem , item.DEPLOYNAME );
					}
				}
				
				if( conf && deployment.comp.hasConfItems( action ) ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( meta , this , deployType , deployPath );
						locations.put( key , location );
					}
					
					for( MetaDistrComponentItem item : deployment.comp.mapBinaryItems.values() ) {
						if( item.confItem != null )
							location.addConfItem( action , deployment , item.confItem );
					}
				}
			}
		}
		
		return( locations.values().toArray( new MetaEnvServerLocation[0] ) );
	}

	public boolean isDeployPossible( ActionBase action ) throws Exception {
		if( !isConfigurable( action ) ) {
			action.trace( "ignore due to server type=" + getServerTypeName( action ) );
			return( false );
		}
		
		// check deploy items
		if( deployments != null ) {
			for( MetaEnvServerDeployment item : deployments ) {
				if( !item.isManual( action ) )
					return( true );
			}
		}
		
		return( false );
	}

	public Map<String,MetaDatabaseSchema> getSchemaSet( ActionBase action ) throws Exception {
		Map<String,MetaDatabaseSchema> schemaMap = new HashMap<String,MetaDatabaseSchema>();
		for( MetaDatabaseDatagroup dg : datagroupMap.values() ) {
			for( MetaDatabaseSchema schema : dg.getSchemes( action ).values() )
				schemaMap.put( schema.SCHEMA , schema );
		}
		return( schemaMap );
	}

	public MetaEnvServerNode getStandbyNode( ActionBase action ) throws Exception {
		for( MetaEnvServerNode node : getNodes( action ) ) {
			if( node.STANDBY )
				return( node );
		}
		
		action.exit0( _Error.MissingStandbyNode0 , "unable to find standby node" );
		return( null );
	}

	public MetaEnvServerNode getActiveNode( ActionBase action ) throws Exception {
		for( MetaEnvServerNode node : getNodes( action ) ) {
			if( !node.OFFLINE )
				return( node );
		}
		
		action.exit0( _Error.MissingActiveNode0 , "unable to find active node" );
		return( null );
	}

	public boolean isWindows( ActionBase action ) throws Exception {
		return( osType == VarOSTYPE.WINDOWS );
	}

	public boolean isLinux( ActionBase action ) throws Exception {
		return( osType == VarOSTYPE.LINUX );
	}

	public boolean isDatabase( ActionBase action ) throws Exception {
		return( serverRunType == VarSERVERRUNTYPE.DATABASE );
	}

	public boolean isService( ActionBase action ) throws Exception {
		return( serverAccessType == VarSERVERACCESSTYPE.SERVICE );
	}

	public boolean isOffline( ActionBase action ) throws Exception {
		return( OFFLINE );
	}
	
	public boolean isCommand( ActionBase action ) throws Exception {
		return( serverRunType == VarSERVERRUNTYPE.COMMAND );
	}
	
	public boolean isGeneric( ActionBase action ) throws Exception {
		return( serverAccessType == VarSERVERACCESSTYPE.GENERIC );
	}

	public boolean isWebUser( ActionBase action ) throws Exception {
		return( serverRunType == VarSERVERRUNTYPE.WEBUI );
	}

	public boolean isCallable( ActionBase action ) throws Exception {
		if( OFFLINE )
			return( false );
		if( serverRunType == VarSERVERRUNTYPE.DATABASE ||
			serverRunType == VarSERVERRUNTYPE.APP ) 
			return( false );
		return( true );
			
	}

	public boolean isStartable( ActionBase action ) throws Exception {
		if( OFFLINE )
			return( false );
		if( serverAccessType == VarSERVERACCESSTYPE.MANUAL )
			return( false );
		return( true );
	}

	public String getSystemPath( ActionBase action ) throws Exception {
		if( isLinux( action ) && isService( action ) )
			return( "/etc/init.d" );
		
		return( Common.getPath( ROOTPATH , BINPATH ) );
	}
	
	public String getSystemFiles( ActionBase action ) throws Exception {
		if( isLinux( action ) && isService( action ) )
			return( SERVICENAME );
		
		if( isLinux( action ) )
			return( "server.*.sh" );
		
		return( "server.*.cmd" );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
	}
	
	public void createServer( ActionBase action , String NAME , VarOSTYPE osType , VarSERVERRUNTYPE runType , VarSERVERACCESSTYPE accessType ) throws Exception {
		this.NAME = NAME;
		this.serverRunType = runType;
		this.serverAccessType = accessType;
		this.osType = osType;
		this.OFFLINE = true;
		if( !super.initCreateStarted( dc.getProperties() ) )
			return;

		super.setStringProperty( PROPERTY_NAME , NAME );
		super.setStringProperty( PROPERTY_OSTYPE , Common.getEnumLower( osType ) );
		super.setStringProperty( PROPERTY_SERVERRUNTYPE , Common.getEnumLower( runType ) );
		super.setStringProperty( PROPERTY_SERVERACCESSTYPE , Common.getEnumLower( accessType ) );
		super.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
		super.finishProperties( action );
		super.initFinished();
		
		scatterProperties( action );
	}

	public void setOfflineStatus( ServerTransaction transaction , boolean OFFLINE ) throws Exception {
		this.OFFLINE = OFFLINE;
	}
	
}
