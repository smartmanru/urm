package org.urm.meta.product;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.custom.CommandCustom;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaSourceProject {

	public Meta meta;
	public MetaSourceProjectSet set;
	public VarCATEGORY CATEGORY;
	
	public String PROJECT;
	private String VCS;
	public String REPOSITORY;
	public String VERSION;
	public String GROUP;
	public String PATH;
	public String CODEPATH;
	public String JIRA;
	public String BRANCH;
	public String BUILDER;
	public String DISTITEM;
	public MetaDistrBinaryItem distItem;
	public String DISTLIBITEM;

	List<MetaSourceProjectItem> itemList = new LinkedList<MetaSourceProjectItem>();
	Map<String,MetaSourceProjectItem> itemMap = new HashMap<String,MetaSourceProjectItem>();

	public boolean CUSTOMBUILD;
	public boolean CUSTOMGET;
	
	public MetaSourceProject( Meta meta , MetaSourceProjectSet set ) {
		this.meta = meta;
		this.set = set;
		this.CATEGORY = set.CATEGORY;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		PROJECT = action.getNameAttr( node , VarNAMETYPE.ALPHANUMDOTDASH );

		// read item attrs
		REPOSITORY = ConfReader.getAttrValue( node , "repository" );
		if( REPOSITORY.isEmpty() )
			REPOSITORY = PROJECT;

		VERSION = ConfReader.getRequiredAttrValue( node , "version" );
		GROUP = ConfReader.getAttrValue( node , "group" );
		JIRA = ConfReader.getAttrValue( node , "jira" );
		BRANCH = ConfReader.getAttrValue( node , "branch" );
		BUILDER = ConfReader.getAttrValue( node , "builder" );
		DISTITEM = ConfReader.getAttrValue( node , "distitem" );

		if( CATEGORY != VarCATEGORY.PREBUILT ) {
			VCS = ConfReader.getRequiredAttrValue( node , "vcs" );
			if( !VCS.equals( "none" ) ) {
				PATH = ConfReader.getRequiredAttrValue( node , "path" );
				CODEPATH = ConfReader.getAttrValue( node , "codepath" );
			}
		}
		
		if( BRANCH.isEmpty() )
			BRANCH = PROJECT + "-prod";
		
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
		if( !DISTITEM.isEmpty() ) {
			MetaDistr distr = meta.getDistr( action );
			distItem = distr.getBinaryItem( action , DISTITEM );
		}
		
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
		Common.xmlSetElementAttr( doc , root , "name" , PROJECT );

		// read item attrs
		Common.xmlSetElementAttr( doc , root , "repository" , REPOSITORY );
		Common.xmlSetElementAttr( doc , root , "version" , VERSION );
		Common.xmlSetElementAttr( doc , root , "group" , GROUP );
		Common.xmlSetElementAttr( doc , root , "jira" , JIRA );
		Common.xmlSetElementAttr( doc , root , "branch" , BRANCH );
		Common.xmlSetElementAttr( doc , root , "builder" , BUILDER );
		Common.xmlSetElementAttr( doc , root , "distitem" , DISTITEM );
		Common.xmlSetElementAttr( doc , root , "vcs" , VCS );
		Common.xmlSetElementAttr( doc , root , "path" , PATH );
		Common.xmlSetElementAttr( doc , root , "codepath" , CODEPATH );
		
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
		r.PROJECT = PROJECT;

		// read item attrs
		r.REPOSITORY = REPOSITORY;
		r.VERSION = VERSION;
		r.GROUP = GROUP;
		r.JIRA = JIRA;
		r.BRANCH = BRANCH;
		r.BUILDER = BUILDER;
		r.DISTITEM = DISTITEM;
		r.VCS = VCS;
		r.PATH = PATH;
		r.CODEPATH = CODEPATH;
		
		// project items
		for( MetaSourceProjectItem item : itemList ) {
			MetaSourceProjectItem ritem = item.copy( action , meta , r );
			addItem( ritem );
		}
		
		// resolve references
		if( !DISTITEM.isEmpty() ) {
			MetaDistr distr = meta.getDistr( action );
			r.distItem = distr.getBinaryItem( action , DISTITEM );
		}
		
		r.CUSTOMBUILD = CUSTOMBUILD;
		r.CUSTOMGET = CUSTOMGET;
		return( r );
	}
	
	public String getVCS( ActionBase action ) {
		return( VCS );
	}
	public boolean isGitVCS( ActionBase action ) throws Exception {
		ServerAuthResource res = action.getResource( VCS );
		return( res.isGit() );
	}
	public boolean isSvnVCS( ActionBase action ) throws Exception {
		ServerAuthResource res = action.getResource( VCS );
		return( res.isSvn() );
	}

	public boolean isBuildable() {
		if( CATEGORY == VarCATEGORY.BUILD )
			return( true );
		return( false );
	}

	public MetaSourceProjectItem findItem( String name ) {
		return( itemMap.get( name ) );
	}
	
	public MetaSourceProjectItem getItem( ActionBase action , String name ) throws Exception {
		MetaSourceProjectItem item = itemMap.get( name );
		if( item == null )
			action.exit2( _Error.UnknownSourceProjectItem2 , "unknown source project item=" + name + ", in project=" + PROJECT , PROJECT , name );
		
		return( item );
	}
	
	public boolean isEmpty( ActionBase action ) throws Exception {
		if( distItem != null )
			return( false );
		
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
		
		String branch = PROJECT + "-prod";
		return( branch );
	}
	
	public String[] getItemNames() {
		return( Common.getSortedKeys( itemMap ) );
	}
	
}
