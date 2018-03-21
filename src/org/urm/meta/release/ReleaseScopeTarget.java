package org.urm.meta.release;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist._Error;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseScopeTarget {

	Meta meta;
	public ReleaseScopeSet set;
	public DBEnumScopeCategoryType CATEGORY;

	public boolean ALL;
	public String NAME = "";
	
	public String BUILDBRANCH = ""; 
	public String BUILDTAG = ""; 
	public String BUILDVERSION = "";
	
	public MetaSourceProject sourceProject;
	public MetaDistrConfItem distConfItem;
	public MetaDistrBinaryItem distManualItem;
	public MetaDistrBinaryItem distDerivedItem;
	
	public MetaDistrDelivery distDelivery;
	
	Map<String,ReleaseScopeItem> itemMap = new HashMap<String,ReleaseScopeItem>();
	
	public String DISTFILE;

	public ReleaseScopeTarget( Meta meta , ReleaseScopeSet set , DBEnumScopeCategoryType CATEGORY ) {
		this.meta = meta;
		this.set = set;
		this.CATEGORY = CATEGORY;
	}

	public ReleaseScopeTarget copy( ActionBase action , Release nr , ReleaseScopeSet ns ) throws Exception {
		ReleaseScopeTarget nx = new ReleaseScopeTarget( ns.meta , ns , CATEGORY );
		
		nx.ALL = ALL;
		nx.NAME = NAME;
		
		nx.BUILDBRANCH = BUILDBRANCH; 
		nx.BUILDTAG = BUILDTAG; 
		nx.BUILDVERSION = BUILDVERSION;
		
		nx.sourceProject = ( sourceProject == null )? null : ns.set.getProject( sourceProject.NAME );
		MetaDistr ndistr = ns.meta.getDistr();
		nx.distConfItem = ( distConfItem == null )? null : ndistr.getConfItem( distConfItem.NAME );
		nx.distDelivery = ( distDelivery == null )? null : ndistr.getDelivery( distDelivery.NAME );
		nx.distManualItem = ( distManualItem == null )? null : ndistr.getBinaryItem( distManualItem.NAME );
		nx.distDerivedItem = ( distDerivedItem == null )? null : ndistr.getBinaryItem( distDerivedItem.NAME );

		for( Entry<String,ReleaseScopeItem> entry : itemMap.entrySet() ) {
			ReleaseScopeItem item = entry.getValue().copy( action , nr , ns , nx );
			nx.itemMap.put( entry.getKey() , item );
		}
		
		return( nx );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( CATEGORY.isSourceCategory() )
			loadProject( action , node );
		else
		if( CATEGORY == DBEnumScopeCategoryType.CONFIG )
			loadConfiguration( action , node );
		else
		if( CATEGORY == DBEnumScopeCategoryType.MANUAL )
			loadManual( action , node );
		else
		if( CATEGORY == DBEnumScopeCategoryType.DERIVED )
			loadDerived( action , node );
		else
		if( CATEGORY == DBEnumScopeCategoryType.DB )
			loadDatabase( action , node );
		else
		if( CATEGORY == DBEnumScopeCategoryType.DOC )
			loadDoc( action , node );
		else
			action.exitUnexpectedCategory( CATEGORY );
	}
	
	private void loadProject( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , EnumNameType.ALPHANUMDOTDASH );
		BUILDBRANCH = ConfReader.getAttrValue( node , ReleaseTarget.PROPERTY_BUILDBRANCH , BUILDBRANCH );
		BUILDTAG = ConfReader.getAttrValue( node , ReleaseTarget.PROPERTY_BUILDTAG , BUILDTAG );
		BUILDVERSION = ConfReader.getAttrValue( node , ReleaseTarget.PROPERTY_BUILDVERSION , BUILDVERSION );
		
		// find in sources
		MetaSources sources = meta.getSources(); 
		sourceProject = sources.getProject( name ); 
		NAME = sourceProject.NAME;
		
		Node[] items = ConfReader.xmlGetChildren( node , Release.ELEMENT_DISTITEM );
		if( items == null ) {
			ALL = ConfReader.getBooleanAttrValue( node , ReleaseTarget.PROPERTY_ALL , false );
			if( ALL )
				addAllSourceItems( action );
			return;
		}

		ALL = false;
		for( Node inode : items ) {
			ReleaseScopeItem item = new ReleaseScopeItem( meta , this );
			item.loadSourceItem( action , inode );
			itemMap.put( item.NAME , item );
		}
	}

	private void loadConfiguration( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , EnumNameType.ALPHANUMDOT );
		MetaDistr distr = meta.getDistr(); 
		distConfItem = distr.getConfItem( name );
		this.NAME = name;
		
		ALL = ConfReader.getBooleanAttrValue( node , ReleaseTarget.PROPERTY_ALL , true );
	}
	
	private void loadManual( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , EnumNameType.ALPHANUMDOT );
		MetaDistr distr = meta.getDistr(); 
		distManualItem = distr.getBinaryItem( name );
		this.NAME = name;
		
		ALL = true;
	}

	private void loadDerived( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , EnumNameType.ALPHANUMDOT );
		MetaDistr distr = meta.getDistr(); 
		distDerivedItem = distr.getBinaryItem( name );
		this.NAME = name;
		
		ALL = true;
	}

	private void loadDatabase( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , EnumNameType.ALPHANUMDOT );
		MetaDistr distr = meta.getDistr(); 
		distDelivery = distr.getDelivery( name );
		this.NAME = name;

		// add database
		Node[] items = ConfReader.xmlGetChildren( node , Release.ELEMENT_SCHEMA );
		if( items == null ) {
			ALL = ConfReader.getBooleanAttrValue( node , ReleaseTarget.PROPERTY_ALL , false );
			if( ALL )
				addAllDeliverySchemes( action );
			return;
		}

		ALL = false;
		for( Node inode : items ) {
			ReleaseScopeItem item = new ReleaseScopeItem( meta , this );
			item.loadDatabaseItem( action , inode );
			itemMap.put( item.NAME , item );
		}
	}

	private void loadDoc( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , EnumNameType.ALPHANUMDOT );
		MetaDistr distr = meta.getDistr(); 
		distDelivery = distr.getDelivery( name );
		this.NAME = name;

		// add documents
		Node[] items = ConfReader.xmlGetChildren( node , Release.ELEMENT_DOC );
		if( items == null ) {
			ALL = ConfReader.getBooleanAttrValue( node , ReleaseTarget.PROPERTY_ALL , false );
			if( ALL )
				addAllDeliveryDocs( action );
			return;
		}

		ALL = false;
		for( Node inode : items ) {
			ReleaseScopeItem item = new ReleaseScopeItem( meta , this );
			item.loadDocItem( action , inode );
			itemMap.put( item.NAME , item );
		}
	}

	public void addReleaseTarget( ActionBase action , ReleaseScopeTarget srctarget ) throws Exception {
		for( Entry<String,ReleaseScopeItem> entry : srctarget.itemMap.entrySet() ) {
			ReleaseScopeItem srcitem = entry.getValue();
			ReleaseScopeItem item = itemMap.get( entry.getKey() );
			if( item == null ) {
				item = srcitem.copy( action , set.release , set , this );
				itemMap.put( entry.getKey() , item );
			}
		}
	}
	
	public void setDistFile( ActionBase action , String DISTFILE ) throws Exception {
		this.DISTFILE = DISTFILE;
	}
	
	public boolean isCategoryItem( ActionBase action , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		if( CATEGORY.isSourceCategory() ) {
			if( sourceProject != null )
				return( true );
		}
		else if( CATEGORY == DBEnumScopeCategoryType.CONFIG ) {
			if( distConfItem != null )
				return( true );
		}
		else if( CATEGORY == DBEnumScopeCategoryType.MANUAL ) {
			if( distManualItem != null )
				return( true );
		}
		else if( CATEGORY == DBEnumScopeCategoryType.DERIVED ) {
			if( distDerivedItem != null )
				return( true );
		}
		else if( CATEGORY == DBEnumScopeCategoryType.DB ) {
			if( this.CATEGORY == CATEGORY )
				return( true );
		}
		else if( CATEGORY == DBEnumScopeCategoryType.DOC ) {
			if( this.CATEGORY == CATEGORY )
				return( true );
		}
		
		action.exitUnexpectedState();
		return( false );
	}
	
	public boolean isSourceTarget() {
		if( sourceProject != null )
			return( true );
		return( false );
	}
	
	public boolean isManualTarget() {
		if( distManualItem != null )
			return( true );
		return( false );
	}
	
	public boolean isDerivedTarget() {
		if( distDerivedItem != null )
			return( true );
		return( false );
	}
	
	public boolean isBinaryTarget() {
		if( isSourceTarget() || isManualTarget() || isDerivedTarget() )
			return( true );
		return( false );
	}
	
	public boolean isConfTarget() {
		if( distConfItem != null )
			return( true );
		return( false );
	}
	
	public boolean isDatabaseTarget() {
		if( CATEGORY == DBEnumScopeCategoryType.DB )
			return( true );
		return( false );
	}

	public boolean isDocTarget() {
		if( CATEGORY == DBEnumScopeCategoryType.DOC )
			return( true );
		return( false );
	}

	public void createFromProject( ActionBase action , MetaSourceProject sourceProject , boolean allItems ) throws Exception {
		this.sourceProject = sourceProject;
		this.CATEGORY = DBEnumScopeCategoryType.PROJECT;
		
		NAME = sourceProject.NAME;
		ALL = false;
		BUILDBRANCH = "";
		BUILDTAG = "";
		BUILDVERSION = "";
		
		// find in sources
		if( allItems )
			addAllSourceItems( action );
	}

	public void createFromConfItem( ActionBase action , MetaDistrConfItem item , boolean allFiles ) throws Exception {
		this.distConfItem = item;
		this.CATEGORY = DBEnumScopeCategoryType.CONFIG;
		this.ALL = allFiles;
		this.NAME = item.NAME;
	}
	
	public void createFromManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.ITEMORIGIN_TYPE != DBEnumItemOriginType.MANUAL )
			action.exit1( _Error.UnexpectedNonManualItem1 , "unexpected non-manual item=" + item.NAME , item.NAME );
		
		this.distManualItem = item;
		this.CATEGORY = DBEnumScopeCategoryType.MANUAL;
		this.ALL = true;
		this.NAME = item.NAME;
	}
	
	public void createFromDerivedItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.ITEMORIGIN_TYPE != DBEnumItemOriginType.DERIVED )
			action.exit1( _Error.UnexpectedNonManualItem1 , "unexpected non-derived item=" + item.NAME , item.NAME );
		
		this.distDerivedItem = item;
		this.CATEGORY = DBEnumScopeCategoryType.DERIVED;
		this.ALL = true;
		this.NAME = item.NAME;
	}
	
	public void createFromDatabaseDelivery( ActionBase action , MetaDistrDelivery delivery , boolean allSchemes ) throws Exception {
		this.distDelivery = delivery;
		this.CATEGORY = DBEnumScopeCategoryType.DB;
		this.ALL = false;
		this.NAME = delivery.NAME;
		
		if( allSchemes )
			addAllDeliverySchemes( action );
	}
	
	public void createFromDocDelivery( ActionBase action , MetaDistrDelivery delivery , boolean allDocs ) throws Exception {
		this.distDelivery = delivery;
		this.CATEGORY = DBEnumScopeCategoryType.DOC;
		this.ALL = false;
		this.NAME = delivery.NAME;
		
		if( allDocs )
			addAllDeliveryDocs( action );
	}
	
	public void setAll( ActionBase action , boolean ALL ) throws Exception {
		this.ALL = ALL;
	}
	
	public boolean checkPropsEqualsToOptions( ActionBase action ) throws Exception {
		boolean change = false; 
		if( action.context.CTX_BRANCH.isEmpty() == false && !this.BUILDBRANCH.equals( action.context.CTX_BRANCH ) )
			change = true; 
		if( action.context.CTX_TAG.isEmpty() == false && !this.BUILDTAG.equals( action.context.CTX_TAG ) )
			change = true; 
		if( action.context.CTX_VERSION.isEmpty() == false && !this.BUILDVERSION.equals( action.context.CTX_VERSION ) )
			change = true; 

		if( change ) {
			action.error( NAME + " project attributes are different, please delete first" );
			return( false );
		}
		
		return( true );
	}
	
	public ReleaseScopeItem[] addSourceItem( ActionBase action , MetaSourceProjectItem projectitem ) throws Exception {
		// ignore internal items
		if( projectitem.isInternal() )
			return( new ReleaseScopeItem[0] );
		
		List<ReleaseScopeItem> list = new LinkedList<ReleaseScopeItem>();
		MetaDistr distr = meta.getDistr();
		for( MetaDistrBinaryItem distItem : distr.getBinaryItems() ) {
			if( distItem.sourceProjectItem == projectitem ) {
				ReleaseScopeItem item = new ReleaseScopeItem( meta , this );
				item.createFromDistrItem( action , distItem );
				itemMap.put( item.NAME , item );
				list.add( item );
			}
		}
		return( list.toArray( new ReleaseScopeItem[0] ) );
	}
	
	public ReleaseScopeItem addDeliverySchema( ActionBase action , MetaDatabaseSchema schema ) throws Exception {
		ReleaseScopeItem item = new ReleaseScopeItem( meta , this );
		item.createFromSchema( action , schema );
		itemMap.put( item.NAME , item );
		return( item );
	}
	
	public ReleaseScopeItem addDeliveryDoc( ActionBase action , MetaProductDoc doc ) throws Exception {
		ReleaseScopeItem item = new ReleaseScopeItem( meta , this );
		item.createFromDoc( action , doc );
		itemMap.put( item.NAME , item );
		return( item );
	}
	
	public String[] getItemNames() {
		return( Common.getSortedKeys( itemMap ) );
	}
	
	public ReleaseScopeItem findItem( String NAME ) {
		return( itemMap.get( NAME ) );
	}
	
	public ReleaseScopeItem findProjectItem( MetaSourceProjectItem item ) {
		return( itemMap.get( item.NAME ) );
	}
	
	public ReleaseScopeItem findDistItem( MetaDistrBinaryItem item ) {
		if( isSourceTarget() ) {
			if( item.isProjectItem() )
				return( findProjectItem( item.sourceProjectItem ) );
			return( null );
		}
			
		return( itemMap.get( item.NAME ) );
	}
	
	public ReleaseScopeItem findDeliverySchema( MetaDatabaseSchema schema ) {
		return( itemMap.get( schema.NAME ) );
	}
	
	public ReleaseScopeItem findDeliveryDoc( MetaProductDoc doc ) {
		return( itemMap.get( doc.NAME ) );
	}
	
	public void addAllSourceItems( ActionBase action ) throws Exception {
		ALL = true;
		
		// read source items
		for( MetaSourceProjectItem projectitem : sourceProject.getItems() )
			addSourceItem( action , projectitem );
	}

	public void addAllDeliverySchemes( ActionBase action ) throws Exception {
		ALL = true;
		
		// read source items
		for( MetaDatabaseSchema schema : distDelivery.getDatabaseSchemes() )
			addDeliverySchema( action , schema );
	}

	public void addAllDeliveryDocs( ActionBase action ) throws Exception {
		ALL = true;
		
		// read source items
		for( MetaProductDoc doc : distDelivery.getDocs() )
			addDeliveryDoc( action , doc );
	}

	public ReleaseScopeItem[] getItems() {
		return( itemMap.values().toArray( new ReleaseScopeItem[0] ) );
	}
	
	public MetaDistrDelivery getDelivery( ActionBase action ) throws Exception {
		if( distConfItem != null )
			return( distConfItem.delivery );
		if( distDelivery != null )
			return( distDelivery );
		if( distManualItem != null )
			return( distManualItem.delivery );
		
		action.exitUnexpectedCategory( CATEGORY );
		return( null );
	}
	
	public String getSpecifics() {
		if( isSourceTarget() )
			return( getSpecificsProject() );
		if( isConfTarget() )
			return( getSpecificsConfig() );
		if( isManualTarget() )
			return( getSpecificsManual() );
		if( isDerivedTarget() )
			return( getSpecificsDerived() );
		if( isDatabaseTarget() )
			return( getSpecificsDatabase() );
		if( isDocTarget() )
			return( getSpecificsDoc() );
		return( "" );
	}
	
	public String getSpecificsProject() {
		String s = "";
		if( ALL == false )
			s += "partial";
		if( !BUILDBRANCH.isEmpty() )
			s = Common.addToList( s , "BUILDBRANCH=" + BUILDBRANCH , ", " ); 
		if( !BUILDBRANCH.isEmpty() )
			s = Common.addToList( s , "BUILDTAG=" + BUILDTAG , ", " ); 
		if( !BUILDBRANCH.isEmpty() )
			s = Common.addToList( s , "BUILDVERSION=" + BUILDVERSION , ", " );
		return( s );
	}

	public String getSpecificsConfig() {
		if( !ALL )
			return( "partial" );
		return( "" );
	}
	
	public String getSpecificsManual() {
		return( "" );
	}
	
	public String getSpecificsDerived() {
		return( "" );
	}
	
	public String getSpecificsDatabase() {
		if( !ALL )
			return( "partial" );
		return( "" );
	}
	
	public String getSpecificsDoc() {
		if( !ALL )
			return( "partial" );
		return( "" );
	}
	
	public boolean isEmpty() {
		return( itemMap.isEmpty() );
	}

	public Element createXml( ActionBase action , Document doc , Element parent ) throws Exception {
		if( isSourceTarget() )
			return( createXmlBinary( action , doc , parent ) );
		if( isConfTarget() )
			return( createXmlConfig( action , doc , parent ) );
		if( isManualTarget() )
			return( createXmlManual( action , doc , parent ) );
		if( isDerivedTarget() )
			return( createXmlDerived( action , doc , parent ) );
		if( isDatabaseTarget() )
			return( createXmlDatabase( action , doc , parent ) );
		if( isDocTarget() )
			return( createXmlDoc( action , doc , parent ) );
		return( null );
	}
	
	public Element createXmlBinary( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_PROJECT );
		
		Meta.setNameAttr( action , doc , element , EnumNameType.ALPHANUMDOTDASH , sourceProject.NAME );
		if( !BUILDBRANCH.isEmpty() )
			Common.xmlSetElementAttr( doc , element , ReleaseTarget.PROPERTY_BUILDBRANCH , BUILDBRANCH );
		if( !BUILDTAG.isEmpty() )
			Common.xmlSetElementAttr( doc , element , ReleaseTarget.PROPERTY_BUILDTAG , BUILDTAG );
		if( !BUILDVERSION.isEmpty() )
			Common.xmlSetElementAttr( doc , element , ReleaseTarget.PROPERTY_BUILDVERSION , BUILDVERSION );

		// all project items
		if( ALL ) {
			Common.xmlSetElementAttr( doc , element , ReleaseTarget.PROPERTY_ALL , Common.getBooleanValue( true ) );
			return( element );
		}
		
		// selected items
		for( ReleaseScopeItem item : itemMap.values() )
			item.createXml( action , doc , element );
		
		return( element );
	}
	
	public Element createXmlConfig( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_CONFITEM );
		Meta.setNameAttr( action , doc , element , EnumNameType.ALPHANUMDOTDASH , distConfItem.NAME );
		if( ALL )
			Common.xmlSetElementAttr( doc , element , ReleaseTarget.PROPERTY_ALL , Common.getBooleanValue( true ) );
		return( element );
	}

	public Element createXmlManual( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_DISTITEM );
		Meta.setNameAttr( action , doc , element , EnumNameType.ALPHANUMDOTDASH , distManualItem.NAME );
		return( element );
	}

	public Element createXmlDerived( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_DISTITEM );
		Meta.setNameAttr( action , doc , element , EnumNameType.ALPHANUMDOTDASH , distDerivedItem.NAME );
		return( element );
	}

	public Element createXmlDatabase( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_DELIVERY );
		Meta.setNameAttr( action , doc , element , EnumNameType.ALPHANUMDOTDASH , distDelivery.NAME );
		
		// all project items
		if( ALL ) {
			Common.xmlSetElementAttr( doc , element , ReleaseTarget.PROPERTY_ALL , Common.getBooleanValue( true ) );
			return( element );
		}
		
		// selected items
		for( ReleaseScopeItem item : itemMap.values() )
			item.createXml( action , doc , element );
		
		return( element );
	}

	public Element createXmlDoc( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_DELIVERY );
		Meta.setNameAttr( action , doc , element , EnumNameType.ALPHANUMDOTDASH , distDelivery.NAME );
		
		// all project items
		if( ALL ) {
			Common.xmlSetElementAttr( doc , element , ReleaseTarget.PROPERTY_ALL , Common.getBooleanValue( true ) );
			return( element );
		}
		
		// selected items
		for( ReleaseScopeItem item : itemMap.values() )
			item.createXml( action , doc , element );
		
		return( element );
	}

	public void removeSourceItem( ActionBase action , ReleaseScopeItem buildItem ) throws Exception {
		itemMap.remove( buildItem.NAME );
		ALL = false;
	}

	public void removeDatabaseItem( ActionBase action , ReleaseScopeItem databaseItem ) throws Exception {
		itemMap.remove( databaseItem.NAME );
		ALL = false;
	}

	public void removeDocItem( ActionBase action , ReleaseScopeItem docItem ) throws Exception {
		itemMap.remove( docItem.NAME );
		ALL = false;
	}

	public boolean isBuildableProject() {
		if( CATEGORY == DBEnumScopeCategoryType.PROJECT && sourceProject.isBuildable() )
			return( true );
		return( false );
	}
	
	public boolean isPrebuiltProject() {
		if( CATEGORY == DBEnumScopeCategoryType.PROJECT && !sourceProject.isBuildable() )
			return( true );
		return( false );
	}

	public void setSpecifics( ActionBase action , String BUILDBRANCH , String BUILDTAG , String BUILDVERSION ) throws Exception {
		this.BUILDBRANCH = BUILDBRANCH; 
		this.BUILDTAG = BUILDTAG; 
		this.BUILDVERSION = BUILDVERSION;
	}
	
}
