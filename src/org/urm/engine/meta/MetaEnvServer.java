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
import org.urm.engine.meta.Meta.VarSERVERTYPE;
import org.urm.engine.shell.Account;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServer extends PropertyController {

	public Meta meta;
	public MetaEnvDC dc;
	
	public String NAME;
	public String XDOC;
	public String BASELINE;
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
	
	public MetaEnvServerBase base;
	List<MetaEnvServerDeployment> deployments;
	List<MetaEnvServerNode> nodes;
	
	public boolean primary;
	public MetaEnvStartGroup startGroup;
	
	public MetaEnvServer( Meta meta , MetaEnvDC dc ) {
		super( "server" );
		this.meta = meta;
		this.dc = dc;
		this.primary = false;
		
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
		
		NAME = properties.getSystemRequiredStringProperty( "name" );
		action.trace( "load properties of server=" + NAME );
		
		BASELINE = super.getStringProperty( action , "configuration-baseline" ); 
		if( BASELINE.equals( "default" ) )
			BASELINE = NAME;
		
		SERVERTYPE = super.getStringPropertyRequired( action , "type" );
		serverType = Meta.getServerType( SERVERTYPE );
		osType = Meta.getOSType( super.getStringPropertyRequired( action , "ostype" , "unix" ) );
		OFFLINE = super.getBooleanProperty( action , "offline" );
		XDOC = super.getPathProperty( action , "xdoc" , NAME + ".xml" );
		
		if( isStartable( action ) || isDeployPossible( action ) ) {
			ROOTPATH = super.getPathProperty( action , "rootpath" );
		}
		
		if( isStartable( action ) ) {
			BINPATH = super.getPathProperty( action , "binpath" );
			PORT = super.getIntProperty( action , "port" , 0 );
			NLBSERVER = super.getStringProperty( action , "nlbserver" );
			PROXYSERVER = super.getStringProperty( action , "proxy-server" , "" );
			STATICSERVER = super.getStringProperty( action , "static-server" , "" );
			SUBORDINATESERVERS = super.getStringProperty( action , "subordinate-servers" , "" );
			STARTTIME = super.getIntProperty( action , "starttime" , 0 );
			STOPTIME = super.getIntProperty( action , "stoptime" , 0 );
			HOTDEPLOYPATH = super.getPathProperty( action , "hotdeploypath" );
			HOTDEPLOYDATA = super.getStringProperty( action , "hotdeploydata" );
			WEBDOMAIN = super.getStringProperty( action , "webdomain" );
			WEBMAINURL = super.getStringProperty( action , "webmainurl" );
			LOGPATH = super.getPathProperty( action , "logpath" );
			LOGFILEPATH = super.getPathProperty( action , "logfilepath" );
			NOPIDS = super.getBooleanProperty( action , "nopids" );
		}

		if( isService( action ) )
			SERVICENAME = super.getStringPropertyRequired( action , "servicename" );

		if( isDeployPossible( action ) ) {
			DEPLOYPATH = super.getPathProperty( action , "deploypath" );
			LINKFROMPATH = super.getPathProperty( action , "linkfrompath" );
			DEPLOYSCRIPT = super.getPathProperty( action , "deployscript" );
		}
		
		if( isDatabase( action ) ) {
			dbType = Meta.getDbmsType( super.getStringPropertyRequired( action , "dbmstype" ) );
			DBMSADDR = super.getStringPropertyRequired( action , "dbmsaddr" );
			DATAGROUPS = super.getStringPropertyRequired( action , "datagroups" );
			ALIGNED = super.getStringProperty( action , "aligned" );
			REGIONS = super.getStringProperty( action , "regions" );
			ADMSCHEMA = super.getStringProperty( action , "admschema" );
			
			MetaDatabase database = meta.getDatabase( action );
			for( String dg : Common.splitSpaced( DATAGROUPS ) ) {
				MetaDatabaseDatagroup datagroup = database.getDatagroup( action , dg );
				datagroupMap.put( datagroup.NAME , datagroup );
			}
			
			admSchema = database.getSchema( action , ADMSCHEMA );
		}

		properties.finishRawProperties();
	}
	
	@Override
	public void gatherProperties( ActionBase action ) throws Exception {
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
		primary = true;
		this.startGroup = group;
	}
	
	private void loadNodes( ActionBase action , Node node , boolean loadProps ) throws Exception {
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

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
	}
	
	public void createServer( ActionBase action ) throws Exception {
		if( !super.initCreateStarted( dc.getProperties() ) )
			return;

		gatherProperties( action );
		super.finishProperties( action );
		super.initFinished();
		
		scatterProperties( action );
		
		base = new MetaEnvServerBase( meta , this );
	}

	public void setOfflineStatus( ServerTransaction transaction , boolean OFFLINE ) throws Exception {
		this.OFFLINE = OFFLINE;
	}
	
}
