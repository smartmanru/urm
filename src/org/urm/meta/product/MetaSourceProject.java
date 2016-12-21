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
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaSourceProject {

	public Meta meta;
	public MetaSourceProjectSet set;
	
	public String NAME = "";
	public String DESC = "";
	public boolean codebaseProject = false;
	public String RESOURCE = "";
	public String REPOSITORY = "";
	public boolean codebaseProd = false;
	public String GROUP = "";
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
	
	public void create( ServerTransaction transaction , String name ) throws Exception {
		NAME = name;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		NAME = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );
		DESC = ConfReader.getAttrValue( node , "desc" );

		// read item attrs
		REPOSITORY = ConfReader.getAttrValue( node , "repository" );
		if( REPOSITORY.isEmpty() )
			REPOSITORY = NAME;

		GROUP = ConfReader.getAttrValue( node , "group" );
		RESOURCE = ConfReader.getRequiredAttrValue( node , "resource" );
		if( !RESOURCE.isEmpty() ) {
			REPOPATH = ConfReader.getAttrValue( node , "repopath" );
			CODEPATH = ConfReader.getAttrValue( node , "codepath" );
		}
		
		codebaseProject = ConfReader.getBooleanAttrValue( node , "codebase" , true );
		if( codebaseProject ) {
			codebaseProd = ConfReader.getBooleanAttrValue( node , "version" , false );
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
				MetaSourceProjectItem distItem = new MetaSourceProjectItem( meta , this );
				distItem.load( action , item );
				addItem( distItem );
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

	private void addItem( MetaSourceProjectItem distItem ) {
		itemList.add( distItem );
		itemMap.put( distItem.ITEMNAME , distItem );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );

		// read item attrs
		Common.xmlSetElementAttr( doc , root , "repository" , REPOSITORY );
		Common.xmlSetElementAttr( doc , root , "codebase" , Common.getBooleanValue( codebaseProject ) );
		Common.xmlSetElementAttr( doc , root , "group" , GROUP );
		if( !RESOURCE.isEmpty() ) {
			Common.xmlSetElementAttr( doc , root , "resource" , RESOURCE );
			Common.xmlSetElementAttr( doc , root , "repopath" , REPOPATH );
			Common.xmlSetElementAttr( doc , root , "codepath" , CODEPATH );
		}
		
		if( codebaseProject ) {
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
		r.NAME = NAME;
		r.DESC = DESC;

		// read item attrs
		r.REPOSITORY = REPOSITORY;
		r.codebaseProject = codebaseProject;
		r.codebaseProd = codebaseProd;
		r.GROUP = GROUP;
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
		if( codebaseProject )
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
	
	public List<MetaSourceProjectItem> getIitemList( ActionBase action ) {
		return( itemList );
	}
	
	public Map<String,MetaSourceProjectItem> getIitemMap( ActionBase action ) {
		return( itemMap );
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

	public void setProjectData( ServerTransaction transaction , String group , boolean codebase , String resource , String repoName , String repoPath , String codePath , String branch ) throws Exception {
		this.GROUP = group;
		this.codebaseProject = codebase;
		
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
	
}
