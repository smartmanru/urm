package org.urm.meta.env;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.loader.MatchItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;

public class MetaEnvServerDeployment {

	// properties
	public static String PROPERTY_DEPLOYMODE = "deploymode";
	public static String PROPERTY_DEPLOYPATH = "deploypath";
	public static String PROPERTY_DBNAME = "dbname";
	public static String PROPERTY_DBUSER = "dbuser";
	public static String PROPERTY_NODETYPE = "nodetype";
	public static String PROPERTY_COMPONENT = "component";
	public static String PROPERTY_DISTITEM = "distitem";
	public static String PROPERTY_CONFITEM = "confitem";
	public static String PROPERTY_SCHEMA = "schema";
	
	public Meta meta;
	public MetaEnvServer server;

	public int ID;
	public DBEnumServerDeploymentType SERVERDEPLOYMENT_TYPE;
	private MatchItem COMP;
	private MatchItem BINARYITEM;
	private MatchItem CONFITEM;
	private MatchItem SCHEMA;
	public DBEnumDeployModeType DEPLOYMODE_TYPE;
	public String DEPLOYPATH;
	public String DBNAME;
	public String DBUSER;
	public DBEnumNodeType NODE_TYPE;
	public int EV;
	
	public MetaEnvServerDeployment( Meta meta , MetaEnvServer server ) {
		this.meta = meta;
		this.server = server;
		this.ID = -1;
		this.EV = -1;
	}

	public MetaEnvServerDeployment copy( Meta rmeta , MetaEnvServer rserver ) throws Exception {
		MetaEnvServerDeployment r = new MetaEnvServerDeployment( rmeta , rserver );
		
		r.ID = ID;
		r.SERVERDEPLOYMENT_TYPE = SERVERDEPLOYMENT_TYPE;
		r.COMP = MatchItem.copy( COMP );
		r.BINARYITEM = MatchItem.copy( BINARYITEM );
		r.CONFITEM = MatchItem.copy( CONFITEM );
		r.SCHEMA = MatchItem.copy( SCHEMA );
		r.DEPLOYMODE_TYPE = DEPLOYMODE_TYPE;
		r.DEPLOYPATH = DEPLOYPATH;
		r.DBNAME = DBNAME;
		r.DBUSER = DBUSER;
		r.NODE_TYPE = NODE_TYPE;
		r.EV = EV;
		
		return( r );
	}
	
	public boolean checkMatched() {
		if( !MatchItem.isMatched( COMP ) )
			return( false );
		if( !MatchItem.isMatched( BINARYITEM ) )
			return( false );
		if( !MatchItem.isMatched( CONFITEM ) )
			return( false );
		if( !MatchItem.isMatched( SCHEMA ) )
			return( false );
		return( true );
	}

	public void create( DBEnumServerDeploymentType type , MatchItem comp , MatchItem binaryItem , MatchItem confItem ,
			MatchItem schema , DBEnumDeployModeType deployMode , String deployPath , String dbName , String dbUser , DBEnumNodeType nodeType ) throws Exception {
		modify( type , comp , binaryItem , confItem ,
				schema , deployMode , deployPath , dbName , dbUser , nodeType );
	}
	
	public void modify( DBEnumServerDeploymentType type , MatchItem comp , MatchItem binaryItem , MatchItem confItem ,
			MatchItem schema , DBEnumDeployModeType deployMode , String deployPath , String dbName , String dbUser , DBEnumNodeType nodeType ) throws Exception {
		this.SERVERDEPLOYMENT_TYPE = type;
		this.COMP = comp;
		this.BINARYITEM = binaryItem;
		this.CONFITEM = confItem;
		this.SCHEMA = schema;
		this.DEPLOYMODE_TYPE = deployMode;
		this.DEPLOYPATH = deployPath;
		this.DBNAME = dbName;
		this.DBUSER = dbUser;
		this.NODE_TYPE = nodeType;
	}
	
	public void createComponent( MetaDistrComponent comp , 
			DBEnumDeployModeType deployMode , String deployPath , DBEnumNodeType nodeType ) throws Exception {
		if( server.hasComponentDeployment( comp ) )
			Common.exit1( _Error.DuplicateComponent1 , "duplicate component=" + comp.NAME , comp.NAME );
		create( DBEnumServerDeploymentType.COMP , new MatchItem( comp.ID ) , null , null ,
				null , deployMode , deployPath , "" , "" , nodeType );
	}
	
	public void modifyComponent( MetaDistrComponent comp , 
			DBEnumDeployModeType deployMode , String deployPath , DBEnumNodeType nodeType ) throws Exception {
		if( this.COMP.FKID != comp.ID && server.hasComponentDeployment( comp ) )
			Common.exit1( _Error.DuplicateComponent1 , "duplicate component=" + comp.NAME , comp.NAME );
		modify( DBEnumServerDeploymentType.COMP , new MatchItem( comp.ID ) , null , null ,
				null , deployMode , deployPath , "" , "" , nodeType );
	}
	
