package ru.egov.urm.dist;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.MetaSourceProjectItem;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarNAMETYPE;

public class ReleaseTargetItem {

	Metadata meta;
	public ReleaseTarget target;
	
	public MetaSourceProjectItem sourceItem;
	public MetaDistrBinaryItem distItem;
	public String NAME = "";
	public String BUILDVERSION = "";
	
	public String DISTFILE;

	public ReleaseTargetItem( Metadata meta , ReleaseTarget target ) {
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
		NAME = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUMDOT );
		BUILDVERSION = ConfReader.getAttrValue( action , node , "BUILDVERSION" );
		this.sourceItem = target.sourceProject.getItem( action , NAME );
		this.distItem = sourceItem.distItem;
	}
	
	public void createFromSourceItem( ActionBase action , MetaSourceProjectItem projectItem ) {
		this.sourceItem = projectItem;
		this.distItem = sourceItem.distItem;
		NAME = sourceItem.ITEMNAME;
		BUILDVERSION = "";
	}
	
	public void createFromDistrItem( ActionBase action , MetaDistrBinaryItem distItem ) {
		this.distItem = distItem;
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
