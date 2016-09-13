package org.urm.server.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta.VarDBMSTYPE;
import org.urm.server.meta.Meta.VarDEPLOYTYPE;
import org.urm.server.meta.Meta.VarSERVERTYPE;
import org.urm.server.shell.Account;
import org.w3c.dom.Node;

public class MetaEnvServer {

	public Meta meta;
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
	
	public MetaEnvServer( Meta meta , MetaEnvDC dc ) {
		this.meta = meta;
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
		if( meta.distr != null )
			loadDeployments( action , node );
		
		properties = new PropertySet( "server" , dc.properties );
		properties.loadRawFromNodeAttributes( node );
		scatterSystemProperties( action );
		
		if( loadProps ) {
			properties.loadRawFromNodeElements( node );
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
			for( String id : Common.splitSpaced( ALIGNED ) )
				meta.database.checkAligned( action , id );
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
		
		NAME = properties.getSystemRequiredStringProperty( "name" );
		action.trace( "load properties of server=" + NAME );
		
		BASELINE = properties.getSystemStringProperty( "configuration-baseline" , "" ); 
		if( BASELINE.equals( "default" ) )
			BASELINE = NAME;
		
		SERVERTYPE = properties.getSystemRequiredStringProperty( "type" );
		serverType = meta.getServerType( SERVERTYPE );
		osType = meta.getOSType( properties.getSystemStringProperty( "ostype" , "unix" ) );
		OFFLINE = properties.getSystemBooleanProperty( "offline" , false );
		XDOC = properties.getSystemPathProperty( "xdoc" , NAME , action.session.execrc );
		
		if( isStartable( action ) || isDeployPossible( action ) ) {
			ROOTPATH = properties.getSystemPathProperty( "rootpath" , "" , action.session.execrc );
		}
		
		if( isStartable( action ) ) {
			BINPATH = properties.getSystemPathProperty( "binpath" , "" , action.session.execrc );
			PORT = properties.getSystemIntProperty( "port" , 0 );
			NLBSERVER = properties.getSystemStringProperty( "nlbserver" , "" );
			PROXYSERVER = properties.getSystemStringProperty( "proxy-server" , "" );
			STATICSERVER = properties.getSystemStringProperty( "static-server" , "" );
			SUBORDINATESERVERS = properties.getSystemStringProperty( "subordinate-servers" , "" );
			STARTTIME = properties.getSystemIntProperty( "starttime" , 0 );
			STOPTIME = properties.getSystemIntProperty( "stoptime" , 0 );
			HOTDEPLOYPATH = properties.getSystemPathProperty( "hotdeploypath" , "" , action.session.execrc );
			HOTDEPLOYDATA = properties.getSystemStringProperty( "hotdeploydata" , "" );
			WEBDOMAIN = properties.getSystemStringProperty( "webdomain" , "" );
			WEBMAINURL = properties.getSystemStringProperty( "webmainurl" , "" );
			LOGPATH = properties.getSystemPathProperty( "logpath" , "" , action.session.execrc );
			LOGFILEPATH = properties.getSystemPathProperty( "logfilepath" , "" , action.session.execrc );
			NOPIDS = properties.getSystemBooleanProperty( "nopids" , false );
		}

		if( isService( action ) )
			SERVICENAME = properties.getSystemRequiredStringProperty( "servicename" );

		if( isDeployPossible( action ) ) {
			DEPLOYPATH = properties.getSystemPathProperty( "deploypath" , "" , action.session.execrc );
			LINKFROMPATH = properties.getSystemPathProperty( "linkfrompath" , "" , action.session.execrc );
			DEPLOYSCRIPT = properties.getSystemPathProperty( "deployscript" , "" , action.session.execrc );
		}
		
		if( isDatabase( action ) ) {
			dbType = meta.getDbmsType( properties.getSystemRequiredStringProperty( "dbmstype" ) );
			DBMSADDR = properties.getSystemRequiredStringProperty( "dbmsaddr" );
			DATAGROUPS = properties.getSystemRequiredStringProperty( "datagroups" );
			ALIGNED = properties.getSystemStringProperty( "aligned" , "" );
			REGIONS = properties.getSystemStringProperty( "regions" , "" );
			ADMSCHEMA = properties.getSystemStringProperty( "admschema" , "" );
			
			if( meta.distr != null ) {
				MetaDatabase database = action.meta.database;
				for( String dg : Common.splitSpaced( DATAGROUPS ) ) {
					MetaDatabaseDatagroup datagroup = database.getDatagroup( action , dg );
					datagroupMap.put( datagroup.NAME , datagroup );
				}
				
				admSchema = database.getSchema( action , ADMSCHEMA );
			}
		}

		properties.finishRawProperties();
	}
	
	private void loadNodes( ActionBase action , Node node , boolean loadProps ) throws Exception {
		nodes = new LinkedList<MetaEnvServerNode>(); 
		
		Node[] items = ConfReader.xmlGetChildren( node , "node" );
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
		Node item = ConfReader.xmlGetFirstChild( node , "base" );
		if( item == null )
			return;
		
		base = new MetaEnvServerBase( meta , this );
		base.load( action , item );
	}
		
	private void loadDeployments( ActionBase action , Node node ) throws Exception {
		deployments = new LinkedList<MetaEnvServerDeployment>(); 
		
		Node[] items = ConfReader.xmlGetChildren( node , "deploy" );
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
		if( serverType == VarSERVERTYPE.GENERIC_NOSSH ||
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
		return( serverType == VarSERVERTYPE.SERVICE_DATABASE ||
				serverType == VarSERVERTYPE.GENERIC_DATABASE );
	}

	public boolean isService( ActionBase action ) throws Exception {
		return( serverType == VarSERVERTYPE.SERVICE ||
				serverType == VarSERVERTYPE.SERVICE_DATABASE  );
	}

	public boolean isOffline( ActionBase action ) throws Exception {
		return( OFFLINE ||
				serverType == VarSERVERTYPE.OFFLINE );
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
		if( OFFLINE )
			return( false );
		if( serverType == VarSERVERTYPE.SERVICE ||
			serverType == VarSERVERTYPE.GENERIC_SERVER || 
			serverType == VarSERVERTYPE.GENERIC_COMMAND ||
			serverType == VarSERVERTYPE.GENERIC_NOSSH ) 
			return( true );
		return( false );
			
	}

	public boolean isStartable( ActionBase action ) throws Exception {
		if( OFFLINE )
			return( false );
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
