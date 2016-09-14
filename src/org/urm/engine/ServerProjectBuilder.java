package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.shell.Account;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerProjectBuilder {

	public ServerBuilders builders;

	private boolean loaded;
	public boolean loadFailed;

	PropertySet properties;
	
	public String NAME;
	public String TYPE;
	public String HOSTLOGIN;
	public String DESC;
	public String OSTYPE;

	public static String BUILDER_TYPE_MAVEN = "Maven";
	public static String BUILDER_TYPE_GRADLE = "Gradle";
	public static String BUILDER_TYPE_DOTNET = ".NET";
	
	public ServerProjectBuilder( ServerBuilders builders ) {
		this.builders = builders;
		loaded = false;
		loadFailed = false;
	}

	public ServerProjectBuilder copy( ServerBuilders builders ) throws Exception {
		ServerProjectBuilder r = new ServerProjectBuilder( builders );
		r.properties = properties.copy( null );
		r.scatterSystemProperties();
		return( r );
	}
	
	public void load( Node node ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		properties = new PropertySet( "builder" , null );
		properties.loadRawFromNodeElements( node );
		
		scatterSystemProperties();
		properties.finishRawProperties();
	}
	
	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root );
	}
	
	private void scatterSystemProperties() throws Exception {
		NAME = properties.getSystemRequiredStringProperty( "name" );
		TYPE = properties.getSystemRequiredStringProperty( "type" );
		HOSTLOGIN = properties.getSystemRequiredStringProperty( "hostlogin" );
		DESC = properties.getSystemStringProperty( "desc" , "" );
		OSTYPE = properties.getSystemStringProperty( "ostype" , "" );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "builder" , null );
		properties.setStringProperty( "name" , NAME );
		properties.setStringProperty( "type" , TYPE );
		properties.setStringProperty( "hostlogin" , HOSTLOGIN );
		properties.setStringProperty( "desc" , DESC );
		properties.setStringProperty( "ostype" , OSTYPE );
	}

	public boolean isMaven() {
		if( TYPE.equals( BUILDER_TYPE_MAVEN ) )
			return( true );
		return( false );
	}
	
	public boolean isGradle() {
		if( TYPE.equals( BUILDER_TYPE_GRADLE ) )
			return( true );
		return( false );
	}

	public boolean isDotNet() {
		if( TYPE.equals( BUILDER_TYPE_DOTNET ) )
			return( true );
		return( false );
	}

	public void updateBuilder( ServerTransaction transaction , ServerProjectBuilder src ) throws Exception {
		if( !NAME.equals( src.NAME ) )
			transaction.exit( _Error.TransactionBuilderOld1 , "mismatched buider name on change new name=" + src.NAME , new String[] { src.NAME } );
		
		TYPE = src.TYPE;
		HOSTLOGIN = src.HOSTLOGIN;
		DESC = src.DESC;
		OSTYPE = src.OSTYPE;
		
		createProperties();
	}
	
	public void createBuilder() throws Exception {
		createProperties();
	}

	public Account getAccount( ActionBase action ) throws Exception {
		VarOSTYPE osType = VarOSTYPE.valueOf( OSTYPE );
		return( Account.getAccount( action , HOSTLOGIN , osType ) );
	}
	
}
