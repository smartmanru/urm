package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.urm.engine.ServerTransaction;
import org.urm.engine.TransactionBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductBuildSettings extends PropertyController {

	public String name;
	public Meta meta;
	public MetaProductSettings product;
	
	public String CONFIG_RELEASE_LASTMAJOR;
	public String CONFIG_RELEASE_NEXTMAJOR;
	public String CONFIG_RELEASE_LASTMINOR;
	public String CONFIG_RELEASE_NEXTMINOR;
	public String CONFIG_RELEASE_VERSION;
	public String CONFIG_APPVERSION;
	public String CONFIG_LOGPATH;
	
	public String CONFIG_ARTEFACTDIR;
	public String CONFIG_NEXUS_REPO;
	public String CONFIG_NEXUS_REPO_THIRDPARTY;
	public String CONFIG_MAVEN_CFGFILE;

	public String CONFIG_COMMIT_TRACKERLIST;
	public String CONFIG_BRANCHNAME;
	public String CONFIG_RELEASE_GROUPFOLDER;

	// version
	public static String PROPERTY_RELEASE_LASTMAJOR = "release.lastmajor";
	public static String PROPERTY_RELEASE_NEXTMAJOR = "release.nextmajor";
	public static String PROPERTY_RELEASE_LASTMINOR = "release.lastminor";
	public static String PROPERTY_RELEASE_NEXTMINOR = "release.nextminor";
	public static String PROPERTY_RELEASE_VERSION = "release.version";
	public static String PROPERTY_APPVERSION = "app.version";
	public static String PROPERTY_LOGPATH = "app.logpath";

	// build operation
	public static String PROPERTY_ARTEFACTDIR = "build.artefacts";
	public static String PROPERTY_NEXUS_REPO = "nexus.repo";
	public static String PROPERTY_NEXUS_REPO_THIRDPARTY = "nexus.thirdparty";
	public static String PROPERTY_MAVEN_CFGFILE = "maven.conf";

	// build source code
	public static String PROPERTY_BRANCHNAME = "source.branch";
	public static String PROPERTY_RELEASE_GROUPFOLDER = "release.group";

	public static String BUILDER_TYPE_MAVEN = "Maven";
	public static String BUILDER_TYPE_GRADLE = "Gradle";
	public static String BUILDER_TYPE_DOTNET = ".NET";
	
	public MetaProductBuildSettings( String name , Meta meta , MetaProductSettings product ) {
		super( product , name );
		
		this.name = name;
		this.meta = meta;
		this.product = product;
	}
	
	@Override
	public String getName() {
		return( "meta-build-settings" );
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}

	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		CONFIG_RELEASE_LASTMAJOR = super.getStringProperty( action , PROPERTY_RELEASE_LASTMAJOR );
		CONFIG_RELEASE_NEXTMAJOR = super.getStringProperty( action , PROPERTY_RELEASE_NEXTMAJOR );
		CONFIG_RELEASE_LASTMINOR = super.getStringProperty( action , PROPERTY_RELEASE_LASTMINOR );
		CONFIG_RELEASE_NEXTMINOR = super.getStringProperty( action , PROPERTY_RELEASE_NEXTMINOR );
		CONFIG_RELEASE_VERSION = super.getStringProperty( action , PROPERTY_RELEASE_VERSION );
		CONFIG_APPVERSION = super.getStringProperty( action , PROPERTY_APPVERSION );
		CONFIG_LOGPATH = super.getPathProperty( action , PROPERTY_LOGPATH );
		
		CONFIG_ARTEFACTDIR = super.getStringProperty( action , PROPERTY_ARTEFACTDIR );
		CONFIG_NEXUS_REPO = super.getStringProperty( action , PROPERTY_NEXUS_REPO );
		CONFIG_NEXUS_REPO_THIRDPARTY = super.getStringProperty( action , PROPERTY_NEXUS_REPO_THIRDPARTY );
		CONFIG_MAVEN_CFGFILE = super.getStringProperty( action , PROPERTY_MAVEN_CFGFILE );

		CONFIG_BRANCHNAME = super.getStringProperty( action , PROPERTY_BRANCHNAME );
		CONFIG_RELEASE_GROUPFOLDER = super.getStringProperty( action , PROPERTY_RELEASE_GROUPFOLDER );
	}

	public void createSettings( TransactionBase transaction , PropertySet src , PropertySet parent ) throws Exception {
		if( !super.initCreateStarted( parent ) )
			return;

		if( src != null )
			super.copyOriginalPropertiesToRaw( src );
		
		super.updateProperties( transaction.action );
		super.initFinished();
	}
	
	public MetaProductBuildSettings copy( ActionBase action , Meta meta , MetaProductSettings product , PropertySet parent ) throws Exception {
		MetaProductBuildSettings r = new MetaProductBuildSettings( name , meta , product );
		r.initCopyStarted( this , parent );
		r.updateProperties( action );
		r.initFinished();
		
		return( r );
	}
	
	public void load( ActionBase action , Node root , PropertySet parent ) throws Exception {
		if( !initCreateStarted( parent ) )
			return;

		super.loadFromNodeElements( action , root , false );
		super.updateProperties( action );
		super.initFinished();
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveAsElements( doc , root , false );
	}

	public void setProperties( ServerTransaction transaction , PropertySet props ) throws Exception {
		super.updateProperties( transaction , props , true );
	}

}
