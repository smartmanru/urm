package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.Types;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseDistSet {

	Meta meta;
	Release release;
	public EnumScopeCategory CATEGORY;
	
	public MetaSourceProjectSet set;
	
	public String NAME;
	public boolean ALL;
	public String BUILDBRANCH = "";
	public String BUILDTAG = "";
	public String BUILDVERSION = "";

	Map<String,ReleaseTarget> map = new HashMap<String,ReleaseTarget>(); 
	
	public ReleaseDistSet( Meta meta , Release release , EnumScopeCategory CATEGORY ) {
		this.meta = meta;
		this.release = release;
		this.CATEGORY = CATEGORY;
	}
	
	public ReleaseDistSet copy( ActionBase action , Release nr ) throws Exception {
		ReleaseDistSet nx = new ReleaseDistSet( nr.meta , nr , CATEGORY );
		nx.NAME = NAME;
		nx.ALL = ALL;
		nx.BUILDBRANCH = BUILDBRANCH;
		nx.BUILDTAG = BUILDTAG;
		nx.BUILDVERSION = BUILDVERSION;
		if( set != null ) {
			MetaSources nsources = nr.meta.getSources();
			nx.set = nsources.getProjectSet( set.NAME );
		}
		
		for( Entry<String,ReleaseTarget> entry : map.entrySet() ) {
			ReleaseTarget item = entry.getValue().copy( action , nr , nx );
			nx.map.put( entry.getKey() , item );
		}
		
		return( nx );
	}
	
	public void addReleaseSet( ActionBase action , ReleaseDistSet srcset ) throws Exception {
		for( Entry<String,ReleaseTarget> entry : srcset.map.entrySet() ) {
			ReleaseTarget srcitem = entry.getValue();
			ReleaseTarget item = map.get( entry.getKey() );
			if( item == null ) {
				item = srcitem.copy( action , release , srcset );
				map.put( entry.getKey() , item );
			}
			else
				item.addReleaseTarget( action , item );
		}
	}
	
	public String getId() {
		return( set.NAME );
	}

	public boolean isSourceSet() {
		if( CATEGORY == EnumScopeCategory.PROJECT )
			return( true );
		return( false );
	}
	
	public boolean isCategorySet() {
		return( !isSourceSet() );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		ALL = ConfReader.getBooleanAttrValue( node , Release.PROPERTY_ALL , false );
		if( Types.isSourceCategory( CATEGORY ) )
			loadBinary( action , node );
		else {
			NAME = Common.getEnumLower( CATEGORY );
			if( CATEGORY == EnumScopeCategory.CONFIG )
				loadConfiguration( action , node );
			else
			if( CATEGORY == EnumScopeCategory.DB )
				loadDatabase( action , node );
			else
			if( CATEGORY == EnumScopeCategory.MANUAL )
				loadManual( action , node );
			else
			if( CATEGORY == EnumScopeCategory.DERIVED )
				loadDerived( action , node );
			else
				action.exitUnexpectedCategory( CATEGORY );
		}
	}

	private ReleaseTarget loadTarget( ActionBase action , Node node ) throws Exception {
		ReleaseTarget target = new ReleaseTarget( meta , this , CATEGORY );
		target.load( action , node );
		
		map.put( target.NAME , target );
		return( target );
	}
	
	private void loadBinary( ActionBase action , Node node ) throws Exception {
		String SET = action.getNameAttr( node , EnumNameType.ALPHANUMDOT );
		MetaSources sources = meta.getSources(); 
		set = sources.getProjectSet( SET );
		NAME = set.NAME;
		
		BUILDBRANCH = ConfReader.getAttrValue( node , Release.PROPERTY_BUILDBRANCH );
		BUILDTAG = ConfReader.getAttrValue( node , Release.PROPERTY_BUILDTAG );
		BUILDVERSION = ConfReader.getAttrValue( node , Release.PROPERTY_BUILDVERSION );

		Node[] projects = ConfReader.xmlGetChildren( node , Release.ELEMENT_PROJECT );
		if( ALL ) {
			if( projects == null || projects.length == 0 ) {
				addAllSourceProjects( action );
				return;
			}

			action.exit1( _Error.UnexpectedFullSetProjects1 , "unexpected projects defined for all=true in set=" + set.NAME , set.NAME );
		}

		if( projects == null )
			return;
		
		for( Node pnode : projects ) {
			ReleaseTarget buildProject = loadTarget( action , pnode ); 
			buildProject.BUILDBRANCH = BUILDBRANCH;
			buildProject.BUILDTAG = BUILDTAG;
			buildProject.BUILDVERSION = BUILDVERSION;
		}
	}
	
	private void loadConfiguration( ActionBase action , Node node ) throws Exception {
		NAME = Common.getEnumLower( EnumScopeCategory.CONFIG );
		ALL = ConfReader.getBooleanAttrValue( node , Release.PROPERTY_ALL , false );

		Node[] confitems = ConfReader.xmlGetChildren( node , Release.ELEMENT_CONFITEM );
		if( ALL ) {
			if( confitems == null || confitems.length == 0 ) {
				addAllConfItems( action );
				return;
			}

			action.exit0( _Error.UnexpectedFullSetConfigurationItems0 , "unexpected configuration items defined with all=true" );
		}

		if( confitems == null )
			return;
		
		for( Node pnode : confitems )
			loadTarget( action , pnode ); 
	}
	
	private void loadDatabase( ActionBase action , Node node ) throws Exception {
		NAME = Common.getEnumLower( EnumScopeCategory.DB );
		ALL = ConfReader.getBooleanAttrValue( node , Release.PROPERTY_ALL , false );

		Node[] dbitems = ConfReader.xmlGetChildren( node , Release.ELEMENT_DELIVERY );
		if( ALL ) {
			if( dbitems == null || dbitems.length == 0 ) {
				addAllDatabaseItems( action );
				return;
			}

			action.exit0( _Error.UnexpectedFullSetDatabaseItems0 , "unexpected database items defined with all=true" );
		}

		if( dbitems == null )
			return;
		
		for( Node pnode : dbitems )
			loadTarget( action , pnode ); 
	}

	private void loadManual( ActionBase action , Node node ) throws Exception {
		NAME = Common.getEnumLower( EnumScopeCategory.MANUAL );
		ALL = ConfReader.getBooleanAttrValue( node , Release.PROPERTY_ALL , false );

		Node[] manualitems = ConfReader.xmlGetChildren( node , Release.ELEMENT_DISTITEM );
		if( ALL ) {
			if( manualitems == null || manualitems.length == 0 ) {
				addAllManualItems( action );
				return;
			}

			action.exit0( _Error.UnexpectedFullSetManuaItems0 , "unexpected manual items defined with all=true" );
		}

		if( manualitems == null )
			return;
		
		for( Node pnode : manualitems )
			loadTarget( action , pnode ); 
	}

	private void loadDerived( ActionBase action , Node node ) throws Exception {
		NAME = Common.getEnumLower( EnumScopeCategory.DERIVED );
		ALL = ConfReader.getBooleanAttrValue( node , Release.PROPERTY_ALL , false );

		Node[] deriveditems = ConfReader.xmlGetChildren( node , Release.ELEMENT_DISTITEM );
		if( ALL ) {
			if( deriveditems == null || deriveditems.length == 0 ) {
				addAllDerivedItems( action );
				return;
			}

			action.exit0( _Error.UnexpectedFullSetDerivedItems0 , "unexpected derived items defined with all=true" );
		}

		if( deriveditems == null )
			return;
		
		for( Node pnode : deriveditems )
			loadTarget( action , pnode ); 
	}

	public void createSourceSet( ActionBase action , MetaSourceProjectSet set , boolean ALL ) throws Exception {
		this.set = set;
		this.NAME = set.NAME;
		this.CATEGORY = EnumScopeCategory.PROJECT;
		this.ALL = ALL;
		this.BUILDBRANCH = action.context.CTX_BRANCH;
		this.BUILDTAG = action.context.CTX_TAG;
		this.BUILDVERSION = action.context.CTX_VERSION;
		
		if( ALL )
			addAllSourceProjects( action );
	}
	
	public void createCategorySet( ActionBase action , EnumScopeCategory CATEGORY , boolean ALL ) throws Exception {
		this.CATEGORY = CATEGORY;
		this.NAME = Common.getEnumLower( CATEGORY );
		this.ALL = ALL;
		this.BUILDBRANCH = action.context.CTX_BRANCH;
		this.BUILDTAG = action.context.CTX_TAG;
		this.BUILDVERSION = action.context.CTX_VERSION;
		
		if( ALL ) {
			if( CATEGORY == EnumScopeCategory.CONFIG )
				addAllConfItems( action );
			else
			if( CATEGORY == EnumScopeCategory.DB )
				addAllDatabaseItems( action );
			else
			if( CATEGORY == EnumScopeCategory.MANUAL )
				addAllManualItems( action );
			else
			if( CATEGORY == EnumScopeCategory.DERIVED )
				addAllDerivedItems( action );
			else
				action.exitUnexpectedCategory( CATEGORY );
		}
	}
	
	public boolean checkPropsEqualsToOptions( ActionBase action ) throws Exception {
		if( this.BUILDBRANCH.equals( action.context.CTX_BRANCH ) &&
			this.BUILDTAG.equals( action.context.CTX_TAG ) &&
			this.BUILDVERSION.equals( action.context.CTX_VERSION ) )
			return( true );
		
		action.error( getId() + " set attributes are different, please delete first" );
		return( false );
	}
	
	public void addAllSourceProjects( ActionBase action ) throws Exception {
		action.trace( "add all source projects to release ..." );
		for( MetaSourceProject project : set.getProjects() )
			addSourceProject( action , project , true );
	}

	public ReleaseTarget addSourceProject( ActionBase action , MetaSourceProject sourceProject , boolean allItems ) throws Exception {
		action.trace( "add source project=" + sourceProject.NAME + " to release ..." );
		ReleaseTarget project = new ReleaseTarget( meta , this , CATEGORY );
		project.createFromProject( action , sourceProject , allItems );
		project.BUILDBRANCH = BUILDBRANCH;
		project.BUILDTAG = BUILDTAG;
		project.BUILDVERSION = BUILDVERSION;
		
		map.put( project.NAME , project );
		return( project );
	}

	public void addAllConfItems( ActionBase action ) throws Exception {
		MetaDistr distr = meta.getDistr(); 
		for( MetaDistrConfItem comp : distr.getConfItems() )
			addConfItem( action , comp , true );
	}

	public void addAllDatabaseItems( ActionBase action ) throws Exception {
		MetaDistr distr = meta.getDistr(); 
		for( MetaDistrDelivery delivery : distr.getDatabaseDeliveries() )
			addDatabaseDelivery( action , delivery , true );
	}

	public void addAllManualItems( ActionBase action ) throws Exception {
		MetaDistr distr = meta.getDistr(); 
		for( MetaDistrBinaryItem item : distr.getBinaryItems() ) {
			if( item.ITEMORIGIN_TYPE == DBEnumItemOriginType.MANUAL )
				addManualItem( action , item );
		}
	}

	public void addAllDerivedItems( ActionBase action ) throws Exception {
		MetaDistr distr = meta.getDistr(); 
		for( MetaDistrBinaryItem item : distr.getBinaryItems() ) {
			if( item.ITEMORIGIN_TYPE == DBEnumItemOriginType.DERIVED )
				addDerivedItem( action , item );
		}
	}

	public ReleaseTarget addConfItem( ActionBase action , MetaDistrConfItem item , boolean allFiles ) throws Exception {
		ReleaseTarget confItem = new ReleaseTarget( meta , this , CATEGORY );
		confItem.createFromConfItem( action , item , allFiles );
		
		map.put( confItem.NAME , confItem );
		return( confItem );
	}
	
	public ReleaseTarget addDatabaseDelivery( ActionBase action , MetaDistrDelivery delivery , boolean allSchemes ) throws Exception {
		ReleaseTarget dbItem = new ReleaseTarget( meta , this , CATEGORY );
		dbItem.createFromDatabaseDelivery( action , delivery , allSchemes );
		
		map.put( dbItem.NAME , dbItem );
		return( dbItem );
	}
	
	public ReleaseTarget addManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		ReleaseTarget manualItem = new ReleaseTarget( meta , this , CATEGORY );
		manualItem.createFromManualItem( action , item );
		
		map.put( manualItem.NAME , manualItem );
		return( manualItem );
	}

	public ReleaseTarget addDerivedItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		ReleaseTarget derivedItem = new ReleaseTarget( meta , this , CATEGORY );
		derivedItem.createFromDerivedItem( action , item );
		
		map.put( derivedItem.NAME , derivedItem );
		return( derivedItem );
	}

	public void removeTarget( ActionBase action , ReleaseTarget source ) throws Exception {
		map.remove( source.NAME );
		ALL = false;
	}
	
	public String[] getTargetNames() {
		return( Common.getSortedKeys( map ) );
	}
	
	public ReleaseTarget[] getTargets() {
		return( map.values().toArray( new ReleaseTarget[0] ) );
	}

	public String getProjectBranch( ActionBase action , String name ) throws Exception {
		ReleaseTarget project = findTarget( name );
		if( project == null )
			return( "" );
	
		if( !project.BUILDBRANCH.isEmpty() )
			return( project.BUILDBRANCH );
		
		return( BUILDBRANCH );
	}
	
	public String getProjectTag( ActionBase action , String name ) throws Exception {
		ReleaseTarget project = findTarget( name );
		if( project == null )
			return( "" );
	
		if( !project.BUILDTAG.isEmpty() )
			return( project.BUILDTAG );
		
		return( BUILDTAG );
	}

	public ReleaseTarget findTarget( String key ) {
 		return( map.get( key ) );
	}
	
	public ReleaseTarget getTarget( ActionBase action , String key ) throws Exception {
		ReleaseTarget source = findTarget( key );
		if( source == null || !source.isCategoryItem( action , CATEGORY ) )
			action.exit1( _Error.UnknownReleaseTarget1 , "unknown release target key=" + key , key );
		return( source );
	}

	public void makePartial( ActionBase action ) throws Exception {
		ALL = false;
	}
	
	public boolean isEmpty() {
		return( map.isEmpty() );
	}
	
	public String getSpecifics( ActionBase action ) throws Exception {
		String s = "";
		if( ALL )
			s += "all";
		if( !BUILDBRANCH.isEmpty() )
			s = Common.addToList( s , "BUILDBRANCH=" + BUILDBRANCH , ", " );
		if( !BUILDTAG.isEmpty() )
			s = Common.addToList( s , "BUILDTAG=" + BUILDTAG , ", " );
		if( !BUILDVERSION.isEmpty() )
			s = Common.addToList( s , "BUILDVERSION=" + BUILDVERSION , ", " );
		return( s );
	}

	public Element createXml( ActionBase action , Document doc , Element parent ) throws Exception {
		if( Types.isSourceCategory( CATEGORY ) )
			return( createXmlBinary( action , doc , parent ) );

		return( createXmlCategory( action , doc , parent ) );
	}
	
	public Element createXmlBinary( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , Release.ELEMENT_SET );
		
		Meta.setNameAttr( action , doc , element , EnumNameType.ALPHANUMDOTDASH , set.NAME );
		if( !BUILDBRANCH.isEmpty() )
			Common.xmlSetElementAttr( doc , element , Release.PROPERTY_BUILDBRANCH , BUILDBRANCH );
		if( !BUILDTAG.isEmpty() )
			Common.xmlSetElementAttr( doc , element , Release.PROPERTY_BUILDTAG , BUILDTAG );
		if( !BUILDVERSION.isEmpty() )
			Common.xmlSetElementAttr( doc , element , Release.PROPERTY_BUILDVERSION , BUILDVERSION );
		
		return( addXmlTargetList( action , doc , element ) );
	}
	
	public Element createXmlCategory( ActionBase action , Document doc , Element parent ) throws Exception {
		return( addXmlTargetList( action , doc , parent ) );
	}

	public Element addXmlTargetList( ActionBase action , Document doc , Element element ) throws Exception {
		if( ALL ) {
			Common.xmlSetElementAttr( doc , element , Release.PROPERTY_ALL , Common.getBooleanValue( true ) );
			return( element );
		}

		for( String key : Common.getSortedKeys( map ) ) {
			ReleaseTarget target = map.get( key );
			target.createXml( action , doc , element );
		}
		
		return( element );
	}

	public void setSpecifics( ActionBase action , String BUILDBRANCH , String BUILDTAG , String BUILDVERSION ) throws Exception {
		this.BUILDBRANCH = BUILDBRANCH; 
		this.BUILDTAG = BUILDTAG; 
		this.BUILDVERSION = BUILDVERSION;
	}
	
}
