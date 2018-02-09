package org.urm.meta.env;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.shell.Account;
import org.urm.meta.engine.AccountReference;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.urm.meta.MatchItem;

public class MetaEnvServer extends EngineObject {

	// properties
	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_BASELINE = "baseserver";
	public static String PROPERTY_OSTYPE = "ostype";
	public static String PROPERTY_SERVERRUNTYPE = "runtype";
	public static String PROPERTY_SERVERACCESSTYPE = "accesstype";
	public static String PROPERTY_SYSNAME = "sysname";
	public static String PROPERTY_OFFLINE = "offline";
	public static String PROPERTY_BASEITEM = "baseitem";
	
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
	
	public Meta meta;
	public MetaEnvSegment sg;
	
	// table data
	private ObjectProperties ops;
	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumServerRunType SERVERRUN_TYPE;
	public DBEnumServerAccessType SERVERACCESS_TYPE;
	public DBEnumOSType OS_TYPE;
	private MatchItem BASELINE;
	public boolean OFFLINE;
	public DBEnumDbmsType DBMS_TYPE;
	private MatchItem DATABASE_ADMSCHEMA;
	private MatchItem BASEITEM;
	public int EV;
	
	// properties
	public String XDOC;
	public String ROOTPATH;
	public String BINPATH;
	public String SYSNAME;
	public int PORT;
	public int STARTTIME;
	public int STOPTIME;
	public String DEPLOYPATH;
	public String LINKFROMPATH;
	public String DEPLOYSCRIPT;
	public String HOTDEPLOYPATH;
	public String HOTDEPLOYDATA;
	public String WEBSERVICEURL;
	public String WEBMAINURL;
	public String LOGPATH;
	public String LOGFILEPATH;
	public boolean NOPIDS;
	public String DBMSADDR;
	public String ALIGNED = "";
	public String REGIONS = "";
	
	// dependencies
	private MetaEnvServer nlbServer;
	private MetaEnvServer proxyServer;
	private MetaEnvServer staticServer;
	private List<MetaEnvServer> subordinateServers;

	private Map<Integer,MetaEnvServerDeployment> deployMapById;
	private List<MetaEnvServerDeployment> deployments;
	private List<MetaEnvServerNode> nodes;

	private MetaEnvStartGroup startGroup;
	
	public MetaEnvServer( Meta meta , MetaEnvSegment sg ) {
		super( sg );
		this.meta = meta;
		this.sg = sg;

		ID = -1;
		EV = -1;
		subordinateServers = new LinkedList<MetaEnvServer>();
		deployments = new LinkedList<MetaEnvServerDeployment>(); 
		deployMapById = new HashMap<Integer,MetaEnvServerDeployment>();
		nodes = new LinkedList<MetaEnvServerNode>();
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public MetaEnvServer copy( Meta rmeta , MetaEnvSegment rsg ) throws Exception {
		MetaEnvServer r = new MetaEnvServer( rmeta , rsg );

		ProductEnvs renvs = rmeta.getEnviroments();

		// primary
		r.ops = ops.copy( rsg.getProperties() );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.SERVERRUN_TYPE = SERVERRUN_TYPE;
		r.SERVERACCESS_TYPE = SERVERACCESS_TYPE;
		r.OS_TYPE = OS_TYPE;
		r.BASELINE = MatchItem.copy( BASELINE );
		r.OFFLINE = false;
		r.DBMS_TYPE = DBMS_TYPE;
		r.DATABASE_ADMSCHEMA = MatchItem.copy( DATABASE_ADMSCHEMA );
		r.BASEITEM = MatchItem.copy( BASEITEM );
		r.EV = EV;

		// properties
		refreshPrimaryProperties();
		scatterExtraProperties();
		
		// children
		r.nlbServer = renvs.findMetaEnvServer( nlbServer );
		r.proxyServer = renvs.findMetaEnvServer( proxyServer );
		r.staticServer = renvs.findMetaEnvServer( staticServer );
		
		for( MetaEnvServer server : subordinateServers ) {
			MetaEnvServer rserver = renvs.findMetaEnvServer( server );
			r.subordinateServers.add( rserver );
		}
			
		for( MetaEnvServerDeployment deployment : deployments ) {
			MetaEnvServerDeployment rdeployment = deployment.copy( rmeta , r );
			r.addDeployment( rdeployment );
		}
		for( MetaEnvServerNode node : nodes ) {
			MetaEnvServerNode rnode = node.copy( rmeta , r );
			r.addNodeInternal( rnode );
		}
		
		return( r );
	}
	
	public void createSettings( ObjectProperties ops ) throws Exception {
		this.ops = ops;
	}
	
	public ObjectProperties getProperties() {
		return( ops );
	}

	public boolean checkMatched() {
		if( !MatchItem.isMatched( BASELINE ) )
			return( false );
		if( !MatchItem.isMatched( DATABASE_ADMSCHEMA ) )
			return( false );
		if( !MatchItem.isMatched( BASEITEM ) )
			return( false );
		
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( !deployment.checkMatched() )
				return( false );
		}
		for( MetaEnvServerNode node : nodes ) {
			if( !node.checkMatched() )
				return( false );
		}
		
		return( true );
	}

