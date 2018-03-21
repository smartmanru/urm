package org.urm.meta.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseScopeItem {

	Meta meta;
	public ReleaseScopeTarget target;
	
	public MetaSourceProjectItem sourceItem;
	public MetaDistrBinaryItem distItem;
	public MetaDatabaseSchema schema;
	public MetaProductDoc doc;
	
	public String NAME = "";
	public String BUILDVERSION = "";
	public String DISTFILE;

	public ReleaseScopeItem( Meta meta , ReleaseScopeTarget target ) {
		this.meta = meta;
		this.target = target;
	}

	public ReleaseScopeItem copy( ActionBase action , Release nr , ReleaseScopeSet ns , ReleaseScopeTarget nt ) throws Exception {
		ReleaseScopeItem nx = new ReleaseScopeItem( nt.meta , nt );
		
		nx.sourceItem = ( sourceItem == null )? null : nt.sourceProject.getItem( sourceItem.NAME );
		nx.distItem = ( distItem == null )? null : sourceItem.distItem;
		MetaDatabase ndb = nt.meta.getDatabase();
		nx.schema = ( schema == null )? null : ndb.getSchema( schema.NAME );
		MetaDocs ndocs = nt.meta.getDocs();
		nx.doc = ( doc == null )? null : ndocs.getDoc( doc.NAME );
		nx.NAME = NAME;
		nx.BUILDVERSION = BUILDVERSION;
		
		return( nx );
	}
	
	public boolean isBinary() {
		if( distItem != null )
			return( true );
		return( false );
	}
	
	public boolean isDatabase() {
		if( schema != null )
			return( true );
		return( false );
	}
	
	public boolean isDoc() {
		if( doc != null )
			return( true );
		return( false );
	}
	
	public void setDistFile( ActionBase action , String DISTFILE ) throws Exception {
		this.DISTFILE = DISTFILE;
	}
	
	public String getId() {
		if( isBinary() )
			return( target.NAME + ":" + distItem.NAME );
		if( isDatabase() )
			return( target.NAME + ":" + schema.NAME );
		if( isDoc() )
			return( target.NAME + ":" + doc.NAME );
		return( null );
	}
	
	public boolean checkPropsEqualsToOptions( ActionBase action ) throws Exception {
		if( this.BUILDVERSION.equals( action.context.CTX_VERSION ) )
			return( true );
		
		action.error( getId() + " item attributes are different, please delete first" );
		return( false );
	}

	public MetaDistrDelivery getDelivery( ActionBase action ) throws Exception {
		if( isBinary() ) {
			if( distItem == null )
				return( null );
			return( distItem.delivery );
		}
		if( isDatabase() )
			return( target.distDelivery );
		if( isDoc() )
			return( target.distDelivery );
		action.exitUnexpectedState();
		return( null );
	}
	
	public void loadSourceItem( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , EnumNameType.ALPHANUMDOT );
		BUILDVERSION = ConfReader.getAttrValue( node , ReleaseTarget.PROPERTY_BUILDVERSION );
		MetaDistr distr = meta.getDistr();
		this.distItem = distr.getBinaryItem( NAME );
		this.sourceItem = target.sourceProject.getItem( distItem.sourceProjectItem.NAME );
	}
	
	public void loadDatabaseItem( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , EnumNameType.ALPHANUMDOT );
		BUILDVERSION = "";
		MetaDatabase db = meta.getDatabase();
		this.schema = db.getSchema( NAME );
	}
	
	public void loadDocItem( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , EnumNameType.ALPHANUMDOT );
		BUILDVERSION = "";
		MetaDocs docs = meta.getDocs();
		this.doc = docs.getDoc( NAME );
	}
	
	public void createFromDistrItem( ActionBase action , MetaDistrBinaryItem distItem ) throws Exception {
		this.distItem = distItem;
		this.sourceItem = target.sourceProject.getItem( distItem.sourceProjectItem.NAME );
		NAME = distItem.NAME;
		BUILDVERSION = "";
	}
	
	public void createFromSchema( ActionBase action , MetaDatabaseSchema schema ) throws Exception {
		this.schema = schema;
		NAME = schema.NAME;
		BUILDVERSION = "";
	}
	
	public void createFromDoc( ActionBase action , MetaProductDoc doc ) throws Exception {
		this.doc = doc;
		NAME = doc.NAME;
		BUILDVERSION = "";
	}
	
	public String getSpecifics( ActionBase action ) throws Exception {
		if( BUILDVERSION.isEmpty() )
			return( "" );
		return( "BUILDVERSION=" + BUILDVERSION );
	}
	
	public Element createXml( ActionBase action , Document doc , Element parent ) throws Exception {
		if( isBinary() ) {
			Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_DISTITEM );
			Meta.setNameAttr( action , doc , element , EnumNameType.ALPHANUMDOTDASH , NAME );
			if( !BUILDVERSION.isEmpty() )
				Common.xmlSetElementAttr( doc , element , ReleaseTarget.PROPERTY_BUILDVERSION , BUILDVERSION );
			return( element );
		}
		if( isDatabase() ) {
			Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_SCHEMA );
			Meta.setNameAttr( action , doc , element , EnumNameType.ALPHANUMDOTDASH , NAME );
			return( element );
		}
		if( isDoc() ) {
			Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_DOC );
			Meta.setNameAttr( action , doc , element , EnumNameType.ALPHANUMDOTDASH , NAME );
			return( element );
		}
		action.exitUnexpectedState();
		return( null );
	}
	
}
