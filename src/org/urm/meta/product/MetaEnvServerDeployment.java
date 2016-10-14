package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.PropertyController;
import org.urm.meta.product.Meta.VarDEPLOYTYPE;
import org.urm.meta.product.Meta.VarNODETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServerDeployment extends PropertyController {
	
	protected Meta meta;
	MetaEnvServer server;
	
	public String COMP;
	public MetaDistrComponent comp;
	public String DISTITEM;
	public MetaDistrBinaryItem binaryItem;
	public String CONFITEM;
	public MetaDistrConfItem confItem;
	
	private VarDEPLOYTYPE DEPLOYTYPE;
	private String DEPLOYPATH;
	private VarNODETYPE nodeType;
	
	public static String PROPERTY_DEPLOYTYPE = "deploytype";
	public static String PROPERTY_DEPLOYPATH = "deploypath";
	public static String PROPERTY_NODETYPE = "nodetype";
	public static String PROPERTY_COMPONENT = "component";
	public static String PROPERTY_DISTITEM = "distitem";
	public static String PROPERTY_CONFITEM = "confitem";
	
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
		String value = super.getStringProperty( action , PROPERTY_DEPLOYTYPE );
		if( value.isEmpty() )
			value = "cold";
		DEPLOYTYPE = Meta.getDeployType( value , false );
		DEPLOYPATH = super.getStringProperty( action , PROPERTY_DEPLOYPATH );
		value = super.getStringProperty( action , PROPERTY_NODETYPE );
		nodeType = Meta.getNodeType( value , VarNODETYPE.SELF );
		
		COMP = super.getStringProperty( action , PROPERTY_COMPONENT );
		if( !COMP.isEmpty() )
			return;
		
		DISTITEM = super.getStringProperty( action , PROPERTY_DISTITEM );
		if( !DISTITEM.isEmpty() )
			return;
		
		CONFITEM = super.getStringProperty( action , PROPERTY_CONFITEM );
		if( !CONFITEM.isEmpty() )
			return;
		
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
		MetaDistr distr = meta.getDistr( action ); 
		if( !COMP.isEmpty() )
			comp = distr.getComponent( action , COMP );
		if( !DISTITEM.isEmpty() )
			binaryItem = distr.getBinaryItem( action , DISTITEM );
		if( !CONFITEM.isEmpty() )
			confItem = distr.getConfItem( action , CONFITEM );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			return;

		properties.loadFromNodeAttributes( node );
		scatterProperties( action );
		
		super.initFinished();
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		properties.saveSplit( doc , root );
	}
	
	public boolean hasConfItemDeployment( ActionBase action , MetaDistrConfItem p_confItem ) throws Exception {
		if( this.confItem == p_confItem ) 
			return( true );
		
		if( comp != null ) {
			for( MetaDistrComponentItem item : comp.getConfItems( action ).values() )
				if( item.confItem == p_confItem )
					return( true );
		}
		return( true );
	}
	
	public boolean hasBinaryItemDeployment( ActionBase action , MetaDistrBinaryItem p_binaryItem ) throws Exception {
		if( this.binaryItem == p_binaryItem ) 
			return( true );
		
		if( comp != null ) {
			for( MetaDistrComponentItem item : comp.getConfItems( action ).values() )
				if( item.binaryItem == p_binaryItem )
					return( true );
		}
		return( true );
	}

	public String getDeployPath( ActionBase action ) throws Exception {
		if( DEPLOYPATH.isEmpty() || DEPLOYPATH.equals( "default" ) ) {
			if( server.DEPLOYPATH.isEmpty() )
				action.exit0( _Error.UnknownDeploymentPath0 , "deployment has unknown deployment path" );
			return( server.DEPLOYPATH );
		}
		
		return( DEPLOYPATH );
	}

	public VarDEPLOYTYPE getDeployType( ActionBase action ) throws Exception {
		return( DEPLOYTYPE );
	}

	public boolean isManual() {
		return( DEPLOYTYPE == VarDEPLOYTYPE.MANUAL );
	}
	
	public MetaEnvServerLocation getLocation( ActionBase action ) throws Exception {
		VarDEPLOYTYPE deployType = getDeployType( action );
		String deployPath = getDeployPath( action );
		return( new MetaEnvServerLocation( meta , server , deployType , deployPath ) );
	}

	public boolean isNodeAdminDeployment( ActionBase action ) throws Exception {
		if( nodeType == VarNODETYPE.ADMIN )
			return( true );
		
		if( DEPLOYTYPE == VarDEPLOYTYPE.HOT ) {
			if( nodeType == VarNODETYPE.UNKNOWN )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean isNodeSlaveDeployment( ActionBase action ) throws Exception {
		if( nodeType == VarNODETYPE.SLAVE )
			return( true );
		
		if( DEPLOYTYPE != VarDEPLOYTYPE.HOT ) {
			if( nodeType == VarNODETYPE.UNKNOWN )
				return( true );
			return( false );
		}
		
		return( false );
	}

	public boolean isNodeSelfDeployment( ActionBase action ) throws Exception {
		if( nodeType == VarNODETYPE.SELF )
			return( true );
		
		if( nodeType == VarNODETYPE.UNKNOWN )
			return( true );
		
		return( false );
	}
}
