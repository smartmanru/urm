package org.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.meta.Metadata.VarCATEGORY;
import org.urm.meta.Metadata.VarNAMETYPE;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaSourceProject {

	Metadata meta;
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
	
	public MetaSourceProject( Metadata meta , MetaSourceProjectSet set ) {
		this.meta = meta;
		this.set = set;
		this.CATEGORY = set.CATEGORY;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		PROJECT = ConfReader.getNameAttr( action , node , VarNAMETYPE.ALPHANUMDOTDASH );

		// read item attrs
		REPOSITORY = ConfReader.getAttrValue( action , node , "repository" );
		if( REPOSITORY.isEmpty() )
			REPOSITORY = PROJECT;

		VERSION = ConfReader.getRequiredAttrValue( action , node , "version" );
		GROUP = ConfReader.getAttrValue( action , node , "group" );
		JIRA = ConfReader.getAttrValue( action , node , "jira" );
		BRANCH = ConfReader.getAttrValue( action , node , "branch" );
		JAVAVERSION = ConfReader.getAttrValue( action , node , "javaversion" );
		BUILDERTYPE = ConfReader.getAttrValue( action , node , "buildertype" );
		BUILDERVERSION = ConfReader.getAttrValue( action , node , "builderversion" );
		BUILDERCMD = ConfReader.getAttrValue( action , node , "buildercmd" );
		DISTITEM = ConfReader.getAttrValue( action , node , "distitem" );

		if( CATEGORY != VarCATEGORY.PREBUILT ) {
			VCS = ConfReader.getRequiredAttrValue( action , node , "vcs" );
			if( !VCS.equals( "none" ) ) {
				PATH = ConfReader.getRequiredAttrValue( action , node , "path" );
				CODEPATH = ConfReader.getAttrValue( action , node , "codepath" );
			}
		}
		
		if( BRANCH.isEmpty() )
			BRANCH = PROJECT + "-prod";
		
		// read project items
		Node[] items = ConfReader.xmlGetChildren( action , node , "distitem" );
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
		
		CUSTOMBUILD = ConfReader.getBooleanAttrValue( action , node , "custombuild" , false );
		CUSTOMGET = ConfReader.getBooleanAttrValue( action , node , "customget" , false );
		
		if( CUSTOMBUILD || CUSTOMGET )
			action.custom.parseProject( action , this , node );
	}

	public String getVCS( ActionBase action ) {
		return( VCS );
	}
	public boolean isSvnOldVCS( ActionBase action ) {
		return( VCS.equals( "svnold" ) );
	}
	public boolean isSvnNewVCS( ActionBase action ) {
		return( VCS.equals( "svn" ) || VCS.equals( "svnnew" ) );
	}
	public boolean isGitVCS( ActionBase action ) {
		return( VCS.equals( "git" ) );
	}
	public boolean isSvn( ActionBase action ) {
		return( isSvnOldVCS( action ) || isSvnNewVCS( action ) );
	}
	
	public MetaSourceProjectItem getItem( ActionBase action , String name ) throws Exception {
		MetaSourceProjectItem item = itemMap.get( name );
		if( item == null )
			action.exit( "unknown item=" + name + ", in project=" + PROJECT );
		
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
	
	public String getBuilder( ActionBase action ) {
		if( !BUILDERTYPE.isEmpty() )
			return( BUILDERTYPE );
		
		String builder = meta.product.CONFIG_BUILDER_TYPE;
		if( builder.isEmpty() )
			builder = "maven";

		return( builder );
	}

	public String getJavaVersion( ActionBase action ) throws Exception {
		if( !JAVAVERSION.isEmpty() )
			return( JAVAVERSION );
		
		String version = meta.product.CONFIG_JAVA_VERSION;
		if( version.isEmpty() )
			action.exit( "BUILD_JAVA_VERSION is not defined - java version is unknown" );
		
		return( version );
	}

	public String getBuilderVersion( ActionBase action ) throws Exception {
		if( !BUILDERVERSION.isEmpty() )
			return( BUILDERVERSION );
		
		String builder = getBuilder( action );
		String version = meta.product.CONFIG_BUILDER_VERSION;
		if( version.isEmpty() ) {
			if( builder.equals( "maven" ) ) {
				version = meta.product.CONFIG_MAVEN_VERSION;
				if( version.isEmpty() )
					action.exit( "MAVEN_VERSION is not defined - maven version is unknown" );
			}
		}

		if( version.isEmpty() )
			action.exit( "CONFIG_BUILDER_VERSION is not defined - " + builder + " version is unknown" );
		
		return( version );
	}
	
	public String getDefaultBranch( ActionBase action ) throws Exception {
		if( !BRANCH.isEmpty() )
			return( BRANCH );
		
		if( !meta.product.CONFIG_BRANCHNAME.isEmpty() )
			return( meta.product.CONFIG_BRANCHNAME );
		
		String branch = PROJECT + "-prod";
		return( branch );
	}
}