	public void setServerPrimary( String name , String desc , DBEnumServerRunType runType , DBEnumServerAccessType accessType , DBEnumOSType osType , 
			MatchItem baselineMatchItem , boolean offline , DBEnumDbmsType dbmsType , MatchItem admSchemaMatchItem , MatchItem baseItemMatchItem ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.SERVERRUN_TYPE = runType;
		this.SERVERACCESS_TYPE = accessType;
		this.OS_TYPE = osType;
		this.BASELINE = MatchItem.copy( baselineMatchItem );
		this.OFFLINE = offline;
		this.DBMS_TYPE = dbmsType;
		this.DATABASE_ADMSCHEMA = MatchItem.copy( admSchemaMatchItem );
		this.BASEITEM = MatchItem.copy( baseItemMatchItem );
	}
	
	private void refreshPrimaryProperties() throws Exception {
		ops.clearProperties( DBEnumParamEntityType.SERVER );
		
		ops.setStringProperty( PROPERTY_NAME , NAME );
		ops.setStringProperty( PROPERTY_DESC , DESC );
		ops.setEnumProperty( PROPERTY_SERVERRUNTYPE , SERVERRUN_TYPE );
		ops.setEnumProperty( PROPERTY_SERVERACCESSTYPE , SERVERACCESS_TYPE );
		ops.setEnumProperty( PROPERTY_OSTYPE , OS_TYPE );
		
		MetaEnvServer serverBaseline = getBaseline();
		if( serverBaseline != null )
			ops.setStringProperty( PROPERTY_BASELINE , serverBaseline.NAME );

		ops.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
		ops.setEnumProperty( PROPERTY_DBMSTYPE , DBMS_TYPE );
		
		MetaDatabaseSchema admSchema = getAdmSchema();
		if( admSchema != null )
			ops.setStringProperty( PROPERTY_ADMSCHEMA , admSchema.NAME );

		BaseItem baseItem = getBaseItem();
		if( baseItem != null )
			ops.setStringProperty( PROPERTY_BASEITEM , baseItem.NAME );
	}
		