	public void createBinaryItem( MetaDistrBinaryItem item , 
			DBEnumDeployModeType deployMode , String deployPath , DBEnumNodeType nodeType ) throws Exception {
		if( server.hasBinaryItemDeployment( item ) )
			Common.exit1( _Error.DuplicateBinaryItem1 , "duplicate binary item=" + item.NAME , item.NAME );
		create( DBEnumServerDeploymentType.BINARY , null , new MatchItem( item.ID ) , null ,
				null , deployMode , deployPath , "" , "" , nodeType );
	}
	
	public void modifyBinaryItem( MetaDistrBinaryItem item , 
			DBEnumDeployModeType deployMode , String deployPath , DBEnumNodeType nodeType ) throws Exception {
		if( this.BINARYITEM.FKID != item.ID && server.hasBinaryItemDeployment( item ) )
			Common.exit1( _Error.DuplicateBinaryItem1 , "duplicate binary item=" + item.NAME , item.NAME );
		modify( DBEnumServerDeploymentType.BINARY , null , new MatchItem( item.ID ) , null ,
				null , deployMode , deployPath , "" , "" , nodeType );
	}
	
	public void createConfItem( MetaDistrConfItem item , 
			DBEnumDeployModeType deployMode , String deployPath , DBEnumNodeType nodeType ) throws Exception {
		if( server.hasConfItemDeployment( item ) )
			Common.exit1( _Error.DuplicateConfItem1 , "duplicate configuration item=" + item.NAME , item.NAME );
		create( DBEnumServerDeploymentType.CONF , null , null , new MatchItem( item.ID ) , 
				null , deployMode , deployPath , "" , "" , nodeType );
	}
	
	public void modifyConfItem( MetaDistrConfItem item , 
			DBEnumDeployModeType deployMode , String deployPath , DBEnumNodeType nodeType ) throws Exception {
		if( this.CONFITEM.FKID != item.ID && server.hasConfItemDeployment( item ) )
			Common.exit1( _Error.DuplicateConfItem1 , "duplicate configuration item=" + item.NAME , item.NAME );
		modify( DBEnumServerDeploymentType.CONF , null , null , new MatchItem( item.ID ) , 
				null , deployMode , deployPath , "" , "" , nodeType );
	}
	
	public void createSchema( MetaDatabaseSchema schema , 
			DBEnumDeployModeType deployMode , String dbName , String dbUser ) throws Exception {
		if( server.hasDatabaseItemDeployment( schema ) )
			Common.exit1( _Error.DuplicateSchemaItem1 , "duplicate database schema=" + schema.NAME , schema.NAME );
		create( DBEnumServerDeploymentType.SCHEMA, null , null , null ,
				new MatchItem( schema.ID ) , deployMode , "" , dbName , dbUser , DBEnumNodeType.ADMIN );
	}
	
	public void modifySchema( MetaDatabaseSchema schema , 
			DBEnumDeployModeType deployMode , String dbName , String dbUser ) throws Exception {
		if( this.SCHEMA.FKID != schema.ID && server.hasDatabaseItemDeployment( schema ) )
			Common.exit1( _Error.DuplicateSchemaItem1 , "duplicate database schema=" + schema.NAME , schema.NAME );
		modify( DBEnumServerDeploymentType.SCHEMA , null , null , null ,
				new MatchItem( schema.ID ) , deployMode , "" , dbName , dbUser , DBEnumNodeType.ADMIN );
	}
	
	public boolean hasConfItemDeployment( MetaDistrConfItem confItem ) {
		if( MatchItem.equals( CONFITEM , confItem.ID ) ) 
			return( true );
		
		MetaDistrComponent comp = findComponent();
		if( comp != null ) {
			for( MetaDistrComponentItem item : comp.getConfItems() )
				if( item.confItem == confItem )
					return( true );
		}
		return( false );
	}
	
	public boolean hasBinaryItemDeployment( MetaDistrBinaryItem binaryItem ) {
		if( MatchItem.equals( BINARYITEM , binaryItem.ID ) ) 
			return( true );
		
		MetaDistrComponent comp = findComponent();
		if( comp != null ) {
			for( MetaDistrComponentItem item : comp.getBinaryItems() )
				if( item.binaryItem == binaryItem )
					return( true );
		}
		return( false );
	}

	public boolean hasDatabaseItemDeployment( MetaDatabaseSchema schema ) {
		if( MatchItem.equals( SCHEMA , schema.ID ) ) 
			return( true );
		
		MetaDistrComponent comp = findComponent();
		if( comp != null ) {
			for( MetaDistrComponentItem item : comp.getSchemaItems() )
				if( item.schema == schema )
					return( true );
		}
		return( false );
	}

