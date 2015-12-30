package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.Metadata.VarDEPLOYTYPE;
import ru.egov.urm.run.ActionBase;

public class MetaEnvServerDeployment {
	
	MetaEnvServer server;
	
	public MetaDistrComponent comp;
	public MetaDistrBinaryItem binaryItem;
	public MetaDistrConfItem confItem;
	
	private VarDEPLOYTYPE DEPLOYTYPE;
	private String DEPLOYPATH;
	
	public MetaEnvServerDeployment( MetaEnvServer server ) {
		this.server = server;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		MetaDistr distr = action.meta.distr;
		
		DEPLOYTYPE = action.meta.getDeployType( action , ConfReader.getAttrValue( action , node , "deploytype" , "default" ) );
		DEPLOYPATH = ConfReader.getAttrValue( action , node , "deploypath" );
		
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
		if( DEPLOYTYPE == VarDEPLOYTYPE.UNKNOWN ) {
			if( server.DEPLOYTYPE == VarDEPLOYTYPE.UNKNOWN )
				return( VarDEPLOYTYPE.DEFAULT );
			return( server.DEPLOYTYPE );
		}
		
		return( DEPLOYTYPE );
	}

	public MetaEnvServerLocation getLocation( ActionBase action ) throws Exception {
		VarDEPLOYTYPE deployType = getDeployType( action );
		String deployPath = getDeployPath( action );
		return( new MetaEnvServerLocation( server , deployType , deployPath ) );
	}
	
}
