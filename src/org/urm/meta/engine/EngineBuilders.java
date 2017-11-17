package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineBuilders extends EngineObject {

	public EngineRegistry registry;

	public boolean registerBuild;
	public String MAVEN_HOMEPATH;
	public String MAVEN_VERSION;
	public String MAVEN_SETTINGS;
	public String JAVA_HOMEPATH;
	public String NEXUS_RESOURCE;
	public String NEXUS_ROOTPATH;
	
	Map<String,ProjectBuilder> builderMap;

	public EngineBuilders( EngineRegistry registry ) {
		super( registry );
		this.registry = registry;
		
		builderMap = new HashMap<String,ProjectBuilder>();
	}

	@Override
	public String getName() {
		return( "server-builders" );
	}
	
	public EngineBuilders copy() throws Exception {
		EngineBuilders r = new EngineBuilders( registry );

		r.registerBuild = registerBuild;
		r.MAVEN_HOMEPATH = MAVEN_HOMEPATH;
		r.MAVEN_VERSION = MAVEN_VERSION;
		r.MAVEN_SETTINGS = MAVEN_SETTINGS;
		r.JAVA_HOMEPATH = JAVA_HOMEPATH;
		r.NEXUS_RESOURCE = NEXUS_RESOURCE;
		r.NEXUS_ROOTPATH = NEXUS_ROOTPATH;
		
		for( ProjectBuilder res : builderMap.values() ) {
			ProjectBuilder rc = res.copy( r );
			r.builderMap.put( rc.NAME , rc );
		}
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		registerBuild = ConfReader.getBooleanPropertyValue( root , "register" , false );
		if( registerBuild ) {
			MAVEN_HOMEPATH = ConfReader.getPropertyValue( root , "maven.homepath" );
			MAVEN_VERSION = ConfReader.getPropertyValue( root , "maven.version" );
			MAVEN_SETTINGS = ConfReader.getPropertyValue( root , "maven.settings" );
			JAVA_HOMEPATH = ConfReader.getPropertyValue( root , "java.homepath" );
			NEXUS_RESOURCE = ConfReader.getPropertyValue( root , "nexus.resource" );
			NEXUS_ROOTPATH = ConfReader.getPropertyValue( root , "nexus.rootpath" );
		}
		
		Node[] list = ConfReader.xmlGetChildren( root , "builder" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ProjectBuilder builder = new ProjectBuilder( this );
			builder.load( node );

			builderMap.put( builder.NAME , builder );
		}
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlCreateBooleanPropertyElement( doc , root , "register" , registerBuild );
		if( registerBuild ) {
			Common.xmlCreatePropertyElement( doc , root , "maven.homepath" , MAVEN_HOMEPATH );
			Common.xmlCreatePropertyElement( doc , root , "maven.version" , MAVEN_VERSION );
			Common.xmlCreatePropertyElement( doc , root , "maven.settings" , MAVEN_SETTINGS );
			Common.xmlCreatePropertyElement( doc , root , "java.homepath" , JAVA_HOMEPATH );
			Common.xmlCreatePropertyElement( doc , root , "nexus.resource" , NEXUS_RESOURCE );
			Common.xmlCreatePropertyElement( doc , root , "nexus.rootpath" , NEXUS_ROOTPATH );
		}
		
		for( ProjectBuilder res : builderMap.values() ) {
			Element resElement = Common.xmlCreateElement( doc , root , "builder" );
			res.save( doc , resElement );
		}
	}

	public ProjectBuilder findBuilder( String name ) {
		ProjectBuilder builder = builderMap.get( name );
		return( builder );
	}

	public ProjectBuilder getBuilder( String name ) throws Exception {
		ProjectBuilder builder = builderMap.get( name );
		if( builder == null )
			Common.exit1( _Error.UnknownBuilder1 , "unknown builder=" + name , name );
		return( builder );
	}

	public String[] getList() {
		return( Common.getSortedKeys( builderMap ) );
	}
	
	public ProjectBuilder createBuilder( EngineTransaction transaction , ProjectBuilder builderNew ) throws Exception {
		if( builderMap.get( builderNew.NAME ) != null )
			transaction.exit( _Error.BuilderAlreadyExists1 , "builder already exists name=" + builderNew.NAME , new String[] { builderNew.NAME } );
			
		ProjectBuilder builder = new ProjectBuilder( this );
		builder.setBuilderData( transaction ,  builderNew );
		builderMap.put( builder.NAME , builder );
		return( builder );
	}
	
	public void deleteBuilder( EngineTransaction transaction , ProjectBuilder builder ) throws Exception {
		if( builderMap.get( builder.NAME ) == null )
			transaction.exit( _Error.UnknownBuilder1 , "unknown builder name=" + builder.NAME , new String[] { builder.NAME } );
			
		builderMap.remove( builder.NAME );
	}

	public void setRegisterData( EngineTransaction transaction , boolean regUse , String regMavenHome , String regMavenVersion , String regMavenSettings , String regJavaHome , String regNexusRes , String regNexusRoot ) throws Exception {
		this.registerBuild = regUse;
		if( regUse ) {
			this.MAVEN_HOMEPATH = regMavenHome;
			this.MAVEN_VERSION = regMavenVersion;
			this.MAVEN_SETTINGS = regMavenSettings;
			this.JAVA_HOMEPATH = regJavaHome;
			this.NEXUS_RESOURCE = regNexusRes;
			this.NEXUS_ROOTPATH = regNexusRoot;
		}
		else {
			this.MAVEN_HOMEPATH = "";
			this.MAVEN_VERSION = "";
			this.MAVEN_SETTINGS = "";
			this.JAVA_HOMEPATH = "";
			this.NEXUS_RESOURCE = "";
			this.NEXUS_ROOTPATH = "";
		}
	}
	
}
