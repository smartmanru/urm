package org.urm.engine.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.engine.ServerAuthResource;
import org.urm.engine.meta.Meta.VarCATEGORY;
import org.urm.engine.meta.Meta.VarNAMETYPE;
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
	public String JAVAVERSION;
	public String BUILDERTYPE;
	public String BUILDERVERSION;
	public String BUILDERCMD;
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
		JAVAVERSION = ConfReader.getAttrValue( node , "javaversion" );
		BUILDERTYPE = ConfReader.getAttrValue( node , "buildertype" );
		BUILDERVERSION = ConfReader.getAttrValue( node , "builderversion" );
		BUILDERCMD = ConfReader.getAttrValue( node , "buildercmd" );
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
				
				itemList.add( distItem );
				itemMap.put( distItem.ITEMNAME , distItem );
			}
		}
		
		// resolve references
		if( !DISTITEM.isEmpty() )
			distItem = meta.distr.getBinaryItem( action , DISTITEM );
		
		CUSTOMBUILD = ConfReader.getBooleanAttrValue( node , "custombuild" , false );
		CUSTOMGET = ConfReader.getBooleanAttrValue( node , "customget" , false );
		
		if( CUSTOMBUILD || CUSTOMGET )
			action.custom.parseProject( action , this , node );
	}

	public String getVCS( ActionBase action ) {
		return( VCS );
	}
	public boolean isGitVCS( ActionBase action ) throws Exception {
		ServerAuthResource res = action.getResource( VCS );
		return( res.TYPE.equals( ServerAuthResource.TYPE_GIT ) );
	}
	public boolean isSvnVCS( ActionBase action ) throws Exception {
		ServerAuthResource res = action.getResource( VCS );
		return( res.TYPE.equals( ServerAuthResource.TYPE_SVN ) );
	}

	public boolean isBuildable() {
		if( CATEGORY == VarCATEGORY.BUILD )
			return( true );
		return( false );
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
		if( !BUILDERTYPE.isEmpty() )
			return( BUILDERTYPE );
		
		MetaProductBuildSettings build = action.getBuildSettings();
		String builder = build.CONFIG_BUILDER_TYPE;
		if( builder.isEmpty() )
			builder = "maven";

		return( builder );
	}

	public String getJavaVersion( ActionBase action ) throws Exception {
		if( !JAVAVERSION.isEmpty() )
			return( JAVAVERSION );
		
		MetaProductBuildSettings build = action.getBuildSettings();
		String version = build.CONFIG_MAVEN_JAVA_VERSION;
		if( version.isEmpty() )
			action.exit0( _Error.UnknownJavaVersion0 , "unknown java version" );
		
		return( version );
	}

	public String getBuilderVersion( ActionBase action ) throws Exception {
		if( !BUILDERVERSION.isEmpty() )
			return( BUILDERVERSION );
		
		String builder = getBuilder( action );
		MetaProductBuildSettings build = action.getBuildSettings();
		String version = build.CONFIG_BUILDER_VERSION;
		if( version.isEmpty() ) {
			if( builder.equals( "maven" ) ) {
				version = build.CONFIG_MAVEN_VERSION;
				if( version.isEmpty() )
					action.exit0( _Error.UnknownMavenVersion0 , "maven version is unknown" );
			}
		}

		if( version.isEmpty() )
			action.exit1( _Error.UnknownBuilderVersion1 , builder + " version is unknown" , builder );
		
		return( version );
	}
	
	public String getDefaultBranch( ActionBase action ) throws Exception {
		if( !BRANCH.isEmpty() )
			return( BRANCH );
		
		MetaProductBuildSettings build = action.getBuildSettings();
		if( !build.CONFIG_BRANCHNAME.isEmpty() )
			return( build.CONFIG_BRANCHNAME );
		
		String branch = PROJECT + "-prod";
		return( branch );
	}
}