	public void scatterExtraProperties() throws Exception {
		XDOC = "";
		ROOTPATH = "";
		BINPATH = "";
		SYSNAME = "";
		PORT = 0;
		STARTTIME = 0;
		STOPTIME = 0;
		DEPLOYPATH = "";
		LINKFROMPATH = "";
		DEPLOYSCRIPT = "";
		HOTDEPLOYPATH = "";
		HOTDEPLOYDATA = "";
		WEBSERVICEURL = "";
		WEBMAINURL = "";
		LOGPATH = "";
		LOGFILEPATH = "";
		NOPIDS = false;
		DBMSADDR = "";
		ALIGNED = "";
		REGIONS = "";
		
		XDOC = ops.getPathProperty( PROPERTY_XDOC );
		SYSNAME = ops.getStringProperty( PROPERTY_SYSNAME );
		
		if( isStartable() || isDeployPossible() ) {
			ROOTPATH = ops.getPathProperty( PROPERTY_ROOTPATH );
		}
		
		if( isStartable() ) {
			BINPATH = ops.getPathProperty( PROPERTY_BINPATH );
			PORT = ops.getIntProperty( PROPERTY_PORT );
			STARTTIME = ops.getIntProperty( PROPERTY_STARTTIME );
			STOPTIME = ops.getIntProperty( PROPERTY_STOPTIME );
			HOTDEPLOYPATH = ops.getPathProperty( PROPERTY_HOTDEPLOYPATH );
			HOTDEPLOYDATA = ops.getStringProperty( PROPERTY_HOTDEPLOYDATA );
			WEBSERVICEURL = ops.getStringProperty( PROPERTY_WEBSERVICEURL );
			WEBMAINURL = ops.getStringProperty( PROPERTY_WEBMAINURL );
			LOGPATH = ops.getPathProperty( PROPERTY_LOGPATH );
			LOGFILEPATH = ops.getPathProperty( PROPERTY_LOGFILEPATH );
			NOPIDS = ops.getBooleanProperty( PROPERTY_NOPIDS );
		}

		if( isDeployPossible() ) {
			DEPLOYPATH = ops.getPathProperty( PROPERTY_DEPLOYPATH );
			LINKFROMPATH = ops.getPathProperty( PROPERTY_LINKFROMPATH );
			DEPLOYSCRIPT = ops.getPathProperty( PROPERTY_DEPLOYSCRIPT );
		}
		
		if( isRunDatabase() ) {
			DBMS_TYPE = DBEnumDbmsType.getValue( ops.getEnumProperty( PROPERTY_DBMSTYPE ) , false );
			DBMSADDR = ops.getStringProperty( PROPERTY_DBMSADDR );
			ALIGNED = ops.getStringProperty( PROPERTY_ALIGNED );
			REGIONS = ops.getStringProperty( PROPERTY_REGIONS );
		}
	}
	
	public DBEnumServerRunType getServerRunType() {
		return( SERVERRUN_TYPE );
	}
	
	public DBEnumServerAccessType getServerAccessType() {
		return( SERVERACCESS_TYPE );
	}
	
	public DBEnumDbmsType getServerDbmsType() {
		return( DBMS_TYPE );
	}
	
	public String getServerTypeName() {
		return( Common.getEnumLower( SERVERRUN_TYPE ) + "/" + Common.getEnumLower( SERVERACCESS_TYPE ) );
	}
	
	public String getEnvObjectName() {
		return( sg.getEnvObjectName() + "::" + NAME );
	}
	
	public boolean hasBaseline() {
		if( BASELINE == null )
			return( false );
		return( true );
	}
	
	public MetaEnvServer getBaseline() throws Exception {
		MetaEnvSegment segmentBaseline = sg.getBaseline();
		if( segmentBaseline == null )
			return( null );
		
		return( segmentBaseline.getServer( BASELINE ) );
	}

