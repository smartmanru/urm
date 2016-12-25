package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseTargetItem {

	Meta meta;
	public ReleaseTarget target;
	
	public MetaSourceProjectItem sourceItem;
	public MetaDistrBinaryItem distItem;
	public String NAME = "";
	public String BUILDVERSION = "";
	
	public String DISTFILE;

	public ReleaseTargetItem( Meta meta , ReleaseTarget target ) {
		this.meta = meta;
		this.target = target;
	}

	public ReleaseTargetItem copy( ActionBase action , Release nr , ReleaseSet ns , ReleaseTarget nt ) throws Exception {
		ReleaseTargetItem nx = new ReleaseTargetItem( meta , nt );
		
		nx.sourceItem = sourceItem;
		nx.distItem = distItem;
		nx.NAME = NAME;
		nx.BUILDVERSION = BUILDVERSION;
		
		return( nx );
	}
	
	public void setDistFile( ActionBase action , String DISTFILE ) throws Exception {
		this.DISTFILE = DISTFILE;
	}
	
	public String getId() {
		return( target.NAME + ":" + distItem.KEY );
	}
	
	public boolean checkPropsEqualsToOptions( ActionBase action ) throws Exception {
		if( this.BUILDVERSION.equals( action.context.CTX_VERSION ) )
			return( true );
		
		action.error( getId() + " item attributes are different, please delete first" );
		return( false );
	}

	public MetaDistrDelivery getDelivery( ActionBase action ) throws Exception {
		if( distItem == null )
			return( null );
		return( distItem.delivery );
	}
	
	public void loadSourceItem( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		BUILDVERSION = ConfReader.getAttrValue( node , "BUILDVERSION" );
		MetaDistr distr = meta.getDistr( action );
		this.distItem = distr.getBinaryItem( action , NAME );
		this.sourceItem = target.sourceProject.getItem( action , distItem.sourceProjectItem.ITEMNAME );
	}
	
	public void createFromDistrItem( ActionBase action , MetaDistrBinaryItem distItem ) throws Exception {
		this.distItem = distItem;
		this.sourceItem = target.sourceProject.getItem( action , distItem.sourceProjectItem.ITEMNAME );
		NAME = distItem.KEY;
		BUILDVERSION = "";
	}
	
	public String getSpecifics( ActionBase action ) throws Exception {
		if( BUILDVERSION.isEmpty() )
			return( "" );
		return( "BUILDVERSION=" + BUILDVERSION );
	}
	
	public Element createXml( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , "distitem" );
		Common.xmlSetElementAttr( doc , element , "name" , NAME );
		if( !BUILDVERSION.isEmpty() )
			Common.xmlSetElementAttr( doc , element , "buildversion" , BUILDVERSION );
		return( element );
	}
	
}
