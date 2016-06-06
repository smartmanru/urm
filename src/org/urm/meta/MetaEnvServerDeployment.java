package org.urm.meta;

import org.urm.common.ConfReader;
import org.urm.meta.Metadata.VarDEPLOYTYPE;
import org.urm.meta.Metadata.VarNODETYPE;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaEnvServerDeployment {
	
	Metadata meta;
	MetaEnvServer server;
	
	public MetaDistrComponent comp;
	public MetaDistrBinaryItem binaryItem;
	public MetaDistrConfItem confItem;
	
	private VarDEPLOYTYPE DEPLOYTYPE;
	private String DEPLOYPATH;
	public String NODETYPE;
	private VarNODETYPE nodeType;
	
	public MetaEnvServerDeployment( Metadata meta , MetaEnvServer server ) {
		this.meta = meta;
		this.server = server;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		MetaDistr distr = action.meta.distr;
		
		DEPLOYTYPE = action.meta.getDeployType( action , ConfReader.getAttrValue( action , node , "deploytype" , "cold" ) );
		DEPLOYPATH = ConfReader.getAttrValue( action , node , "deploypath" );
		NODETYPE = ConfReader.getAttrValue( action , node , "nodetype" , "unknown" );
		nodeType = action.meta.getNodeType( action , NODETYPE , VarNODETYPE.SELF );
		
		String COMP = ConfReader.getAttrValue( action , node , "component" );
		if( !COMP.isEmpty() ) {
			comp = distr.getComponent( action , COMP );
			return;
		}
		
		String DISTITEM = ConfReader.getAttrValue( action , node , "distitem" );
		if( !DISTITEM.isEmpty() ) {
			binaryItem = distr.getBinaryItem( action , DISTITEM );
			return;
		}
		
		String CONFITEM = ConfReader.getAttrValue( action , node , "confitem" );
		if( !CONFITEM.isEmpty() ) {
			confItem = distr.getConfItem( action , CONFITEM );
			return;
		}
		
		action.exit( "unexpected deployment type found, server=" + server.NAME );
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
				action.exit( "deployment has unknown deployment path" );
			return( server.DEPLOYPATH );
		}
		
		return( DEPLOYPATH );
	}

	public VarDEPLOYTYPE getDeployType( ActionBase action ) throws Exception {
		return( DEPLOYTYPE );
	}

	public boolean isManual( ActionBase action ) throws Exception {
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