	public Map<String,MetaEnvServer> getAssociatedServers() throws Exception {
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
	
	public void setStartGroup( MetaEnvStartGroup group ) {
		this.startGroup = group;
	}
	
	private void addNodeInternal( MetaEnvServerNode sn ) {
		nodes.add( sn );
	}

	private void addDeployment( MetaEnvServerDeployment dp ) {
		deployments.add( dp );
		deployMapById.put( dp.ID , dp );
	}
	
	public MetaEnvServerNode[] getNodes() {
		return( nodes.toArray( new MetaEnvServerNode[0] ) );
	}

	public MetaEnvServerNode findNode( int pos ) {
		if( pos < 1 || pos > nodes.size() )
			return( null );
		return( nodes.get( pos - 1 ) );
	}
	
	public MetaEnvServerNode getNodeByPos( int pos ) throws Exception {
		if( pos < 1 || pos > nodes.size() )
			Common.exit2( _Error.InvalidServerNode2 , "invalid node=" + pos + ", server=" + NAME , NAME , "" + pos );
		return( nodes.get( pos - 1 ) );
	}

	public boolean hasWebServices() throws Exception {
		for( MetaEnvServerDeployment deploy : deployments ) {
			MetaDistrComponent comp = deploy.getComponent();
			if( comp != null )
				if( comp.hasWebServices() )
					return( true );
		}
		return( false );
	}

	public MetaEnvServerDeployment[] getDeployments() {
		return( deployments.toArray( new MetaEnvServerDeployment[0] ) );
	}

	public String getFullBinPath() throws Exception {
		if( ROOTPATH.isEmpty() )
			Common.exitUnexpected();
		if( BINPATH.isEmpty() )
			Common.exitUnexpected();
		return( Common.getPath( ROOTPATH , BINPATH ) );
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

	public MetaEnvServerNode[] getNodes( ActionBase action , String[] nodes ) throws Exception {
		List<MetaEnvServerNode> list = new LinkedList<MetaEnvServerNode>();
		for( String pos : nodes ) {
			MetaEnvServerNode node = getNodeByPos( Integer.parseInt( pos ) );
			list.add( node );
		}
		return( list.toArray( new MetaEnvServerNode[0] ) );
	}
	
	public boolean isConfigurable() {
		if( isAccessManual() || SERVERACCESS_TYPE == DBEnumServerAccessType.UNKNOWN )
			return( false );
		if( isRunDatabase() || SERVERRUN_TYPE == DBEnumServerRunType.UNKNOWN ) 
			return( false );
		return( true );
	}
	
	public boolean hasConfiguration() throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.isConfItem() )
				return( true );
			
			if( deployment.isComponent() ) {
				MetaDistrComponent comp = deployment.getComponent();
				if( comp.hasConfItems() )
					return( true );
			}
		}
		return( false );
	}

	public boolean hasConfItemDeployment( MetaDistrConfItem confItem ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.hasConfItemDeployment( confItem ) )
				return( true );
		}
		return( false );
	}

	public boolean hasBinaryItemDeployment( MetaDistrBinaryItem binaryItem ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.hasBinaryItemDeployment( binaryItem ) )
				return( true );
		}
		return( false );
	}

	public boolean hasDatabaseItemDeployment( MetaDistrDelivery delivery ) throws Exception {
		for( MetaDatabaseSchema schema : delivery.getDatabaseSchemes() ) {
			if( hasDatabaseItemDeployment( schema ) )
				return( true );
		}
		return( false );
	}

	public boolean hasDatabaseItemDeployment( MetaDatabaseSchema schema ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.hasDatabaseItemDeployment( schema ) ) 
				return( true );
		}
		return( false );
	}
	
	public MetaDistrConfItem[] getConfItems() throws Exception {
		Map<String, MetaDistrConfItem> confs = new HashMap<String, MetaDistrConfItem>(); 
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.isConfItem() ) {
				MetaDistrConfItem conf = deployment.getConfItem(); 
				confs.put( conf.NAME , conf );
			}
			else
			if( deployment.isComponent() ) {
				MetaDistrComponent comp = deployment.getComponent();
				for( MetaDistrComponentItem item : comp.getConfItems() ) {
					if( item.confItem != null ) {
						MetaDistrConfItem conf = item.confItem; 
						confs.put( conf.NAME , conf );
					}
				}
			}
		}
		
		MetaDistrConfItem[] list = new MetaDistrConfItem[ confs.size() ];
		String[] names = Common.getSortedKeys( confs );
		for( int k = 0; k < names.length; k++ )
			list[ k ] = confs.get( names[ k ] );
		
		return( list );
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
			DBEnumDeployModeType deployType = deployment.DEPLOYMODE_TYPE;
			String key = Common.getEnumLower( deployType ) + "-" + deployPath;
			
			if( deployment.isBinaryItem() ) {
				if( binary ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( meta , this , deployType , deployPath );
						locations.put( key , location );
					}
					
					MetaDistrBinaryItem binaryItem = deployment.getBinaryItem();
					location.addBinaryItem( deployment , binaryItem , "" );
				}
			}
			else
			if( deployment.isConfItem() ) {
				if( conf ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( meta , this , deployType , deployPath );
						locations.put( key , location );
					}
					
					MetaDistrConfItem confItem = deployment.getConfItem();
					location.addConfItem( deployment , confItem );
				}
			}
			else
			if( deployment.isComponent() ) {
				MetaDistrComponent comp = deployment.getComponent();
				if( binary && comp.hasBinaryItems() ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( meta , this , deployType , deployPath );
						locations.put( key , location );
					}
					
					for( MetaDistrComponentItem item : comp.getBinaryItems() ) {
						if( item.binaryItem != null )
							location.addBinaryItem( deployment , item.binaryItem , item.DEPLOY_NAME );
					}
				}
				
				if( conf && comp.hasConfItems() ) {
					MetaEnvServerLocation location = locations.get( key ); 
					if( location == null ) {
						location = new MetaEnvServerLocation( meta , this , deployType , deployPath );
						locations.put( key , location );
					}
					
					for( MetaDistrComponentItem item : comp.getConfItems() ) {
						if( item.confItem != null )
							location.addConfItem( deployment , item.confItem );
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

	public MetaDatabaseSchema[] getSchemaSet() throws Exception {
		Map<String,MetaDatabaseSchema> schemaMap = new HashMap<String,MetaDatabaseSchema>();
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.isDatabase() ) {
				MetaDatabaseSchema schema = deployment.getSchema();
				schemaMap.put( schema.NAME , schema );
			}
			else
			if( deployment.isComponent() ) {
				MetaDistrComponent comp = deployment.getComponent();
				for( MetaDistrComponentItem compItem : comp.getSchemaItems() )
					schemaMap.put( compItem.schema.NAME , compItem.schema );
			}
		}
		
		MetaDatabaseSchema[] list = new MetaDatabaseSchema[ schemaMap.size() ];
		String[] names = Common.getSortedKeys( schemaMap );
		for( int k = 0; k < names.length; k++ )
			list[ k ] = schemaMap.get( names[ k ] );
		
		return( list );
	}

	public MetaEnvServerNode getStandbyNode() throws Exception {
		for( MetaEnvServerNode node : getNodes() ) {
			if( node.OFFLINE )
				continue;
			
			if( node.DBSTANDBY )
				return( node );
		}
		
		Common.exit0( _Error.MissingStandbyNode0 , "unable to find active standby node" );
		return( null );
	}

	public MetaEnvServerNode getMasterNode() throws Exception {
		for( MetaEnvServerNode node : getNodes() ) {
			if( node.OFFLINE )
				continue;
			
			if( node.isAdmin() )
				return( node );
		}
		
		Common.exit0( _Error.MissingActiveNode0 , "unable to find active master node" );
		return( null );
	}

	public boolean isWindows() {
		return( OS_TYPE == DBEnumOSType.WINDOWS );
	}

	public boolean isLinux() {
		return( OS_TYPE == DBEnumOSType.LINUX );
	}

	public boolean isRunDatabase() {
		return( SERVERRUN_TYPE == DBEnumServerRunType.DATABASE );
	}

	public boolean isAccessService() {
		return( SERVERACCESS_TYPE == DBEnumServerAccessType.SERVICE );
	}

	public boolean isAccessDocker() {
		return( SERVERACCESS_TYPE == DBEnumServerAccessType.DOCKER );
	}

	public boolean isAccessManual() {
		if( SERVERACCESS_TYPE == DBEnumServerAccessType.MANUAL )
			return( true );
		return( false );
	}
	
	public boolean isAccessPacemaker() {
		return( SERVERACCESS_TYPE == DBEnumServerAccessType.PACEMAKER );
	}

	public boolean isRunCommand() {
		return( SERVERRUN_TYPE == DBEnumServerRunType.COMMAND );
	}
	
	public boolean isAccessGeneric() {
		return( SERVERACCESS_TYPE == DBEnumServerAccessType.GENERIC );
	}

	public boolean isRunWebUser() {
		return( SERVERRUN_TYPE == DBEnumServerRunType.WEBUI );
	}

	public boolean isRunWebApp() {
		return( SERVERRUN_TYPE == DBEnumServerRunType.WEBAPP );
	}

	public boolean isRunGenericApp() {
		return( SERVERRUN_TYPE == DBEnumServerRunType.APP );
	}

	public boolean isCallable() {
		if( isRunDatabase() || isRunGenericApp() ) 
			return( false );
		return( true );
	}

	public boolean isStartable() {
		if( isAccessManual() )
			return( false );
		return( true );
	}

	public MetaDatabaseSchema getAdmSchema() throws Exception {
		MetaDatabase db = meta.getDatabase();
		return( db.getSchema( DATABASE_ADMSCHEMA ) );
	}

	public BaseItem getBaseItem() throws Exception {
		EngineData data = meta.getEngineData();
		EngineBase base = data.getEngineBase();
		return( base.getItem( BASEITEM ) );
	}
	
	public MetaEnvStartGroup getStartGroup() {
		return( startGroup );
	}

	public MetaEnvServer getProxyServer() {
		return( proxyServer );
	}
			
	public MetaEnvServer getNlbServer() {
		return( nlbServer );
	}
	
	public MetaEnvServer getStaticServer() {
		return( staticServer );
	}
	
	public MetaEnvServer[] getSubordinateServers() {
		return( subordinateServers.toArray( new MetaEnvServer[0] ) );
	}
	
	public void setBaseline( MetaEnv server ) throws Exception {
		if( server == null ) {
			this.BASELINE = null;
			return;
		}
			
		this.BASELINE = new MatchItem( server.ID );
		refreshPrimaryProperties();
	}
	
	public void setPlatform( BaseItem item ) throws Exception {
		if( item == null ) {
			this.BASEITEM = null;
			return;
		}
		
		this.BASEITEM = new MatchItem( item.ID );
		refreshPrimaryProperties();
	}
	
	public void setOffline( boolean offline ) throws Exception {
		// check props
		if( !offline ) {
			if( ROOTPATH == null || ROOTPATH.isEmpty() ) {
				if( deployments != null && !deployments.isEmpty() )
					Common.exit1( _Error.RootpathEmptyRequiredForDeployments1 , "rootpath is empty, required for deployments server=" + NAME , NAME );
				if( isAccessGeneric() )
					Common.exit1( _Error.RootpathEmptyRequiredForGeneric1 , "rootpath is empty, required for generic server=" + NAME , NAME );
			}
		}

		this.OFFLINE = offline;
		refreshPrimaryProperties();
	}

	public void addNode( MetaEnvServerNode node ) throws Exception {
		int index = nodes.size();
		if( node.POS > 0 )
			index = node.POS - 1;
		else
			node.setPos( index + 1 );
		
		if( index >= nodes.size() ) {
			addNodeInternal( node );
			return;
		}
		
		// add and shift tail nodes
		nodes.add( index , node );
		for( int k = index + 1; k < nodes.size(); k++ ) {
			MetaEnvServerNode item = nodes.get( k );
			item.setPos( k + 1 );
		}
	}
	
	public void removeNode( MetaEnvServerNode node ) throws Exception {
		int index = nodes.indexOf( node );
		if( index < 0 )
			return;
		
		// remove and shift tail nodes
		nodes.remove( index );
		for( int k = index; k < nodes.size(); k++ ) {
			MetaEnvServerNode item = nodes.get( k );
			item.setPos( k + 1 );
		}
	}

	public void updateNode( MetaEnvServerNode node ) throws Exception {
		int index = nodes.indexOf( node );
		if( index < 0 )
			return;
		
		if( index + 1 == node.POS )
			return;
		
		nodes.remove( index );
		addNode( node );
	}

	public void getApplicationReferences( HostAccount account , List<AccountReference> refs ) {
		for( MetaEnvServerNode node : nodes )
			node.getApplicationReferences( account , refs );
	}

	public void removeBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.isBinaryItem() ) {
				MetaDistrBinaryItem binaryItem = deployment.getBinaryItem();
				if( binaryItem == item )
					deployments.remove( deployment );
			}
		}
	}
	
	public void removeDeployment( MetaEnvServerDeployment deployment ) {
		deployments.remove( deployment );
		deployMapById.remove( deployment.ID );
	}
	
	public void removeConfItem( MetaDistrConfItem item ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.isConfItem( item ) )
				removeDeployment( deployment );
		}
	}
	
	public void removeComponent( MetaDistrComponent item ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.isComponent( item ) )
				removeDeployment( deployment );
		}
	}
	
	public void removeSchema( MetaDatabaseSchema schema ) throws Exception {
		for( MetaEnvServerDeployment deployment : deployments ) {
			if( deployment.isSchema( schema ) )
				removeDeployment( deployment );
		}
	}

	public String getSchemaDBName( MetaDatabaseSchema schema ) throws Exception {
		for( MetaEnvServerDeployment d : deployments ) {
			if( d.isSchema( schema ) )
				return( d.DBNAME );
			
			if( d.isComponent() ) {
				MetaDistrComponent comp = d.getComponent();
				MetaDistrComponentItem item = comp.findSchemaItem( schema.ID );
				if( item != null )
					return( schema.DBNAMEDEF );
			}
		}
		
		return( null );
	}
	
	public String getSchemaDBUser( MetaDatabaseSchema schema ) throws Exception {
		for( MetaEnvServerDeployment d : deployments ) {
			if( d.isSchema( schema ) )
				return( d.DBUSER );
			
			if( d.isComponent() ) {
				MetaDistrComponent comp = d.getComponent();
				MetaDistrComponentItem item = comp.findSchemaItem( schema.ID );
				if( item != null )
					return( schema.DBUSERDEF );
			}
		}
		
		return( null );
	}

	public void createServer( String name , String desc , DBEnumServerRunType runType , DBEnumServerAccessType accessType , 
			DBEnumOSType osType , MatchItem baselineMatch , boolean offline , DBEnumDbmsType dbmsType , MatchItem admSchemaMatch , MatchItem baseItemMatch ) throws Exception {
		modifyServer( name , desc , runType , accessType , 
				osType , baselineMatch , offline , dbmsType , admSchemaMatch , baseItemMatch );
	}
	
	public void modifyServer( String name , String desc , DBEnumServerRunType runType , DBEnumServerAccessType accessType , 
			DBEnumOSType osType , MatchItem baselineMatch , boolean offline , DBEnumDbmsType dbmsType , MatchItem admSchemaMatch , MatchItem baseItemMatch ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.SERVERRUN_TYPE = runType;
		this.SERVERACCESS_TYPE = accessType;
		this.OS_TYPE = osType;
		this.BASELINE = MatchItem.copy( baselineMatch );
		this.OFFLINE = false;
		this.DBMS_TYPE = dbmsType;
		this.DATABASE_ADMSCHEMA = MatchItem.copy( admSchemaMatch );
		this.BASEITEM = MatchItem.copy( baseItemMatch );
		
		refreshPrimaryProperties();
		scatterExtraProperties();
	}

	public void addDependencyServer( MetaEnvServer server , DBEnumServerDependencyType type ) throws Exception {
		if( type == DBEnumServerDependencyType.NLB )
			this.nlbServer = server;
		else
		if( type == DBEnumServerDependencyType.PROXY )
			this.proxyServer = server;
		else
		if( type == DBEnumServerDependencyType.STATIC )
			this.staticServer = server;
		else
		if( type == DBEnumServerDependencyType.SUBORDINATE )
			subordinateServers.add( server );
		else
			Common.exitUnexpected();
	}
	
	public void updateExtraSettings() throws Exception {
		scatterExtraProperties();
	}
	
	public void updateCustomSettings() throws Exception {
		scatterExtraProperties();
	}
	
	public void clearDeployments() {
		deployments.clear();
		deployMapById.clear();
	}

	public MatchItem getBaselineMatchItem() {
		return( BASELINE );
	}
	
	public MatchItem getAdmSchemaMatchItem() {
		return( DATABASE_ADMSCHEMA );
	}
	
	public MatchItem getBaseItemMatchItem() {
		return( BASEITEM );
	}
	
}
