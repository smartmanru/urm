package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.engine.custom.CommandCustom;
import org.urm.meta.Types;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaSourceProject {

	public Meta meta;
	public MetaSourceProjectSet set;
	
	public int POS = 0;
	public String NAME = "";
	public String DESC = "";
	public VarPROJECTTYPE type;
	public String RESOURCE = "";
	public String REPOSITORY = "";
	public boolean codebaseProd = false;
	public String BUILDGROUP = "";
	public String REPOPATH = "";
	public String CODEPATH = "";
	public String TRACKER = "";
	public String BRANCH = "";
	public String BUILDER = "";

	List<MetaSourceProjectItem> itemList = new LinkedList<MetaSourceProjectItem>();
	Map<String,MetaSourceProjectItem> itemMap = new HashMap<String,MetaSourceProjectItem>();

	public boolean CUSTOMBUILD;
	public boolean CUSTOMGET;
	
	public MetaSourceProject( Meta meta , MetaSourceProjectSet set ) {
		this.meta = meta;
		this.set = set;
	}
	
	public void createProject( ServerTransaction transaction , String name , int POS ) throws Exception {
		this.NAME = name;
		this.POS = POS;
	}
	
	public void addItem( ServerTransaction transaction , MetaSourceProjectItem item ) throws Exception {
		addItem( item );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		POS = ConfReader.getIntegerAttrValue( node , "order" , 0 );
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
		DESC = ConfReader.getAttrValue( node , "desc" );

		// read item attrs
		REPOSITORY = ConfReader.getAttrValue( node , "repository" );
		if( REPOSITORY.isEmpty() )
			REPOSITORY = NAME;

		BUILDGROUP = ConfReader.getAttrValue( node , "group" );
		RESOURCE = ConfReader.getRequiredAttrValue( node , "resource" );
		if( !RESOURCE.isEmpty() ) {
			REPOPATH = ConfReader.getAttrValue( node , "repopath" );
			CODEPATH = ConfReader.getAttrValue( node , "codepath" );
		}
		
		type = Types.getProjectType( ConfReader.getAttrValue( node , "type" ) , true );
		if( type == VarPROJECTTYPE.BUILDABLE ) {
			codebaseProd = ConfReader.getBooleanAttrValue( node , "prod" , false );
			TRACKER = ConfReader.getAttrValue( node , "jira" );
			BRANCH = ConfReader.getAttrValue( node , "branch" );
			BUILDER = ConfReader.getAttrValue( node , "builder" );
			
			if( BRANCH.isEmpty() )
				BRANCH = NAME + "-prod";
		}
		
		// read project items
		Node[] items = ConfReader.xmlGetChildren( node , "distitem" );
		if( items != null ) {
			for( Node item : items ) {
				MetaSourceProjectItem srcItem = new MetaSourceProjectItem( meta , this );
				srcItem.load( action , item );
				addItem( srcItem );
			}
		}
		
		// resolve references
		CUSTOMBUILD = ConfReader.getBooleanAttrValue( node , "custombuild" , false );
		CUSTOMGET = ConfReader.getBooleanAttrValue( node , "customget" , false );
		
		if( CUSTOMBUILD || CUSTOMGET ) {
			CommandCustom custom = new CommandCustom( meta );
			custom.parseProject( action , this , node );
		}
	}

	private void addItem( MetaSourceProjectItem srcItem ) {
		itemList.add( srcItem );
		itemMap.put( srcItem.ITEMNAME , srcItem );
	}
	
	private void removeItem( MetaSourceProjectItem srcItem ) {
		itemList.remove( srcItem );
		itemMap.remove( srcItem.ITEMNAME );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "order" , "" + POS );
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );

		// read item attrs
		Common.xmlSetElementAttr( doc , root , "repository" , REPOSITORY );
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( type ) );
		Common.xmlSetElementAttr( doc , root , "group" , BUILDGROUP );
		if( !RESOURCE.isEmpty() ) {
			Common.xmlSetElementAttr( doc , root , "resource" , RESOURCE );
			Common.xmlSetElementAttr( doc , root , "repopath" , REPOPATH );
			Common.xmlSetElementAttr( doc , root , "codepath" , CODEPATH );
		}
		
		if( type == VarPROJECTTYPE.BUILDABLE ) {
			Common.xmlSetElementAttr( doc , root , "prod" , Common.getBooleanValue( codebaseProd ) );
			Common.xmlSetElementAttr( doc , root , "jira" , TRACKER );
			Common.xmlSetElementAttr( doc , root , "branch" , BRANCH );
			Common.xmlSetElementAttr( doc , root , "builder" , BUILDER );
		}
		
		// project items
		for( MetaSourceProjectItem item : itemList ) {
			Element itemElement = Common.xmlCreateElement( doc , root , "distitem" );
			item.save( action , doc , itemElement );
		}
		
		Common.xmlSetElementAttr( doc , root , "custombuild" , Common.getBooleanValue( CUSTOMBUILD ) );
		Common.xmlSetElementAttr( doc , root , "customget" , Common.getBooleanValue( CUSTOMGET ) );
	}
	
	public MetaSourceProject copy( ActionBase action , Meta meta , MetaSourceProjectSet set ) throws Exception {
		MetaSourceProject r = new MetaSourceProject( meta , set );
		r.POS = POS;
		r.NAME = NAME;
		r.DESC = DESC;
		r.type = type;

		// read item attrs
		r.REPOSITORY = REPOSITORY;
		r.codebaseProd = codebaseProd;
		r.BUILDGROUP = BUILDGROUP;
		r.TRACKER = TRACKER;
		r.BRANCH = BRANCH;
		r.BUILDER = BUILDER;
		r.RESOURCE = RESOURCE;
		r.REPOPATH = REPOPATH;
		r.CODEPATH = CODEPATH;
		
		// project items
		for( MetaSourceProjectItem item : itemList ) {
			MetaSourceProjectItem ritem = item.copy( action , meta , r );
			addItem( ritem );
		}
		
		r.CUSTOMBUILD = CUSTOMBUILD;
		r.CUSTOMGET = CUSTOMGET;
		return( r );
	}

	public boolean isPrebuiltNexus() {
		if( type == VarPROJECTTYPE.PREBUILT_NEXUS )
			return( true );
		return( false );
	}
	
	public boolean isPrebuiltVCS() {
		if( type == VarPROJECTTYPE.PREBUILT_VCS )
			return( true );
		return( false );
	}
	
	public String getVCS( ActionBase action ) {
		return( RESOURCE );
	}
	
	public boolean isGitVCS( ActionBase action ) throws Exception {
		ServerAuthResource res = action.getResource( RESOURCE );
		return( res.isGit() );
	}
	
	public boolean isSvnVCS( ActionBase action ) throws Exception {
		ServerAuthResource res = action.getResource( RESOURCE );
		return( res.isSvn() );
	}

	public boolean isBuildable() {
		if( type == VarPROJECTTYPE.BUILDABLE )
			return( true );
		return( false );
	}

	public MetaSourceProjectItem findItem( String name ) {
		return( itemMap.get( name ) );
	}
	
	public MetaSourceProjectItem getItem( ActionBase action , String name ) throws Exception {
		MetaSourceProjectItem item = itemMap.get( name );
		if( item == null )
			action.exit2( _Error.UnknownSourceProjectItem2 , "unknown source project item=" + name + ", in project=" + NAME , NAME , name );
		
		return( item );
	}
	
	public boolean isEmpty( ActionBase action ) throws Exception {
		return( itemList.isEmpty() );
	}
	
	public MetaSourceProjectItem[] getItems() {
		return( itemList.toArray( new MetaSourceProjectItem[0] ) );
	}
	
	public String getBuilder( ActionBase action ) throws Exception {
		if( !BUILDER.isEmpty() )
			return( BUILDER );
		
		MetaProductBuildSettings build = action.getBuildSettings( meta );
		String builder = build.CONFIG_BUILDER;
		if( builder.isEmpty() )
			builder = "maven";

		return( builder );
	}

	public String getDefaultBranch( ActionBase action ) throws Exception {
		if( !BRANCH.isEmpty() )
			return( BRANCH );
		
		MetaProductBuildSettings build = action.getBuildSettings( meta );
		if( !build.CONFIG_BRANCHNAME.isEmpty() )
			return( build.CONFIG_BRANCHNAME );
		
		String branch = NAME + "-prod";
		return( branch );
	}
	
	public String[] getItemNames() {
		return( Common.getSortedKeys( itemMap ) );
	}

	public void setProjectData( ServerTransaction transaction , String desc , String group , VarPROJECTTYPE type , String resource , String repoName , String repoPath , String codePath , String branch ) throws Exception {
		this.DESC = desc;
		this.BUILDGROUP = group;
		this.type = type;
		
		this.codebaseProd = false;
		this.BRANCH = "";
		this.BUILDER = "";
		
		this.RESOURCE = resource;
		this.REPOSITORY = repoName;
		this.REPOPATH = repoPath;
		this.CODEPATH = codePath;
		this.BRANCH = branch;
	}

	public void setCodebase( ServerTransaction transaction , boolean prod , String branch , String builder ) throws Exception {
		this.codebaseProd = prod;
		this.BRANCH = branch;
		this.BUILDER = builder;
	}

	public void setOrder( ServerTransaction transaction , int POS ) throws Exception {
		this.POS = POS;
	}

	public void changeProjectSet( ServerTransaction transaction , MetaSourceProjectSet setNew ) throws Exception {
		this.set = setNew;
	}

	public void removeItem( ServerTransaction transaction , MetaSourceProjectItem item ) throws Exception {
		removeItem( item );
	}
	
}
