package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.PropertySet;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.Metadata.VarDBMSTYPE;
import ru.egov.urm.meta.Metadata.VarDEPLOYTYPE;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.shell.Account;

public class MetaEnvServer {

	public MetaEnvDC dc;
	
	public String NAME;
	public String XDOC;
	private String BASELINE;
	public boolean OFFLINE;
	
	private String SERVERTYPE;
	public VarSERVERTYPE serverType;
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
	public String ADMSCHEMA;
	public Map<String,MetaDatabaseDatagroup> datagroupMap;
	public MetaDatabaseSchema admSchema;
	
	public String ALIGNED;
	public String REGIONS;
	
	public VarOSTYPE osType;
	
	public PropertySet properties;

	public MetaEnvServerBase base;
	List<MetaEnvServerDeployment> deployments;
	List<MetaEnvServerNode> nodes;
	
	public boolean primary;
	public MetaEnvStartGroup startGroup;
	
	public MetaEnvServer( MetaEnvDC dc ) {
		this.dc = dc;
		this.primary = false;
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
	
	public void load( ActionBase action , Node node , boolean loadProps ) throws Exception {
		if( action.meta.distr != null )
			loadDeployments( action , node );
		
		properties = new PropertySet( "server" , dc.properties );
		properties.loadRawFromAttributes( action , node );
		scatterSystemProperties( action );
		if( loadProps ) {
			properties.loadRawFromElements( action , node );
			properties.moveRawAsStrings( action );
		}

		loadNodes( action , node , loadProps );
		loadBase( action , node );
	}

	public String[] getPropertyList( ActionBase action ) throws Exception {
		return( properties.getOwnProperties( action ) );
	}
	
	public String getPropertyValue( ActionBase action , String var ) throws Exception {
		return( properties.getPropertyAny( action , var ) );
	}
	
	public void resolveLinks( ActionBase action ) throws Exception {
		// check props
		if( ROOTPATH == null || ROOTPATH.isEmpty() ) {
			if( deployments != null && !deployments.isEmpty() )
				action.exit( "rootpath is empty, required for deployments server=" + NAME );
			if( isGeneric( action ) )
				action.exit( "rootpath is empty, required for generic server=" + NAME );
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
			for( String id : Common.splitSpaced( ALIGNED ) )
				action.meta.distr.database.checkAligned( action , id );
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
	
	public void setStartGroup( ActionBase action , MetaEnvStartGroup group ) throws Exception {
		primary = true;
		this.startGroup = group;
	}
	
	private void scatterSystemProperties( ActionBase action ) throws Exception {
		datagroupMap = new HashMap<String,MetaDatabaseDatagroup>(); 
		
		NAME = properties.getSystemRequiredStringProperty( action , "name" );
		action.trace( "load properties of server=" + NAME );
		
		BASELINE = properties.getSystemStringProperty( action , "configuration-baseline" , "" ); 
		if( BASELINE.equals( "default" ) )
			BASELINE = NAME;
		
		SERVERTYPE = properties.getSystemRequiredStringProperty( action , "type" );
		serverType = action.meta.getServerType( action , SERVERTYPE );
		osType = action.meta.getOSType( action , properties.getSystemStringProperty( action , "ostype" , "unix" ) );
		OFFLINE = properties.getSystemBooleanProperty( action , "offline" , false );
		XDOC = properties.getSystemPathProperty( action , "xdoc" , NAME );
		
		if( isStartable( action ) || isDeployPossible( action ) ) {
			ROOTPATH = properties.getSystemPathProperty( action , "rootpath" , "" );
		}
		
		if( isStartable( action ) ) {
			BINPATH = properties.getSystemPathProperty( action , "binpath" , "" );
			PORT = properties.getSystemIntProperty( action , "port" , 0 );
			NLBSERVER = properties.getSystemStringProperty( action , "nlbserver" , "" );
			PROXYSERVER = properties.getSystemStringProperty( action , "proxy-server" , "" );
			STATICSERVER = properties.getSystemStringProperty( action , "static-server" , "" );
			SUBORDINATESERVERS = properties.getSystemStringProperty( action , "subordinate-servers" , "" );
			STARTTIME = properties.getSystemIntProperty( action , "starttime" , 0 );
			STOPTIME = properties.getSystemIntProperty( action , "stoptime" , 0 );
			HOTDEPLOYPATH = properties.getSystemPathProperty( action , "hotdeploypath" , "" );
			HOTDEPLOYDATA = properties.getSystemStringProperty( action , "hotdeploydata" , "" );
			WEBDOMAIN = properties.getSystemStringProperty( action , "webdomain" , "" );
			WEBMAINURL = properties.getSystemStringProperty( action , "webmainurl" , "" );
			LOGPATH = properties.getSystemPathProperty( action , "logpath" , "" );
			LOGFILEPATH = properties.getSystemPathProperty( action , "logfilepath" , "" );
			NOPIDS = properties.getSystemBooleanProperty( action , "nopids" , false );
		}

		if( isService( action ) )
			SERVICENAME = properties.getSystemRequiredStringProperty( action , "servicename" );

		if( isDeployPossible( action ) ) {
			DEPLOYPATH = properties.getSystemPathProperty( action , "deploypath" , "" );
			LINKFROMPATH = properties.getSystemPathProperty( action , "linkfrompath" , "" );
			DEPLOYSCRIPT = properties.getSystemPathProperty( action , "deployscript" , "" );
		}
		
		if( isDatabase( action ) ) {
			dbType = action.meta.getDbmsType( action , properties.getSystemRequiredStringProperty( action , "dbmstype" ) );
			DBMSADDR = properties.getSystemRequiredStringProperty( action , "dbmsaddr" );
			DATAGROUPS = properties.getSystemRequiredStringProperty( action , "datagroups" );
			ALIGNED = properties.getSystemStringProperty( action , "aligned" , "" );
			REGIONS = properties.getSystemStringProperty( action , "regions" , "" );
			ADMSCHEMA = properties.getSystemStringProperty( action , "admschema" , "" );
			
			if( action.meta.distr != null ) {
				MetaDatabase database = action.meta.distr.database;
				for( String dg : Common.splitSpaced( DATAGROUPS ) ) {
					MetaDatabaseDatagroup datagroup = database.getDatagroup( action , dg );
					datagroupMap.put( datagroup.NAME , datagroup );
				}
				
				admSchema = database.getSchema( action , ADMSCHEMA );
			}
		}

		properties.finishRawProperties( action );
	}
	
	private void loadNodes( ActionBase action , Node node , boolean loadProps ) throws Exception {
		nodes = new LinkedList<MetaEnvServerNode>(); 
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "node" );
		if( items == null )
			return;
		
		int pos = 1;
		for( Node snnode : items ) {
			MetaEnvServerNode sn = new MetaEnvServerNode( this , pos );
			sn.load( action , snnode , loadProps );
			nodes.add( sn );
			pos++;
		}
	}
	
	private void loadBase( ActionBase action , Node node ) throws Exception {
		Node item = ConfReader.xmlGetFirstChild( action , node , "base" );
		if( item == null )
			return;
		
		base = new MetaEnvServerBase( this );
		base.load( action , item );
	}
		
	private void loadDeployments( ActionBase action , Node node ) throws Exception {
		deployments = new LinkedList<MetaEnvServerDeployment>(); 
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "deploy" );
		if( items == null )
			return;
		
		for( Node dpnode : items ) {
			MetaEnvServerDeployment dp = new MetaEnvServerDeployment( this );
			dp.load( action , dpnode );
			deployments.add( dp );
		}
	}

	public List<MetaEnvServerNode> getNodes( ActionBase action ) throws Exception {
		return( nodes );
	}

	public MetaEnvServerNode getNode( ActionBase action , int node ) throws Exception {
		if( node < 1 || node > nodes.size() )
			action.exit( "invalid node=" + node + ", server=" + NAME );
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
		action.checkRequired( !ROOTPATH.isEmpty() , "rootpath" );
		action.checkRequired( !BINPATH.isEmpty() , "binpath" );
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
			if( nodeAccount.HOSTLOGIN.equals( account.HOSTLOGIN ) ) {
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
		if( serverType == VarSERVERTYPE.GENERIC_COMMAND || 
			serverType == VarSERVERTYPE.GENERIC_SERVER ||
			serverType == VarSERVERTYPE.GENERIC_WEB ||
			serverType == VarSERVERTYPE.SERVICE ) 
			return( true );
		return( false );
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
		
		action.exit( "unable to find primary node of server=" + NAME );
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
						location = new MetaEnvServerLocation( this , deployType , deployPath );
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
						location = new MetaEnvServerLocation( this , deployType , deployPath );
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
						location = new MetaEnvServerLocation( this , deployType , deployPath );
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
						location = new MetaEnvServerLocation( this , deployType , deployPath );
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
		if( isDatabase( action ) || 
			serverType == VarSERVERTYPE.GENERIC_NOSSH ||
			serverType == VarSERVERTYPE.UNKNOWN ) {
			action.trace( "ignore due to server type=" + Common.getEnumLower( serverType ) );
			return( false );
		}
		
		// check deploy items
		if( deployments != null ) {
			for( MetaEnvServerDeployment item : deployments ) {
				if( !item.isManual( action ) )
					return( true );
			}
		}
		
		action.trace( "ignore due to empty non-manual server deployments" );
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
		
		action.exit( "unable to find standby node" );
		return( null );
	}

	public MetaEnvServerNode getActiveNode( ActionBase action ) throws Exception {
		for( MetaEnvServerNode node : getNodes( action ) ) {
			if( !node.OFFLINE )
				return( node );
		}
		
		action.exit( "unable to find active node" );
		return( null );
	}

	public boolean isWindows( ActionBase action ) throws Exception {
		return( osType == VarOSTYPE.WINDOWS );
	}

	public boolean isLinux( ActionBase action ) throws Exception {
		return( osType == VarOSTYPE.LINUX );
	}

	public boolean isDatabase( ActionBase action ) throws Exception {
		return( serverType == VarSERVERTYPE.SERVICE_DATABASE ||
				serverType == VarSERVERTYPE.GENERIC_DATABASE );
	}

	public boolean isService( ActionBase action ) throws Exception {
		return( serverType == VarSERVERTYPE.SERVICE ||
				serverType == VarSERVERTYPE.SERVICE_DATABASE  );
	}

	public boolean isCommand( ActionBase action ) throws Exception {
		return( serverType == VarSERVERTYPE.GENERIC_COMMAND );
	}
	
	public boolean isGeneric( ActionBase action ) throws Exception {
		if( serverType == VarSERVERTYPE.GENERIC_COMMAND || 
			serverType == VarSERVERTYPE.GENERIC_SERVER ||
			serverType == VarSERVERTYPE.GENERIC_WEB ||
			serverType == VarSERVERTYPE.GENERIC_DATABASE )
			return( true );
		return( false );
	}

	public boolean isGenericWeb( ActionBase action ) throws Exception {
		return( serverType == VarSERVERTYPE.GENERIC_WEB );
	}

	public boolean isCallable( ActionBase action ) throws Exception {
		if( serverType == VarSERVERTYPE.SERVICE ||
			serverType == VarSERVERTYPE.GENERIC_SERVER || 
			serverType == VarSERVERTYPE.GENERIC_COMMAND ||
			serverType == VarSERVERTYPE.GENERIC_NOSSH ) 
			return( true );
		return( false );
			
	}

	public boolean isStartable( ActionBase action ) throws Exception {
		if( serverType == VarSERVERTYPE.GENERIC_SERVER || 
			serverType == VarSERVERTYPE.GENERIC_WEB || 
			serverType == VarSERVERTYPE.GENERIC_COMMAND ||
			serverType == VarSERVERTYPE.GENERIC_DATABASE ||
			serverType == VarSERVERTYPE.SERVICE )
			return( true );
		return( false );
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
	
}
