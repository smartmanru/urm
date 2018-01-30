package org.urm.meta.env;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.PropertyController;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product._Error;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServerDeployment extends PropertyController {
	
	protected Meta meta;
	MetaEnvServer server;
	
	public VarDEPLOYITEMTYPE itemType = VarDEPLOYITEMTYPE.UNKNOWN;
	public String COMP = "";
	public MetaDistrComponent comp;
	public String DISTITEM = "";
	public MetaDistrBinaryItem binaryItem;
	public String CONFITEM = "";
	public MetaDistrConfItem confItem;
	public String SCHEMA = "";
	public MetaDatabaseSchema schema;
	
	public VarDEPLOYMODE deployMode = VarDEPLOYMODE.UNKNOWN;
	public String DEPLOYPATH = "";
	public String DBNAME = "";
	public String DBUSER = "";
	public VarNODETYPE nodeType = VarNODETYPE.UNKNOWN;
	
	public static String PROPERTY_DEPLOYMODE = "deploymode";
	public static String PROPERTY_DEPLOYPATH = "deploypath";
	public static String PROPERTY_DBNAME = "dbname";
	public static String PROPERTY_DBUSER = "dbuser";
	public static String PROPERTY_NODETYPE = "nodetype";
	public static String PROPERTY_COMPONENT = "component";
	public static String PROPERTY_DISTITEM = "distitem";
	public static String PROPERTY_CONFITEM = "confitem";
	public static String PROPERTY_SCHEMA = "schema";
	
	public MetaEnvServerDeployment( Meta meta , MetaEnvServer server ) {
		super( server , "deploy" );
		this.meta = meta;
		this.server = server;
	}

	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		String value = super.getStringProperty( action , PROPERTY_DEPLOYMODE );
		if( value.isEmpty() )
			value = "cold";
		deployMode = Types.getDeployMode( value , false );
		DEPLOYPATH = super.getStringProperty( action , PROPERTY_DEPLOYPATH );
		DBNAME = super.getStringProperty( action , PROPERTY_DBNAME );
		DBUSER = super.getStringProperty( action , PROPERTY_DBUSER );
		
		value = super.getStringProperty( action , PROPERTY_NODETYPE );
		nodeType = Types.getNodeType( value , VarNODETYPE.SELF );
		
		COMP = super.getStringProperty( action , PROPERTY_COMPONENT );
		if( !COMP.isEmpty() ) {
			itemType = VarDEPLOYITEMTYPE.COMP;
			return;
		}
		
		DISTITEM = super.getStringProperty( action , PROPERTY_DISTITEM );
		if( !DISTITEM.isEmpty() ) {
			itemType = VarDEPLOYITEMTYPE.BINARY;
			return;
		}
		
		CONFITEM = super.getStringProperty( action , PROPERTY_CONFITEM );
		if( !CONFITEM.isEmpty() ) {
			itemType = VarDEPLOYITEMTYPE.CONF;
			return;
		}
		
		SCHEMA = super.getStringProperty( action , PROPERTY_SCHEMA );
		if( !SCHEMA.isEmpty() ) {
			itemType = VarDEPLOYITEMTYPE.SCHEMA;
			
			if( DBNAME.isEmpty() )
				DBNAME = SCHEMA;
			if( DBUSER.isEmpty() )
				DBUSER = DBNAME;
			return;
		}
		
		itemType = VarDEPLOYITEMTYPE.UNKNOWN;
		action.exit1( _Error.UnexpectedDeploymentType1 , "unexpected deployment type found, server=" + server.NAME , server.NAME );
	}
	
	public MetaEnvServerDeployment copy( ActionBase action , Meta meta , MetaEnvServer server ) throws Exception {
		MetaEnvServerDeployment r = new MetaEnvServerDeployment( meta , server );
		r.initCopyStarted( this , server.getProperties() );
		r.scatterProperties( action );
		r.resolveLinks( action );
		r.initFinished();
		return( r );
	}
	
	public void resolveLinks( ActionBase action ) throws Exception {
		MetaDistr distr = meta.getDistr(); 
		if( !COMP.isEmpty() )
			comp = distr.getComponent( COMP );
		if( !DISTITEM.isEmpty() )
			binaryItem = distr.getBinaryItem( DISTITEM );
		if( !CONFITEM.isEmpty() )
			confItem = distr.getConfItem( CONFITEM );
		
		MetaDatabase database = meta.getDatabase(); 
		if( !SCHEMA.isEmpty() )
			schema = database.getSchema( SCHEMA );
	}
	
	public void create( EngineTransaction transaction , VarDEPLOYITEMTYPE itemType , String itemName , VarNODETYPE nodeType , VarDEPLOYMODE deployMode , String deployPath , String dbName , String dbUser ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			transaction.exitUnexpectedState();

		ActionBase action = transaction.getAction();
		super.setSystemStringProperty( PROPERTY_DEPLOYMODE , Common.getEnumLower( deployMode ) );
		super.setSystemStringProperty( PROPERTY_DEPLOYPATH , deployPath );
		super.setSystemStringProperty( PROPERTY_NODETYPE , Common.getEnumLower( nodeType ) );
		
		if( itemType == VarDEPLOYITEMTYPE.COMP )
			super.setSystemStringProperty( PROPERTY_COMPONENT , itemName );
		else
		if( itemType == VarDEPLOYITEMTYPE.BINARY )
			super.setSystemStringProperty( PROPERTY_DISTITEM , itemName );
		else
		if( itemType == VarDEPLOYITEMTYPE.CONF )
			super.setSystemStringProperty( PROPERTY_CONFITEM , itemName );
		else
		if( itemType == VarDEPLOYITEMTYPE.SCHEMA ) {
			super.setSystemStringProperty( PROPERTY_SCHEMA , itemName );
			super.setSystemStringProperty( PROPERTY_DBNAME , dbName );
			super.setSystemStringProperty( PROPERTY_DBUSER , dbUser );
		}

		scatterProperties( action );
		resolveLinks( action );
		super.initFinished();
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			return;

		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		
		super.initFinished();
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveSplit( doc , root );
	}
	
	public boolean hasConfItemDeployment( MetaDistrConfItem p_confItem ) {
		if( this.confItem == p_confItem ) 
			return( true );
		
		if( comp != null ) {
			for( MetaDistrComponentItem item : comp.getConfItems() )
				if( item.confItem == p_confItem )
					return( true );
		}
		return( false );
	}
	
	public boolean hasBinaryItemDeployment( MetaDistrBinaryItem p_binaryItem ) {
		if( this.binaryItem == p_binaryItem ) 
			return( true );
		
		if( comp != null ) {
			for( MetaDistrComponentItem item : comp.getBinaryItems() )
				if( item.binaryItem == p_binaryItem )
					return( true );
		}
		return( false );
	}

	public boolean hasDatabaseItemDeployment( MetaDatabaseSchema p_schema ) {
		if( this.schema == p_schema ) 
			return( true );
		
		if( comp != null ) {
			for( MetaDistrComponentItem item : comp.getSchemaItems() )
				if( item.schema == p_schema )
					return( true );
		}
		return( false );
	}

	public String getDeployPath( ActionBase action ) throws Exception {
		if( DEPLOYPATH.isEmpty() || DEPLOYPATH.equals( "default" ) ) {
			if( server.DEPLOYPATH.isEmpty() ) {
				String name = getName();
				action.exit1( _Error.UnknownDeploymentPath1 , "deployment=" + name + " has unknown deployment path" , name );
			}
			return( server.DEPLOYPATH );
		}
		
		return( DEPLOYPATH );
	}

	public VarDEPLOYMODE getDeployType( ActionBase action ) throws Exception {
		return( deployMode );
	}

	public boolean isManual() {
		return( deployMode == VarDEPLOYMODE.MANUAL );
	}
	
	public MetaEnvServerLocation getLocation( ActionBase action ) throws Exception {
		VarDEPLOYMODE deployType = getDeployType( action );
		String deployPath = getDeployPath( action );
		return( new MetaEnvServerLocation( meta , server , deployType , deployPath ) );
	}

	public boolean isNodeAdminDeployment() {
		if( nodeType == VarNODETYPE.ADMIN )
			return( true );
		
		if( deployMode == VarDEPLOYMODE.HOT ) {
			if( nodeType == VarNODETYPE.UNKNOWN )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean isNodeSlaveDeployment() {
		if( nodeType == VarNODETYPE.SLAVE )
			return( true );
		
		if( deployMode != VarDEPLOYMODE.HOT ) {
			if( nodeType == VarNODETYPE.UNKNOWN )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean isNodeSelfDeployment() {
		if( nodeType == VarNODETYPE.SELF )
			return( true );
		
		if( nodeType == VarNODETYPE.UNKNOWN )
			return( true );
		
		return( false );
	}
	
	public boolean isBinaryItem() {
		if( !DISTITEM.isEmpty() )
			return( true );
		return( false );
	}
	
	public boolean isConfItem() {
		if( !CONFITEM.isEmpty() )
			return( true );
		return( false );
	}
	
	public boolean isComponent() {
		if( !COMP.isEmpty() )
			return( true );
		return( false );
	}
	
	public boolean isDatabase() {
		if( !SCHEMA.isEmpty() )
			return( true );
		return( false );
	}

	public boolean hasFileDeployments() {
		if( isBinaryItem() || isConfItem() )
			return( true );
		if( isComponent() ) {
			if( comp.hasBinaryItems() || comp.hasConfItems() )
				return( true );
		}
		return( false );
	}
	
	public String getName() {
		String name = "unknown";
		if( itemType == VarDEPLOYITEMTYPE.BINARY )
			name = "binary-" + DISTITEM;
		else
		if( itemType == VarDEPLOYITEMTYPE.CONF )
			name = "conf-" + CONFITEM;
		else
		if( itemType == VarDEPLOYITEMTYPE.SCHEMA )
			name = "schema-" + SCHEMA;
		else
		if( itemType == VarDEPLOYITEMTYPE.COMP )
			name = "comp-" + COMP;
		return( name );
	}
	
	public static String getName( VarDEPLOYITEMTYPE itemType , String itemName ) {
		String name = itemName;
		if( itemType == VarDEPLOYITEMTYPE.BINARY )
			name = "binary-" + name;
		else
		if( itemType == VarDEPLOYITEMTYPE.CONF )
			name = "conf-" + name;
		else
		if( itemType == VarDEPLOYITEMTYPE.SCHEMA )
			name = "schema-" + name;
		else
		if( itemType == VarDEPLOYITEMTYPE.COMP )
			name = "comp-" + name;
		return( name );
	}

}