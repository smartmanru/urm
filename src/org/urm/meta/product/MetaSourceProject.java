package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.data.EngineMirrors;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.loader.MatchItem;

public class MetaSourceProject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_PROJECTPOS = "order";
	public static String PROPERTY_PROJECTTYPE = "type";
	public static String PROPERTY_PROD = "prod";
	public static String PROPERTY_TRACKER = "tracker";
	public static String PROPERTY_BRANCH = "branch";
	public static String PROPERTY_UNIT = "unit";
	public static String PROPERTY_BUILDER_NAME = "builder";
	public static String PROPERTY_BUILDER_OPTIONS = "builder.addoptions";
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
	public MatchItem MIRROR;
	public String MIRROR_RESOURCE;
	public String MIRROR_REPOSITORY;
	public String MIRROR_REPOPATH;
	public String MIRROR_CODEPATH;
	public MatchItem BUILDER;
	public String BRANCH;
	public String BUILDER_ADDOPTIONS;
	public boolean CUSTOMBUILD;
	public boolean CUSTOMGET;
	public int PV;
	public DBEnumChangeType CHANGETYPE;
	
	List<MetaSourceProjectItem> itemList;
	Map<String,MetaSourceProjectItem> itemMap;
	Map<Integer,MetaSourceProjectItem> itemMapById;

	public MetaSourceProject( Meta meta , MetaSourceProjectSet set ) {
		this.meta = meta;
		this.set = set;
		ID = -1;
		PV = -1;
		
		itemList = new LinkedList<MetaSourceProjectItem>();
		itemMap = new HashMap<String,MetaSourceProjectItem>();
		itemMapById = new HashMap<Integer,MetaSourceProjectItem>();
	}
	
	public MetaSourceProject copy( Meta rmeta , MetaSourceProjectSet rset , boolean all ) throws Exception {
		MetaSourceProject r = new MetaSourceProject( rmeta , rset );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.PROJECT_POS = PROJECT_POS;
		r.PROJECT_TYPE = PROJECT_TYPE;
		r.CODEBASE_PROD = CODEBASE_PROD;
		r.UNIT_ID = UNIT_ID;
		r.TRACKER = TRACKER;
		r.MIRROR = MatchItem.copy( MIRROR );
		r.MIRROR_RESOURCE = MIRROR_RESOURCE;
		r.MIRROR_REPOSITORY = MIRROR_REPOSITORY;
		r.MIRROR_REPOPATH = MIRROR_REPOPATH;
		r.MIRROR_CODEPATH = MIRROR_CODEPATH;
		r.BUILDER = MatchItem.copy( BUILDER );
		r.BRANCH = BRANCH;
		r.BUILDER_ADDOPTIONS = BUILDER_ADDOPTIONS;
		r.CUSTOMBUILD = CUSTOMBUILD;
		r.CUSTOMGET = CUSTOMGET;
		r.PV = PV;
		r.CHANGETYPE = CHANGETYPE;
		
		// project items
		if( all ) {
			for( MetaSourceProjectItem item : itemList ) {
				MetaSourceProjectItem ritem = item.copy( rmeta , r );
				r.addItem( ritem );
			}
		}
		
		return( r );
	}

	public void setMirror( MirrorRepository mirror ) throws Exception {
		MIRROR.match( mirror.ID );
		MIRROR_RESOURCE = "";
		MIRROR_REPOSITORY = "";
		MIRROR_REPOPATH = "";
		MIRROR_CODEPATH = "";
	}
	
	public void createProject( String name , String desc , int pos , Integer unit , boolean prod ) throws Exception {
		modifyProject( name , desc , pos , unit , prod );
	}
	
	public void modifyProject( String name , String desc , int pos , Integer unit , boolean prod ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.PROJECT_POS = pos;
		this.UNIT_ID = unit;
		this.CODEBASE_PROD = prod;
	}
	
	public void setSource( DBEnumProjectType type , String tracker , MatchItem mirror , String repoRes , String repoName , String repoPath , String codePath ) throws Exception {
		this.PROJECT_TYPE = type;
		this.TRACKER = tracker;
		this.MIRROR = mirror;
		this.MIRROR_RESOURCE = repoRes;
		this.MIRROR_REPOSITORY = repoName;
		this.MIRROR_REPOPATH = repoPath;
		this.MIRROR_CODEPATH = codePath;
	}
	
	public void setBuild( MatchItem builder , String addOptions , String branch ) throws Exception {
		this.BUILDER = builder;
		this.BUILDER_ADDOPTIONS = addOptions;
		this.BRANCH = branch;
	}

	public void setCustom( boolean customBuild , boolean customGet ) throws Exception {
		this.CUSTOMBUILD = customBuild;
		this.CUSTOMGET = customGet;
	}

	public void changeOrder( int POS ) throws Exception {
		this.PROJECT_POS = POS;
	}

	public void clearUnit() {
		UNIT_ID = null;
	}
	
	public void changeProjectSet( MetaSourceProjectSet setNew , int posNew ) throws Exception {
		this.set = setNew;
		this.PROJECT_POS = posNew;
	}

	public void addItem( MetaSourceProjectItem srcItem ) {
		itemList.add( srcItem );
		itemMap.put( srcItem.NAME , srcItem );
		itemMapById.put( srcItem.ID , srcItem );
	}
	
	public void removeItem( MetaSourceProjectItem srcItem ) {
		itemList.remove( srcItem );
		itemMap.remove( srcItem.NAME );
		itemMapById.remove( srcItem.ID );
	}
	
	public void updateItem( MetaSourceProjectItem item ) throws Exception {
		Common.changeMapKey( itemMap , item , item.NAME );
	}
	
	public boolean isPrebuiltNexus() {
		if( PROJECT_TYPE == DBEnumProjectType.PREBUILT_NEXUS )
			return( true );
		return( false );
	}
	
	public boolean isPrebuiltVCS() {
		if( PROJECT_TYPE == DBEnumProjectType.PREBUILT_VCS )
			return( true );
		return( false );
	}
	
	public Integer getMirrorId() {
		return( MIRROR.FKID );
	}
	
	public MirrorRepository getMirror( ActionBase action ) throws Exception {
		EngineMirrors mirrors = action.getEngineMirrors();
		MirrorRepository mirror = mirrors.getRepository( MIRROR.FKID );
		return( mirror );
	}
	
	public AuthResource getResource( ActionBase action ) throws Exception {
		MirrorRepository mirror = getMirror( action );
		return( mirror.getResource( action ) );
	}
	
	public boolean isGitVCS( ActionBase action ) throws Exception {
		AuthResource res = getResource( action );
		return( res.isGit() );
	}
	
	public boolean isSvnVCS( ActionBase action ) throws Exception {
		AuthResource res = getResource( action );
		return( res.isSvn() );
	}

	public boolean isBuildable() {
		if( PROJECT_TYPE == DBEnumProjectType.BUILDABLE )
			return( true );
		return( false );
	}

	public MetaSourceProjectItem findItem( String name ) {
		return( itemMap.get( name ) );
	}
	
	public MetaSourceProjectItem getItem( String name ) throws Exception {
		MetaSourceProjectItem item = itemMap.get( name );
		if( item == null )
			Common.exit2( _Error.UnknownSourceProjectItem2 , "unknown source project item=" + name + ", in project=" + NAME , NAME , name );
		
		return( item );
	}
	
	public MetaSourceProjectItem getItem( int id ) throws Exception {
		MetaSourceProjectItem item = itemMapById.get( id );
		if( item == null )
			Common.exit2( _Error.UnknownSourceProjectItem2 , "unknown source project item=" + id + ", in project=" + NAME , NAME , "" + id );
		
		return( item );
	}
	
	public boolean isEmpty( ActionBase action ) throws Exception {
		return( itemList.isEmpty() );
	}
	
	public MetaSourceProjectItem[] getItems() {
		return( itemList.toArray( new MetaSourceProjectItem[0] ) );
	}
	
	public ProjectBuilder getBuilder( ActionBase action ) throws Exception {
		ProjectBuilder builder = action.getBuilder( BUILDER );
		return( builder );
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

	public boolean hasDistItems() {
		for( MetaSourceProjectItem item : itemList ) {
			if( !item.isInternal() )
				return( true );
		}
		return( false );
	}
	
}
