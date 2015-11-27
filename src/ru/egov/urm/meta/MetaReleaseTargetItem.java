package ru.egov.urm.meta;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.run.ActionBase;

public class MetaReleaseTargetItem {

	Metadata meta;
	
	public MetaReleaseTarget releaseProject;
	public MetaSourceProjectItem sourceItem;
	public MetaDistrBinaryItem distItem;
	public String NAME = "";
	public String BUILDVERSION = "";
	
	public String DISTFILE;

	public MetaReleaseTargetItem( Metadata meta ) {
		this.meta = meta;
	}

	public void setDistFile( ActionBase action , String DISTFILE ) throws Exception {
		this.DISTFILE = DISTFILE;
	}
	
	public String getId() {
		return( releaseProject.NAME + ":" + distItem.KEY );
	}
	
	public boolean checkPropsEqualsToOptions( ActionBase action ) throws Exception {
		if( this.BUILDVERSION.equals( action.options.OPT_VERSION ) )
			return( true );
		
		action.log( getId() + " item attributes are different, please delete first" );
		return( false );
	}

	public MetaDistrDelivery getDelivery( ActionBase action ) throws Exception {
		if( distItem == null )
			return( null );
		return( distItem.delivery );
	}
	
	public void load( ActionBase action , Node node , MetaReleaseTarget releaseProject ) throws Exception {
		this.releaseProject = releaseProject;
		NAME = ConfReader.getNameAttr( action , node );
		BUILDVERSION = ConfReader.getAttrValue( action , node , "BUILDVERSION" );
		this.sourceItem = releaseProject.sourceProject.getItem( action , NAME );
		this.distItem = sourceItem.distItem;
	}
	
	public void createFromProjectItem( ActionBase action , MetaReleaseTarget releaseProject , MetaSourceProjectItem projectItem ) {
		this.releaseProject = releaseProject;
		this.sourceItem = projectItem;
		this.distItem = sourceItem.distItem;
		NAME = sourceItem.ITEMNAME;
		BUILDVERSION = "";
	}
	
	public void createFromDistrItem( ActionBase action , MetaReleaseTarget releaseProject , MetaDistrBinaryItem distItem ) {
		this.releaseProject = releaseProject;
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
