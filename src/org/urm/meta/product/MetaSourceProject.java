package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.DBEnumProjectType;
import org.urm.engine.EngineTransaction;
import org.urm.engine.custom.CommandCustom;
import org.urm.meta.MatchItem;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.urm.meta.engine.AuthResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaSourceProject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_PROJECTPOS = "order";
	public static String PROPERTY_PROJECTTYPE = "type";
	public static String PROPERTY_PROD = "prod";
	public static String PROPERTY_TRACKER = "tracker";
	public static String PROPERTY_BRANCH = "branch";
	public static String PROPERTY_BUILDER_OPTIONS = "builder_options";
	public static String PROPERTY_MIRRORRES = "resource";
	public static String PROPERTY_MIRRORREPO = "repository";
	public static String PROPERTY_MIRRORPATH = "repopath";
	public static String PROPERTY_MIRRORDATA = "repodata";
	public static String PROPERTY_CUSTOM_BUILD = "custom_build";
	public static String PROPERTY_CUSTOM_GET = "custom_get";
	
	public Meta meta;
	public MetaSourceProjectSet set;
	
	public int ID;
	public String NAME;
	public String DESC;
	public int PROJECT_POS;
	public DBEnumProjectType PROJECT_TYPE;
	public boolean CODEBASE_PROD;
	public Integer UNIT_ID;
	public String TRACKER;
	public String BRANCH;
	public MatchItem BUILDER;
	public MatchItem MIRROR;
	public String MIRROR_RESOURCE;
	public String MIRROR_REPOSITORY;
	public String MIRROR_REPOPATH;
	public String MIRROR_CODEPATH;
	public String BUILDER_ADDOPTIONS;
	public boolean CUSTOMBUILD;
	public boolean CUSTOMGET;
	public int PV;
	
	List<MetaSourceProjectItem> itemList;
	Map<String,MetaSourceProjectItem> itemMap;

	public MetaSourceProject( Meta meta , MetaSourceProjectSet set ) {
		this.meta = meta;
		this.set = set;
		ID = -1;
		PV = -1;
		
		itemList = new LinkedList<MetaSourceProjectItem>();
		itemMap = new HashMap<String,MetaSourceProjectItem>();
	}
	
	public void setMirror( int mirrorId ) {
		this.mirrorId = mirrorId;
	}
	
	public void createProject( EngineTransaction transaction , String name , int POS ) throws Exception {
		this.NAME = name;
		this.PROJECT_POS = POS;
	}
	
	public void addItem( EngineTransaction transaction , MetaSourceProjectItem item ) throws Exception {
		addItem( item );
	}
	
	private void addItem( MetaSourceProjectItem srcItem ) {
		itemList.add( srcItem );
		itemMap.put( srcItem.NAME , srcItem );
	}
	
	private void removeItem( MetaSourceProjectItem srcItem ) {
		itemList.remove( srcItem );
		itemMap.remove( srcItem.NAME );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "order" , "" + PROJECT_POS );
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		Common.xmlSetElementAttr( doc , root , "prod" , Common.getBooleanValue( CODEBASE_PROD ) );

		// read item attrs
		Common.xmlSetElementAttr( doc , root , "repository" , REPOSITORY );
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( PROJECT_TYPE ) );
		Common.xmlSetElementAttr( doc , root , "unit" , UNIT );
		if( RESOURCE_ID != null ) {
			AuthResource rc = action.getResource( RESOURCE_ID );
			Common.xmlSetElementAttr( doc , root , "resource" , rc.NAME );
			Common.xmlSetElementAttr( doc , root , "repopath" , REPOPATH );
			Common.xmlSetElementAttr( doc , root , "codepath" , CODEPATH );
		}
		
		if( PROJECT_TYPE == VarPROJECTTYPE.BUILDABLE ) {
			Common.xmlSetElementAttr( doc , root , "jira" , TRACKER );
			Common.xmlSetElementAttr( doc , root , "branch" , BRANCH );
			Common.xmlSetElementAttr( doc , root , "builder" , BUILDER );
			Common.xmlSetElementAttr( doc , root , "builder.addoptions" , BUILDER_ADDOPTIONS );
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
		r.PROJECT_POS = PROJECT_POS;
		r.NAME = NAME;
		r.DESC = DESC;
		r.PROJECT_TYPE = PROJECT_TYPE;
		r.CODEBASE_PROD = CODEBASE_PROD;

		// read item attrs
		r.REPOSITORY = REPOSITORY;
		r.UNIT = UNIT;
		r.TRACKER = TRACKER;
		r.BRANCH = BRANCH;
		r.BUILDER = BUILDER;
		r.BUILDER_ADDOPTIONS = BUILDER_ADDOPTIONS;
		r.RESOURCE_ID = RESOURCE_ID;
		r.REPOPATH = REPOPATH;
		r.CODEPATH = CODEPATH;
		
		// project items
		for( MetaSourceProjectItem item : itemList ) {
			MetaSourceProjectItem ritem = item.copy( action , meta , r );
			r.addItem( ritem );
		}
		
		r.CUSTOMBUILD = CUSTOMBUILD;
		r.CUSTOMGET = CUSTOMGET;
		
		r.mirrorId = mirrorId;
		
		return( r );
	}

	public boolean isPrebuiltNexus() {
		if( PROJECT_TYPE == VarPROJECTTYPE.PREBUILT_NEXUS )
			return( true );
		return( false );
	}
	
	public boolean isPrebuiltVCS() {
		if( PROJECT_TYPE == VarPROJECTTYPE.PREBUILT_VCS )
			return( true );
		return( false );
	}
	
	public Integer getVCS( ActionBase action ) {
		return( RESOURCE_ID );
	}
	
	public boolean isGitVCS( ActionBase action ) throws Exception {
		AuthResource res = action.getResource( RESOURCE_ID );
		return( res.isGit() );
	}
	
	public boolean isSvnVCS( ActionBase action ) throws Exception {
		AuthResource res = action.getResource( RESOURCE_ID );
		return( res.isSvn() );
	}

	public boolean isBuildable() {
		if( PROJECT_TYPE == VarPROJECTTYPE.BUILDABLE )
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
		return( BUILDER );
	}

	public String getDefaultBranch( ActionBase action ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings( meta );
		if( !build.CONFIG_BRANCHNAME.isEmpty() )
			return( build.CONFIG_BRANCHNAME );
		
		if( !BRANCH.isEmpty() )
			return( BRANCH );
		
		String branch = NAME + "-prod";
		return( branch );
	}
	
	public String[] getItemNames() {
		return( Common.getSortedKeys( itemMap ) );
	}

	public void setProjectData( EngineTransaction transaction , String desc , boolean prod , String unit , VarPROJECTTYPE type , Integer resourceId , String repoName , String repoPath , String codePath , String branch ) throws Exception {
		this.DESC = desc;
		this.UNIT = unit;
		this.PROJECT_TYPE = type;
		this.CODEBASE_PROD = prod;
		
		this.BUILDER = "";
		this.BUILDER_ADDOPTIONS = "";
		
		this.RESOURCE_ID = resourceId;
		this.REPOSITORY = repoName;
		this.REPOPATH = repoPath;
		this.CODEPATH = codePath;
		this.BRANCH = branch;
	}

	public void setCodebase( EngineTransaction transaction , String branch , String builder , String builderAddOptions ) throws Exception {
		this.BRANCH = branch;
		this.BUILDER = builder;
		this.BUILDER_ADDOPTIONS = builderAddOptions;
	}

	public void setOrder( EngineTransaction transaction , int POS ) throws Exception {
		this.PROJECT_POS = POS;
	}

	public void changeProjectSet( EngineTransaction transaction , MetaSourceProjectSet setNew ) throws Exception {
		this.set = setNew;
	}

	public void removeItem( EngineTransaction transaction , MetaSourceProjectItem item ) throws Exception {
		removeItem( item );
	}

	public boolean hasDistItems() {
		for( MetaSourceProjectItem item : itemList ) {
			if( !item.isInternal() )
				return( true );
		}
		return( false );
	}
	
	public void clearUnit( EngineTransaction transaction ) throws Exception {
		UNIT = "";
	}

}
