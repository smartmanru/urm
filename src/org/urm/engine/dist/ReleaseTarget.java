package org.urm.engine.dist;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.Types;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseTarget {

	Meta meta;
	public ReleaseDistSet set;
	public VarCATEGORY CATEGORY;

	public boolean ALL;
	public String NAME = "";
	
	public String BUILDBRANCH = ""; 
	public String BUILDTAG = ""; 
	public String BUILDVERSION = "";
	
	public MetaSourceProject sourceProject;
	public MetaDistrConfItem distConfItem;
	public MetaDistrDelivery distDatabaseDelivery;
	public MetaDistrBinaryItem distManualItem;
	public MetaDistrBinaryItem distDerivedItem;
	
	Map<String,ReleaseTargetItem> itemMap = new HashMap<String,ReleaseTargetItem>();
	
	public String DISTFILE;

	public ReleaseTarget( Meta meta , ReleaseDistSet set , VarCATEGORY CATEGORY ) {
		this.meta = meta;
		this.set = set;
		this.CATEGORY = CATEGORY;
	}

	public ReleaseTarget copy( ActionBase action , Release nr , ReleaseDistSet ns ) throws Exception {
		ReleaseTarget nx = new ReleaseTarget( ns.meta , ns , CATEGORY );
		
		nx.ALL = ALL;
		nx.NAME = NAME;
		
		nx.BUILDBRANCH = BUILDBRANCH; 
		nx.BUILDTAG = BUILDTAG; 
		nx.BUILDVERSION = BUILDVERSION;
		
		nx.sourceProject = ( sourceProject == null )? null : ns.set.getProject( sourceProject.NAME );
		MetaDistr ndistr = ns.meta.getDistr();
		nx.distConfItem = ( distConfItem == null )? null : ndistr.getConfItem( distConfItem.NAME );
		nx.distDatabaseDelivery = ( distDatabaseDelivery == null )? null : ndistr.getDelivery( distDatabaseDelivery.NAME );
		nx.distManualItem = ( distManualItem == null )? null : ndistr.getBinaryItem( distManualItem.NAME );
		nx.distDerivedItem = ( distDerivedItem == null )? null : ndistr.getBinaryItem( distDerivedItem.NAME );

		for( Entry<String,ReleaseTargetItem> entry : itemMap.entrySet() ) {
			ReleaseTargetItem item = entry.getValue().copy( action , nr , ns , nx );
			nx.itemMap.put( entry.getKey() , item );
		}
		
		return( nx );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( Types.isSourceCategory( CATEGORY ) )
			loadProject( action , node );
		else
		if( CATEGORY == VarCATEGORY.CONFIG )
			loadConfiguration( action , node );
		else
		if( CATEGORY == VarCATEGORY.DB )
			loadDatabase( action , node );
		else
		if( CATEGORY == VarCATEGORY.MANUAL )
			loadManual( action , node );
		else
		if( CATEGORY == VarCATEGORY.DERIVED )
			loadDerived( action , node );
		else
			action.exitUnexpectedCategory( CATEGORY );
	}
	
	private void loadProject( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
		BUILDBRANCH = ConfReader.getAttrValue( node , Release.PROPERTY_BUILDBRANCH , BUILDBRANCH );
		BUILDTAG = ConfReader.getAttrValue( node , Release.PROPERTY_BUILDTAG , BUILDTAG );
		BUILDVERSION = ConfReader.getAttrValue( node , Release.PROPERTY_BUILDVERSION , BUILDVERSION );
		
		// find in sources
		MetaSources sources = meta.getSources(); 
		sourceProject = sources.getProject( name ); 
		NAME = sourceProject.NAME;
		
		Node[] items = ConfReader.xmlGetChildren( node , Release.ELEMENT_DISTITEM );
		if( items == null ) {
			ALL = ConfReader.getBooleanAttrValue( node , Release.PROPERTY_ALL , false );
			if( ALL )
				addAllSourceItems( action );
			return;
		}

		ALL = false;
		for( Node inode : items ) {
			ReleaseTargetItem item = new ReleaseTargetItem( meta , this );
			item.loadSourceItem( action , inode );
			itemMap.put( item.NAME , item );
		}
	}

	private void loadConfiguration( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		MetaDistr distr = meta.getDistr(); 
		distConfItem = distr.getConfItem( name );
		this.NAME = name;
		
		ALL = ( ConfReader.getBooleanAttrValue( node , Release.PROPERTY_PARTIAL , true ) )? false : true;
	}
	
	private void loadDatabase( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		MetaDistr distr = meta.getDistr(); 
		distDatabaseDelivery = distr.getDelivery( name );
		this.NAME = name;

		// add database
		Node[] items = ConfReader.xmlGetChildren( node , Release.ELEMENT_SCHEMA );
		if( items == null ) {
			ALL = ConfReader.getBooleanAttrValue( node , Release.PROPERTY_ALL , false );
			if( ALL )
				addAllDatabaseSchemes( action );
			return;
		}

		ALL = false;
		for( Node inode : items ) {
			ReleaseTargetItem item = new ReleaseTargetItem( meta , this );
			item.loadDatabaseItem( action , inode );
			itemMap.put( item.NAME , item );
		}
	}

	private void loadManual( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		MetaDistr distr = meta.getDistr(); 
		distManualItem = distr.getBinaryItem( name );
		this.NAME = name;
		
		ALL = true;
	}

	private void loadDerived( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		MetaDistr distr = meta.getDistr(); 
		distDerivedItem = distr.getBinaryItem( name );
		this.NAME = name;
		
		ALL = true;
	}

	public void addReleaseTarget( ActionBase action , ReleaseTarget srctarget ) throws Exception {
		for( Entry<String,ReleaseTargetItem> entry : srctarget.itemMap.entrySet() ) {
			ReleaseTargetItem srcitem = entry.getValue();
			ReleaseTargetItem item = itemMap.get( entry.getKey() );
			if( item == null ) {
				item = srcitem.copy( action , set.release , set , this );
				itemMap.put( entry.getKey() , item );
			}
		}
	}
	
	public void setDistFile( ActionBase action , String DISTFILE ) throws Exception {
		this.DISTFILE = DISTFILE;
	}
	
	public boolean isCategoryItem( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		if( Types.isSourceCategory( CATEGORY ) ) {
			if( sourceProject != null )
				return( true );
		}
		else if( CATEGORY == VarCATEGORY.CONFIG ) {
			if( distConfItem != null )
				return( true );
		}
		else if( CATEGORY == VarCATEGORY.DB ) {
			if( distDatabaseDelivery != null )
				return( true );
		}
		else if( CATEGORY == VarCATEGORY.MANUAL ) {
			if( distManualItem != null )
				return( true );
		}
		else if( CATEGORY == VarCATEGORY.DERIVED ) {
			if( distDerivedItem != null )
				return( true );
		}
		
		action.exitUnexpectedState();
		return( false );
	}
	
	public boolean isProjectTarget() {
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
		if( isProjectTarget() || isManualTarget() || isDerivedTarget() )
			return( true );
		return( false );
	}
	
	public boolean isConfTarget() {
		if( distConfItem != null )
			return( true );
		return( false );
	}
	
	public boolean isDatabaseTarget() {
		if( distDatabaseDelivery != null )
			return( true );
		return( false );
	}

	public void createFromProject( ActionBase action , MetaSourceProject sourceProject , boolean allItems ) throws Exception {
		this.sourceProject = sourceProject;
		this.CATEGORY = VarCATEGORY.PROJECT;
		
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
		this.CATEGORY = VarCATEGORY.CONFIG;
		this.ALL = allFiles;
		this.NAME = item.NAME;
	}
	
	public void createFromDatabaseDelivery( ActionBase action , MetaDistrDelivery delivery , boolean allSchemes ) throws Exception {
		this.distDatabaseDelivery = delivery;
		this.CATEGORY = VarCATEGORY.DB;
		this.ALL = false;
		this.NAME = delivery.NAME;
		
		if( allSchemes )
			addAllDatabaseSchemes( action );
	}
	
	public void createFromManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.ITEMORIGIN_TYPE != DBEnumItemOriginType.MANUAL )
			action.exit1( _Error.UnexpectedNonManualItem1 , "unexpected non-manual item=" + item.NAME , item.NAME );
		
		this.distManualItem = item;
		this.CATEGORY = VarCATEGORY.MANUAL;
		this.ALL = true;
		this.NAME = item.NAME;
	}
	
	public void createFromDerivedItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.ITEMORIGIN_TYPE != DBEnumItemOriginType.DERIVED )
			action.exit1( _Error.UnexpectedNonManualItem1 , "unexpected non-derived item=" + item.NAME , item.NAME );
		
		this.distDerivedItem = item;
		this.CATEGORY = VarCATEGORY.DERIVED;
		this.ALL = true;
		this.NAME = item.NAME;
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
	
	public ReleaseTargetItem[] addSourceItem( ActionBase action , MetaSourceProjectItem projectitem ) throws Exception {
		// ignore internal items
		if( projectitem.isInternal() )
			return( new ReleaseTargetItem[0] );
		
		List<ReleaseTargetItem> list = new LinkedList<ReleaseTargetItem>();
		MetaDistr distr = meta.getDistr();
		for( MetaDistrBinaryItem distItem : distr.getBinaryItems() ) {
			if( distItem.sourceProjectItem == projectitem ) {
				ReleaseTargetItem item = new ReleaseTargetItem( meta , this );
				item.createFromDistrItem( action , distItem );
				itemMap.put( item.NAME , item );
				list.add( item );
			}
		}
		return( list.toArray( new ReleaseTargetItem[0] ) );
	}
	
	public ReleaseTargetItem addDatabaseSchema( ActionBase action , MetaDatabaseSchema schema ) throws Exception {
		ReleaseTargetItem item = new ReleaseTargetItem( meta , this );
		item.createFromSchema( action , schema );
		itemMap.put( item.NAME , item );
		return( item );
	}
	
	public String[] getItemNames() {
		return( Common.getSortedKeys( itemMap ) );
	}
	
	public ReleaseTargetItem findItem( String NAME ) {
		return( itemMap.get( NAME ) );
	}
	
	public ReleaseTargetItem findDatabaseSchema( MetaDatabaseSchema schema ) {
		return( itemMap.get( schema.NAME ) );
	}
	
	public ReleaseTargetItem findProjectItem( MetaSourceProjectItem item ) {
		return( itemMap.get( item.NAME ) );
	}
	
	public ReleaseTargetItem findDistItem( MetaDistrBinaryItem item ) {
		if( isProjectTarget() ) {
			if( item.isProjectItem() )
				return( findProjectItem( item.sourceProjectItem ) );
			return( null );
		}
			
		return( itemMap.get( item.NAME ) );
	}
	
	public void addAllSourceItems( ActionBase action ) throws Exception {
		ALL = true;
		
		// read source items
		for( MetaSourceProjectItem projectitem : sourceProject.getItems() )
			addSourceItem( action , projectitem );
	}

	public void addAllDatabaseSchemes( ActionBase action ) throws Exception {
		ALL = true;
		
		// read source items
		for( MetaDatabaseSchema schema : distDatabaseDelivery.getDatabaseSchemes() )
			addDatabaseSchema( action , schema );
	}

	public ReleaseTargetItem[] getItems() {
		return( itemMap.values().toArray( new ReleaseTargetItem[0] ) );
	}
	
	public MetaDistrDelivery getDelivery( ActionBase action ) throws Exception {
		if( distConfItem != null )
			return( distConfItem.delivery );
		if( distDatabaseDelivery != null )
			return( distDatabaseDelivery );
		if( distManualItem != null )
			return( distManualItem.delivery );
		
		action.exitUnexpectedCategory( CATEGORY );
		return( null );
	}
	
	public String getSpecifics() {
		if( sourceProject != null )
			return( getSpecificsProject() );
		if( distConfItem != null )
			return( getSpecificsConfig() );
		if( distDatabaseDelivery != null )
			return( getSpecificsDatabase() );
		if( distManualItem != null )
			return( getSpecificsManual() );
		if( distDerivedItem != null )
			return( getSpecificsDerived() );
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
	
	public String getSpecificsDatabase() {
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
	
	public boolean isEmpty() {
		return( itemMap.isEmpty() );
	}

	public Element createXml( ActionBase action , Document doc , Element parent ) throws Exception {
		if( sourceProject != null )
			return( createXmlBinary( action , doc , parent ) );
		if( distConfItem != null )
			return( createXmlConfig( action , doc , parent ) );
		if( distDatabaseDelivery != null )
			return( createXmlDatabase( action , doc , parent ) );
		if( distManualItem != null )
			return( createXmlManual( action , doc , parent ) );
		if( distManualItem != null )
			return( createXmlDerived( action , doc , parent ) );
		return( null );
	}
	
	public Element createXmlBinary( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_PROJECT );
		
		Meta.setNameAttr( action , doc , element , VarNAMETYPE.ALPHANUMDOTDASH , sourceProject.NAME );
		if( !BUILDBRANCH.isEmpty() )
			Common.xmlSetElementAttr( doc , element , Release.PROPERTY_BUILDBRANCH , BUILDBRANCH );
		if( !BUILDTAG.isEmpty() )
			Common.xmlSetElementAttr( doc , element , Release.PROPERTY_BUILDTAG , BUILDTAG );
		if( !BUILDVERSION.isEmpty() )
			Common.xmlSetElementAttr( doc , element , Release.PROPERTY_BUILDVERSION , BUILDVERSION );

		// all project items
		if( ALL ) {
			Common.xmlSetElementAttr( doc , element , Release.PROPERTY_ALL , Common.getBooleanValue( true ) );
			return( element );
		}
		
		// selected items
		for( ReleaseTargetItem item : itemMap.values() )
			item.createXml( action , doc , element );
		
		return( element );
	}
	
	public Element createXmlConfig( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_CONFITEM );
		Meta.setNameAttr( action , doc , element , VarNAMETYPE.ALPHANUMDOTDASH , distConfItem.NAME );
		String partial = Common.getBooleanValue( !ALL ); 
		Common.xmlSetElementAttr( doc , element , Release.PROPERTY_PARTIAL , partial );
		return( element );
	}

	public Element createXmlDatabase( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_DELIVERY );
		Meta.setNameAttr( action , doc , element , VarNAMETYPE.ALPHANUMDOTDASH , distDatabaseDelivery.NAME );
		
		// all project items
		if( ALL ) {
			Common.xmlSetElementAttr( doc , element , Release.PROPERTY_ALL , Common.getBooleanValue( true ) );
			return( element );
		}
		
		// selected items
		for( ReleaseTargetItem item : itemMap.values() )
			item.createXml( action , doc , element );
		
		return( element );
	}

	public Element createXmlManual( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_DISTITEM );
		Meta.setNameAttr( action , doc , element , VarNAMETYPE.ALPHANUMDOTDASH , distManualItem.NAME );
		return( element );
	}

	public Element createXmlDerived( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_DISTITEM );
		Meta.setNameAttr( action , doc , element , VarNAMETYPE.ALPHANUMDOTDASH , distDerivedItem.NAME );
		return( element );
	}

	public void removeSourceItem( ActionBase action , ReleaseTargetItem buildItem ) throws Exception {
		itemMap.remove( buildItem.NAME );
		ALL = false;
	}

	public void removeDatabaseItem( ActionBase action , ReleaseTargetItem databaseItem ) throws Exception {
		itemMap.remove( databaseItem.NAME );
		ALL = false;
	}

	public boolean isBuildableProject() {
		if( CATEGORY == VarCATEGORY.PROJECT && sourceProject.isBuildable() )
			return( true );
		return( false );
	}
	
	public boolean isPrebuiltProject() {
		if( CATEGORY == VarCATEGORY.PROJECT && !sourceProject.isBuildable() )
			return( true );
		return( false );
	}

	public void setSpecifics( ActionBase action , String BUILDBRANCH , String BUILDTAG , String BUILDVERSION ) throws Exception {
		this.BUILDBRANCH = BUILDBRANCH; 
		this.BUILDTAG = BUILDTAG; 
		this.BUILDVERSION = BUILDVERSION;
	}
	
}
