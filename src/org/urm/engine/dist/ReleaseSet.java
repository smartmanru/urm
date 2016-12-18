package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseSet {

	Meta meta;
	Release release;
	public VarCATEGORY CATEGORY;
	
	public MetaSourceProjectSet set;
	
	public String NAME;
	public boolean ALL;
	public String BUILDBRANCH = "";
	public String BUILDTAG = "";
	public String BUILDVERSION = "";

	Map<String,ReleaseTarget> map = new HashMap<String,ReleaseTarget>(); 
	
	public ReleaseSet( Meta meta , Release release , VarCATEGORY CATEGORY ) {
		this.meta = meta;
		this.release = release;
		this.CATEGORY = CATEGORY;
	}
	
	public ReleaseSet copy( ActionBase action , Release nr ) throws Exception {
		ReleaseSet nx = new ReleaseSet( meta , nr , CATEGORY );
		nx.NAME = NAME;
		nx.ALL = ALL;
		nx.BUILDBRANCH = BUILDBRANCH;
		nx.BUILDTAG = BUILDTAG;
		nx.BUILDVERSION = BUILDVERSION;
		nx.set = set;
		
		for( Entry<String,ReleaseTarget> entry : map.entrySet() ) {
			ReleaseTarget item = entry.getValue().copy( action , nr , nx );
			nx.map.put( entry.getKey() , item );
		}
		
		return( nx );
	}
	
	public void addReleaseSet( ActionBase action , ReleaseSet srcset ) throws Exception {
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

	public boolean isSourceSet( ActionBase action ) throws Exception {
		return( Meta.isSourceCategory( set.CATEGORY ) );
	}
	
	public boolean isCategorySet( ActionBase action ) throws Exception {
		return( !isSourceSet( action ) );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		ALL = ConfReader.getBooleanAttrValue( node , "all" , false );
		if( Meta.isSourceCategory( CATEGORY ) )
			loadBinary( action , node );
		else {
			NAME = Common.getEnumLower( CATEGORY );
			if( CATEGORY == VarCATEGORY.CONFIG )
				loadConfiguration( action , node );
			else
			if( CATEGORY == VarCATEGORY.DB )
				loadDatabase( action , node );
			else
			if( CATEGORY == VarCATEGORY.MANUAL )
				loadManual( action , node );
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
		String SET = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		MetaSource sources = meta.getSources( action ); 
		set = sources.getProjectSet( action , SET );
		NAME = set.NAME;
		
		BUILDBRANCH = ConfReader.getAttrValue( node , "buildbranch" );
		BUILDTAG = ConfReader.getAttrValue( node , "buildtag" );
		BUILDVERSION = ConfReader.getAttrValue( node , "buildversion" );

		Node[] projects = ConfReader.xmlGetChildren( node , "project" );
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
		NAME = Common.getEnumLower( VarCATEGORY.CONFIG );
		ALL = ConfReader.getBooleanAttrValue( node , "all" , false );

		Node[] confitems = ConfReader.xmlGetChildren( node , "confitem" );
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
		NAME = Common.getEnumLower( VarCATEGORY.DB );
		ALL = ConfReader.getBooleanAttrValue( node , "all" , false );

		Node[] dbitems = ConfReader.xmlGetChildren( node , "delivery" );
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
		NAME = Common.getEnumLower( VarCATEGORY.MANUAL );
		ALL = ConfReader.getBooleanAttrValue( node , "all" , false );

		Node[] manualitems = ConfReader.xmlGetChildren( node , "distitem" );
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

	public void createSourceSet( ActionBase action , MetaSourceProjectSet set , boolean ALL ) throws Exception {
		this.set = set;
		this.NAME = set.NAME;
		this.CATEGORY = set.CATEGORY;
		this.ALL = ALL;
		this.BUILDBRANCH = action.context.CTX_BRANCH;
		this.BUILDTAG = action.context.CTX_TAG;
		this.BUILDVERSION = action.context.CTX_VERSION;
		
		if( ALL )
			addAllSourceProjects( action );
	}
	
	public void createCategorySet( ActionBase action , VarCATEGORY CATEGORY , boolean ALL ) throws Exception {
		this.CATEGORY = CATEGORY;
		this.NAME = Common.getEnumLower( CATEGORY );
		this.ALL = ALL;
		this.BUILDBRANCH = action.context.CTX_BRANCH;
		this.BUILDTAG = action.context.CTX_TAG;
		this.BUILDVERSION = action.context.CTX_VERSION;
		
		if( ALL ) {
			if( CATEGORY == VarCATEGORY.CONFIG )
				addAllConfItems( action );
			else
			if( CATEGORY == VarCATEGORY.DB )
				addAllDatabaseItems( action );
			else
			if( CATEGORY == VarCATEGORY.MANUAL )
				addAllManualItems( action );
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
		for( MetaSourceProject project : set.originalList )
			addSourceProject( action , project , true );
	}

	public ReleaseTarget addSourceProject( ActionBase action , MetaSourceProject sourceProject , boolean allItems ) throws Exception {
		action.trace( "add source project=" + sourceProject.PROJECT + " to release ..." );
		ReleaseTarget project = new ReleaseTarget( meta , this , CATEGORY );
		project.createFromProject( action , sourceProject , allItems );
		project.BUILDBRANCH = BUILDBRANCH;
		project.BUILDTAG = BUILDTAG;
		project.BUILDVERSION = BUILDVERSION;
		
		map.put( project.NAME , project );
		return( project );
	}

	public void addAllConfItems( ActionBase action ) throws Exception {
		MetaDistr distr = meta.getDistr( action ); 
		for( MetaDistrConfItem comp : distr.getConfItems() )
			addConfItem( action , comp , true );
	}

	public void addAllDatabaseItems( ActionBase action ) throws Exception {
		MetaDistr distr = meta.getDistr( action ); 
		for( MetaDistrDelivery delivery : distr.getDatabaseDeliveries() )
			addDatabaseItem( action , delivery );
	}

	public void addAllManualItems( ActionBase action ) throws Exception {
		MetaDistr distr = meta.getDistr( action ); 
		for( MetaDistrBinaryItem item : distr.getBinaryItems() ) {
			if( item.distItemOrigin == VarDISTITEMORIGIN.MANUAL )
				addManualItem( action , item );
		}
	}

	public ReleaseTarget addConfItem( ActionBase action , MetaDistrConfItem item , boolean allFiles ) throws Exception {
		ReleaseTarget confItem = new ReleaseTarget( meta , this , CATEGORY );
		confItem.createFromConfItem( action , item , allFiles );
		
		map.put( confItem.NAME , confItem );
		return( confItem );
	}
	
	public ReleaseTarget addDatabaseItem( ActionBase action , MetaDistrDelivery item ) throws Exception {
		ReleaseTarget dbItem = new ReleaseTarget( meta , this , CATEGORY );
		dbItem.createFromDatabaseItem( action , item );
		
		map.put( dbItem.NAME , dbItem );
		return( dbItem );
	}
	
	public ReleaseTarget addManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		ReleaseTarget manualItem = new ReleaseTarget( meta , this , CATEGORY );
		manualItem.createFromManualItem( action , item );
		
		map.put( manualItem.NAME , manualItem );
		return( manualItem );
	}

	public void removeTarget( ActionBase action , ReleaseTarget source ) throws Exception {
		map.remove( source.NAME );
		ALL = false;
	}
	
	public Map<String,ReleaseTarget> getTargets( ActionBase action ) {
		return( map );
	}

	public String getProjectBranch( ActionBase action , String name ) throws Exception {
		ReleaseTarget project = findTarget( action , name );
		if( project == null )
			return( "" );
	
		if( !project.BUILDBRANCH.isEmpty() )
			return( project.BUILDBRANCH );
		
		return( BUILDBRANCH );
	}
	
	public String getProjectTag( ActionBase action , String name ) throws Exception {
		ReleaseTarget project = findTarget( action , name );
		if( project == null )
			return( "" );
	
		if( !project.BUILDTAG.isEmpty() )
			return( project.BUILDTAG );
		
		return( BUILDTAG );
	}

	public ReleaseTarget findTarget( ActionBase action , String key ) throws Exception {
 		return( map.get( key ) );
	}
	
	public ReleaseTarget getTarget( ActionBase action , String key ) throws Exception {
		ReleaseTarget source = findTarget( action , key );
		if( source == null || !source.isCategoryItem( action , CATEGORY ) )
			action.exit1( _Error.UnknownReleaseTarget1 , "unknown release target key=" + key , key );
		return( source );
	}

	public void makePartial( ActionBase action ) throws Exception {
		ALL = false;
	}
	
	public boolean isEmpty( ActionBase action ) throws Exception {
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
		if( Meta.isSourceCategory( CATEGORY ) )
			return( createXmlBinary( action , doc , parent ) );

		return( createXmlCategory( action , doc , parent ) );
	}
	
	public Element createXmlBinary( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , "set" );
		
		Common.xmlSetElementAttr( doc , element , "name" , set.NAME );
		if( !BUILDBRANCH.isEmpty() )
			Common.xmlSetElementAttr( doc , element , "buildbranch" , BUILDBRANCH );
		if( !BUILDTAG.isEmpty() )
			Common.xmlSetElementAttr( doc , element , "buildtag" , BUILDTAG );
		if( !BUILDVERSION.isEmpty() )
			Common.xmlSetElementAttr( doc , element , "buildversion" , BUILDVERSION );
		
		return( addXmlTargetList( action , doc , element ) );
	}
	
	public Element createXmlCategory( ActionBase action , Document doc , Element parent ) throws Exception {
		return( addXmlTargetList( action , doc , parent ) );
	}

	public Element addXmlTargetList( ActionBase action , Document doc , Element element ) throws Exception {
		if( ALL ) {
			Common.xmlSetElementAttr( doc , element , "all" , Common.getBooleanValue( true ) );
			return( element );
		}

		for( String key : Common.getSortedKeys( map ) ) {
			ReleaseTarget target = map.get( key );
			target.createXml( action , doc , element );
		}
		
		return( element );
	}

	public boolean checkAllBinaryIncluded( ActionBase action ) throws Exception {
		for( MetaSourceProject project : set.originalList ) {
			ReleaseTarget target = findTarget( action , project.PROJECT );
			if( target == null )
				return( false );
			
			if( !target.checkSourceAllIncluded( action ) )
				return( false );
		}

		return( true );
	}

}