	public String getDeployPath( ActionBase action ) throws Exception {
		if( DEPLOYPATH.isEmpty() || DEPLOYPATH.equals( "default" ) ) {
			if( server.DEPLOYPATH.isEmpty() )
				action.exit1( _Error.UnknownDeploymentPath1 , "deployment=" + ID + " has unknown deployment path" , "" + ID );
			return( server.DEPLOYPATH );
		}
		
		return( DEPLOYPATH );
	}

	public boolean isAdmin() {
		return( NODE_TYPE == DBEnumNodeType.ADMIN );
	}
	
	public boolean isSlave() {
		return( NODE_TYPE == DBEnumNodeType.SLAVE );
	}
	
	public boolean isSelf() {
		return( NODE_TYPE == DBEnumNodeType.SELF );
	}
	
	public boolean isManual() {
		return( DEPLOYMODE_TYPE == DBEnumDeployModeType.MANUAL );
	}
	
	public boolean isHot() {
		return( DEPLOYMODE_TYPE == DBEnumDeployModeType.HOT );
	}
	
	public MetaEnvServerLocation getLocation() throws Exception {
		return( new MetaEnvServerLocation( meta , server , DEPLOYMODE_TYPE , DEPLOYPATH ) );
	}

	public boolean isNodeAdminDeployment() {
		if( isAdmin() )
			return( true );
		
		if( isHot() ) {
			if( NODE_TYPE == DBEnumNodeType.UNKNOWN )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean isNodeSlaveDeployment() {
		if( isSlave() )
			return( true );
		
		if( !isHot() ) {
			if( NODE_TYPE == DBEnumNodeType.UNKNOWN )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean isNodeSelfDeployment() {
		if( isSelf() )
			return( true );
		
		if( NODE_TYPE == DBEnumNodeType.UNKNOWN )
			return( true );
		
		return( false );
	}
	
	public boolean isComponent() {
		if( SERVERDEPLOYMENT_TYPE == DBEnumServerDeploymentType.COMP )
			return( true );
		return( false );
	}
	
	public boolean isBinaryItem() {
		if( SERVERDEPLOYMENT_TYPE == DBEnumServerDeploymentType.BINARY )
			return( true );
		return( false );
	}
	
	public boolean isConfItem() {
		if( SERVERDEPLOYMENT_TYPE == DBEnumServerDeploymentType.CONF )
			return( true );
		return( false );
	}
	
	public boolean isDatabase() {
		if( SERVERDEPLOYMENT_TYPE == DBEnumServerDeploymentType.SCHEMA )
			return( true );
		return( false );
	}

	public boolean isConfItem( MetaDistrConfItem item ) {
		if( isConfItem() && MatchItem.equals( CONFITEM , item.ID ) )
			return( true );
		return( false );
	}
	
	public boolean isBinaryItem( MetaDistrBinaryItem item ) {
		if( isBinaryItem() && MatchItem.equals( BINARYITEM , item.ID ) )
			return( true );
		return( false );
	}
	
	public boolean isSchema( MetaDatabaseSchema item ) {
		if( isDatabase() && MatchItem.equals( SCHEMA , item.ID ) )
			return( true );
		return( false );
	}
	
	public boolean isComponent( MetaDistrComponent comp ) {
		if( isComponent() && MatchItem.equals( COMP , comp.ID ) )
			return( true );
		return( false );
	}
	
	public MetaDistrComponent getComponent() throws Exception {
		MetaDistr distr = meta.getDistr();
		return( distr.getComponent( COMP ) );
	}
	
	public MetaDistrComponent findComponent() {
		MetaDistr distr = meta.getDistr();
		return( distr.findComponent( COMP ) );
	}
	
	public MetaDistrBinaryItem getBinaryItem() throws Exception {
		MetaDistr distr = meta.getDistr();
		return( distr.getBinaryItem( BINARYITEM ) );
	}
	
	public MetaDistrConfItem getConfItem() throws Exception {
		MetaDistr distr = meta.getDistr();
		return( distr.getConfItem( CONFITEM ) );
	}
	
	public MetaDatabaseSchema getSchema() throws Exception {
		MetaDatabase db = meta.getDatabase();
		return( db.getSchema( SCHEMA ) );
	}
	
	public boolean hasFileDeployments() throws Exception {
		if( isBinaryItem() || isConfItem() )
			return( true );
		
		if( isComponent() ) {
			MetaDistrComponent comp = getComponent();
			if( comp.hasBinaryItems() || comp.hasConfItems() )
				return( true );
		}
		return( false );
	}
	
	public MatchItem getCompMatchItem() {
		return( COMP );
	}
	
	public MatchItem getBinaryItemMatchItem() {
		return( BINARYITEM );
	}
	
	public MatchItem getConfItemMatchItem() {
		return( CONFITEM );
	}
	
	public MatchItem getSchemaMatchItem() {
		return( SCHEMA );
	}
	
}
