package org.urm.engine.dist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaDistrBinaryItem;
import org.urm.engine.meta.MetaDistrConfItem;
import org.urm.engine.meta.MetaDistrDelivery;
import org.urm.engine.meta.MetaSourceProject;
import org.urm.engine.meta.MetaSourceProjectItem;
import org.urm.engine.meta.Meta.VarCATEGORY;
import org.urm.engine.meta.Meta.VarDISTITEMSOURCE;
import org.urm.engine.meta.Meta.VarNAMETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseTarget {

	Meta meta;
	public ReleaseSet set;
	public VarCATEGORY CATEGORY;

	public boolean ALL;
	public String NAME = "";
	
	public String BUILDBRANCH = ""; 
	public String BUILDTAG = ""; 
	public String BUILDVERSION = "";
	
	public MetaSourceProject sourceProject;
	public MetaDistrConfItem distConfItem;
	public MetaDistrDelivery distDatabaseItem;
	public MetaDistrBinaryItem distManualItem;
	
	Map<String,ReleaseTargetItem> itemMap = new HashMap<String,ReleaseTargetItem>();
	
	public String DISTFILE;

	public ReleaseTarget( Meta meta , ReleaseSet set , VarCATEGORY CATEGORY ) {
		this.meta = meta;
		this.set = set;
		this.CATEGORY = CATEGORY;
	}

	public ReleaseTarget copy( ActionBase action , Release nr , ReleaseSet ns ) throws Exception {
		ReleaseTarget nx = new ReleaseTarget( meta , ns , CATEGORY );
		
		nx.ALL = ALL;
		nx.NAME = NAME;
		
		nx.BUILDBRANCH = BUILDBRANCH; 
		nx.BUILDTAG = BUILDTAG; 
		nx.BUILDVERSION = BUILDVERSION;
		
		nx.sourceProject = sourceProject;
		nx.distConfItem = distConfItem;
		nx.distDatabaseItem = distDatabaseItem;
		nx.distManualItem = distManualItem;

		for( Entry<String,ReleaseTargetItem> entry : itemMap.entrySet() ) {
			ReleaseTargetItem item = entry.getValue().copy( action , nr , ns , this );
			nx.itemMap.put( entry.getKey() , item );
		}
		
		return( nx );
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
		if( meta.isSourceCategory( CATEGORY ) ) {
			if( sourceProject != null )
				return( true );
		}
		else if( CATEGORY == VarCATEGORY.CONFIG ) {
			if( distConfItem != null )
				return( true );
		}
		else if( CATEGORY == VarCATEGORY.DB ) {
			if( distDatabaseItem != null )
				return( true );
		}
		else if( CATEGORY == VarCATEGORY.MANUAL ) {
			if( distManualItem != null )
				return( true );
		}
		
		action.exitUnexpectedState();
		return( false );
	}
	
	public boolean isProjectItem( ActionBase action ) throws Exception {
		if( sourceProject != null )
			return( true );
		return( false );
	}
	
	public boolean isConfItem( ActionBase action ) throws Exception {
		if( distConfItem != null )
			return( true );
		return( false );
	}
	
	public boolean isDatabaseItem( ActionBase action ) throws Exception {
		if( distDatabaseItem != null )
			return( true );
		return( false );
	}

	public void load( ActionBase action , Node node ) throws Exception {
		if( meta.isSourceCategory( CATEGORY ) )
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
			action.exitUnexpectedCategory( CATEGORY );
	}
	
	private void loadProject( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
		BUILDBRANCH = ConfReader.getAttrValue( node , "buildbranch" , BUILDBRANCH );
		BUILDTAG = ConfReader.getAttrValue( node , "buildtag" , BUILDTAG );
		BUILDVERSION = ConfReader.getAttrValue( node , "buildversion" , BUILDVERSION );
		
		// find in sources
		sourceProject = meta.sources.getProject( action , name ); 
		NAME = sourceProject.PROJECT;
		
		Node[] items = ConfReader.xmlGetChildren( node , "distitem" );
		if( items == null ) {
			addAllSourceItems( action , sourceProject );
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
		distConfItem = meta.distr.getConfItem( action , name );
		this.NAME = name;
		
		ALL = ( ConfReader.getBooleanAttrValue( node , "partial" , true ) )? false : true;
	}
	
	private void loadDatabase( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		distDatabaseItem = meta.distr.getDelivery( action , name );
		this.NAME = name;
		
		ALL = true;
	}

	private void loadManual( ActionBase action , Node node ) throws Exception {
		String name = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOT );
		distManualItem = meta.distr.getBinaryItem( action , name );
		this.NAME = name;
		
		ALL = true;
	}

	public void createFromProject( ActionBase action , MetaSourceProject sourceProject , boolean allItems ) throws Exception {
		this.sourceProject = sourceProject;
		this.CATEGORY = sourceProject.CATEGORY;
		
		NAME = sourceProject.PROJECT;
		ALL = false;
		BUILDBRANCH = "";
		BUILDTAG = "";
		BUILDVERSION = "";
		
		// find in sources
		if( allItems )
			addAllSourceItems( action , sourceProject );
	}

	public void createFromConfItem( ActionBase action , MetaDistrConfItem item , boolean allFiles ) throws Exception {
		this.distConfItem = item;
		this.CATEGORY = VarCATEGORY.CONFIG;
		this.ALL = allFiles;
		this.NAME = item.KEY;
	}
	
	public void createFromDatabaseItem( ActionBase action , MetaDistrDelivery item ) throws Exception {
		this.distDatabaseItem = item;
		this.CATEGORY = VarCATEGORY.DB;
		this.ALL = true;
		this.NAME = item.NAME;
	}
	
	public void createFromManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.DISTSOURCE != VarDISTITEMSOURCE.MANUAL )
			action.exit1( _Error.UnexpectedNonManualItem1 , "unexpected non-manual item=" + item.KEY , item.KEY );
		
		this.distManualItem = item;
		this.CATEGORY = VarCATEGORY.MANUAL;
		this.ALL = true;
		this.NAME = item.KEY;
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
	
	public ReleaseTargetItem addSourceItem( ActionBase action , MetaSourceProjectItem projectitem ) throws Exception {
		// ignore internal items
		if( projectitem.INTERNAL )
			return( null );
		
		ReleaseTargetItem item = new ReleaseTargetItem( meta , this );
		item.createFromSourceItem( action , projectitem );
		itemMap.put( item.NAME , item );
		return( item );
	}
	
	public ReleaseTargetItem getItem( ActionBase action , String ITEMNAME ) throws Exception {
		return( itemMap.get( ITEMNAME ) );
	}
	
	public void addAllSourceItems( ActionBase action , MetaSourceProject sourceProject ) throws Exception {
		ALL = true;
		if( sourceProject.distItem != null ) {
			ReleaseTargetItem item = new ReleaseTargetItem( meta , this );
			item.createFromDistrItem( action , sourceProject.distItem );
			itemMap.put( item.NAME , item );
			return;
		}
		
		// read source items
		List<MetaSourceProjectItem> projectitems = sourceProject.getIitemList( action );
		for( MetaSourceProjectItem projectitem : projectitems )
			addSourceItem( action , projectitem );
	}

	public Map<String,ReleaseTargetItem> getItems( ActionBase action ) throws Exception {
		return( itemMap );
	}
	
	public MetaDistrDelivery getDelivery( ActionBase action ) throws Exception {
		if( distConfItem != null )
			return( distConfItem.delivery );
		if( distDatabaseItem != null )
			return( distDatabaseItem );
		if( distManualItem != null )
			return( distManualItem.delivery );
		
		action.exitUnexpectedCategory( CATEGORY );
		return( null );
	}
	
	public String getSpecifics( ActionBase action ) throws Exception {
		if( sourceProject != null )
			return( getSpecificsProject( action ) );
		if( distConfItem != null )
			return( getSpecificsConfig( action ) );
		if( distDatabaseItem != null )
			return( getSpecificsDatabase( action ) );
		if( distManualItem != null )
			return( getSpecificsManual( action ) );
		return( "" );
	}
	
	public String getSpecificsProject( ActionBase action ) throws Exception {
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

	public String getSpecificsConfig( ActionBase action ) throws Exception {
		if( !ALL )
			return( "partial" );
		return( "" );
	}
	
	public String getSpecificsDatabase( ActionBase action ) throws Exception {
		if( !ALL )
			return( "partial" );
		return( "" );
	}
	
	public String getSpecificsManual( ActionBase action ) throws Exception {
		return( "" );
	}
	
	public boolean isEmpty( ActionBase action ) throws Exception {
		return( itemMap.isEmpty() );
	}

	public Element createXml( ActionBase action , Document doc , Element parent ) throws Exception {
		if( sourceProject != null )
			return( createXmlBinary( action , doc , parent ) );
		if( distConfItem != null )
			return( createXmlConfig( action , doc , parent ) );
		if( distDatabaseItem != null )
			return( createXmlDatabase( action , doc , parent ) );
		if( distManualItem != null )
			return( createXmlManual( action , doc , parent ) );
		return( null );
	}
	
	public Element createXmlBinary( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , "project" );
		
		Common.xmlSetElementAttr( doc , element , "name" , sourceProject.PROJECT );
		if( !BUILDBRANCH.isEmpty() )
			Common.xmlSetElementAttr( doc , element , "buildbranch" , BUILDBRANCH );
		if( !BUILDTAG.isEmpty() )
			Common.xmlSetElementAttr( doc , element , "buildtag" , BUILDTAG );
		if( !BUILDVERSION.isEmpty() )
			Common.xmlSetElementAttr( doc , element , "buildversion" , BUILDVERSION );

		// single-item projects
		if( sourceProject.distItem != null )
			return( element );
		
		// all project items
		if( ALL ) {
			Common.xmlSetElementAttr( doc , element , "all" , Common.getBooleanValue( true ) );
			return( element );
		}
		
		// selected items
		for( ReleaseTargetItem item : itemMap.values() )
			item.createXml( action , doc , element );
		
		return( element );
	}
	
	public Element createXmlConfig( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , "confitem" );
		Common.xmlSetElementAttr( doc , element , "name" , distConfItem.KEY );
		String partial = Common.getBooleanValue( !ALL ); 
		Common.xmlSetElementAttr( doc , element , "partial" , partial );
		return( element );
	}

	public Element createXmlDatabase( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , "delivery" );
		Common.xmlSetElementAttr( doc , element , "name" , distDatabaseItem.NAME );
		return( element );
	}

	public Element createXmlManual( ActionBase action , Document doc , Element parent ) throws Exception {
		Element element = Common.xmlCreateElement( doc , parent , "distitem" );
		Common.xmlSetElementAttr( doc , element , "name" , distManualItem.KEY );
		return( element );
	}

	public boolean checkSourceAllIncluded( ActionBase action ) throws Exception {
		for( MetaSourceProjectItem projectitem : sourceProject.getIitemList( action ) ) {
			ReleaseTargetItem source = itemMap.get( projectitem.ITEMNAME );
			if( source == null )
				return( false );
		}
		
		return( true );
	}
	
	public void removeSourceItem( ActionBase action , ReleaseTargetItem buildItem ) throws Exception {
		itemMap.remove( buildItem.NAME );
		ALL = false;
	}
	
}
