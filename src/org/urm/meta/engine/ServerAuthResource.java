package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.engine.ServerTransaction;
import org.urm.meta.ServerObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerAuthResource extends ServerObject {

	public enum VarRESOURCECATEGORY {
		ANY ,
		VCS ,
		SSH ,
		NEXUS ,
		SOURCE
	};
	
	public enum VarRESOURCETYPE {
		UNKNOWN ,
		SVN ,
		GIT ,
		NEXUS ,
		SSH
	};
	
	public ServerResources resources;

	private boolean loaded;
	public boolean loadFailed;

	PropertySet properties;
	
	public String NAME;
	public VarRESOURCETYPE rcType;
	public String BASEURL;
	public String DESC;
	public String AUTHKEY;
	public ServerAuthContext ac;
	
	public ServerAuthResource( ServerResources resources ) {
		super( resources );
		this.resources = resources;
		loaded = false;
		loadFailed = false;
	}

	public ServerAuthResource copy( ServerResources resources ) throws Exception {
		ServerAuthResource r = new ServerAuthResource( resources );
		r.properties = properties.copy( null );
		r.scatterSystemProperties();
		if( ac != null )
			r.ac = ac.copy();
		return( r );
	}
	
	public void load( Node node ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		properties = new PropertySet( "resource" , null );
		properties.loadFromNodeElements( node , false );
		
		scatterSystemProperties();
		properties.finishRawProperties();
	}
	
	public void save( Document doc , Element root ) throws Exception {
		properties.saveAsElements( doc , root , false );
		saveAuthData();
	}
	
	private void scatterSystemProperties() throws Exception {
		NAME = properties.getSystemRequiredStringProperty( "name" );
		String TYPE = properties.getSystemRequiredStringProperty( "type" );  
		rcType = ServerAuthResource.getResourceType( TYPE , false );
		BASEURL = properties.getSystemStringProperty( "baseurl" );
		DESC = properties.getSystemStringProperty( "desc" );
		AUTHKEY = properties.getSystemStringProperty( "authkey" );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "resource" , null );
		properties.setOriginalStringProperty( "name" , NAME );
		properties.setOriginalStringProperty( "type" , Common.getEnumLower( rcType ) );
		properties.setOriginalStringProperty( "baseurl" , BASEURL );
		properties.setOriginalStringProperty( "desc" , DESC );
		properties.setOriginalStringProperty( "authkey" , AUTHKEY );
	}

	public boolean isVCS() {
		if( rcType == VarRESOURCETYPE.SVN || rcType == VarRESOURCETYPE.GIT )
			return( true );
		return( false );
	}
	
	public boolean isSvn() {
		if( rcType == VarRESOURCETYPE.SVN )
			return( true );
		return( false );
	}
	
	public boolean isGit() {
		if( rcType == VarRESOURCETYPE.GIT )
			return( true );
		return( false );
	}
	
	public boolean isNexus() {
		if( rcType == VarRESOURCETYPE.NEXUS )
			return( true );
		return( false );
	}

	public boolean isSshKey() {
		if( rcType == VarRESOURCETYPE.SSH )
			return( true );
		return( false );
	}
	
	public void updateResource( ServerTransaction transaction , ServerAuthResource src ) throws Exception {
		if( !NAME.equals( src.NAME ) )
			transaction.exit( _Error.TransactionMismatchedResource1 , "mismatched resource name on change new name=" + src.NAME , new String[] { src.NAME } );
		
		rcType = src.rcType;
		BASEURL = src.BASEURL;
		DESC = src.DESC;
		createProperties();
		
		if( src.ac != null && !src.ac.METHOD.isEmpty() ) {
			ServerAuth auth = resources.engine.getAuth();
			ac = new ServerAuthContext( auth );
			ac.load( src.ac.properties );
		}
	}
	
	public void saveAuthData() throws Exception {
		ServerAuth auth = resources.engine.getAuth();
		AUTHKEY = auth.getAuthKey( ServerAuth.AUTH_GROUP_RESOURCE , NAME );
		properties.setOriginalStringProperty( "authkey" , AUTHKEY );
		
		if( ac != null && !ac.METHOD.isEmpty() )
			auth.saveAuthData( AUTHKEY , ac ); 
	}
	
	public void loadAuthData( ActionBase action ) throws Exception {
		if( ac != null )
			return;
		ServerAuth auth = resources.engine.getAuth();
		ac = auth.loadAuthData( action , AUTHKEY );
	}

	public void createResource() throws Exception {
		ServerAuth auth = resources.engine.getAuth();
		AUTHKEY = auth.getAuthKey( ServerAuth.AUTH_GROUP_RESOURCE , NAME );
		createProperties();
	}

	public static VarRESOURCETYPE getResourceType( String TYPE , boolean required ) throws Exception {
		if( TYPE.isEmpty() ) {
			if( required )
				Common.exit0( _Error.MissingResourceType0 , "missing resource type" );
			return( VarRESOURCETYPE.UNKNOWN );
		}
		
		VarRESOURCETYPE value = null;		
		try {
			value = VarRESOURCETYPE.valueOf( Common.xmlToEnumValue( TYPE ) );
		}
		catch( IllegalArgumentException e ) {
			Common.exit1( _Error.InvalidResourceType1 , "invalid resource type=" + TYPE , TYPE );
		}
		
		return( value );
	}

}
