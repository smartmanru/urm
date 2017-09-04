package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.PropertySet;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.EngineTransaction;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.shell.ShellPool;
import org.urm.engine.storage.NexusStorage;
import org.urm.engine.vcs.GenericVCS;
import org.urm.meta.ServerObject;
import org.urm.meta.Types.VarRESOURCETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerAuthResource extends ServerObject {

	public ServerResources resources;

	private boolean loaded;
	public boolean loadFailed;

	PropertySet properties;
	
	public String NAME;
	public VarRESOURCETYPE rcType;
	public String BASEURL;
	public String DESC;
	public String AUTHKEY;
	public boolean verified;
	public ServerAuthContext ac;
	
	public ServerAuthResource( ServerResources resources ) {
		super( resources );
		this.resources = resources;
		loaded = false;
		loadFailed = false;
		verified = false;
	}

	@Override
	public String getName() {
		return( NAME );
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
		verified = properties.getSystemBooleanProperty( "verified" );
	}

	public void createProperties() throws Exception {
		properties = new PropertySet( "resource" , null );
		properties.setOriginalStringProperty( "name" , NAME );
		properties.setOriginalStringProperty( "type" , Common.getEnumLower( rcType ) );
		properties.setOriginalStringProperty( "baseurl" , BASEURL );
		properties.setOriginalStringProperty( "desc" , DESC );
		properties.setOriginalStringProperty( "authkey" , AUTHKEY );
		properties.setOriginalBooleanProperty( "verified" , verified );
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
	
	public boolean isCredentials() {
		if( rcType == VarRESOURCETYPE.CREDENTIALS )
			return( true );
		return( false );
	}
	
	public void updateResource( EngineTransaction transaction , ServerAuthResource src ) throws Exception {
		if( !NAME.equals( src.NAME ) )
			transaction.exit( _Error.TransactionMismatchedResource1 , "mismatched resource name on change new name=" + src.NAME , new String[] { src.NAME } );
		
		rcType = src.rcType;
		BASEURL = src.BASEURL;
		DESC = src.DESC;
		verified = false;
		createProperties();
		
		if( src.ac != null && !src.ac.METHOD.isEmpty() ) {
			ServerAuth auth = resources.engine.getAuth();
			ac = new ServerAuthContext( auth );
			ac.load( src.ac.properties );
		}
	}

	public void setVerified( EngineTransaction transaction ) throws Exception {
		verified = true;
		createProperties();
	}
	
	public void saveAuthData() throws Exception {
		ServerAuth auth = resources.engine.getAuth();
		AUTHKEY = auth.getAuthKey( ServerAuth.AUTH_GROUP_RESOURCE , NAME );
		properties.setOriginalStringProperty( "authkey" , AUTHKEY );
		
		if( ac != null && !ac.METHOD.isEmpty() )
			auth.saveAuthData( AUTHKEY , ac ); 
	}
	
	public void loadAuthData() throws Exception {
		if( ac != null )
			return;
		ServerAuth auth = resources.engine.getAuth();
		ac = auth.loadAuthData( AUTHKEY );
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

	public boolean vcsVerify( ActionBase action , String repo , String repoPath ) {
		try {
			GenericVCS vcs = GenericVCS.getVCS( action , this );
			if( vcs.verifyRepository( repo , repoPath ) )
				return( true );
		}
		catch( Throwable e ) {
			action.log( "verify vcs resource" , e );
		}
		return( false );
	}
	
	public boolean sshVerify( ActionBase action , VarOSTYPE osType , String host , int port , String user ) {
		try {
			loadAuthData();
			Account account = Account.getResourceAccount( action , NAME , user , host , port , osType );
			ShellPool pool = action.engine.shellPool;
			ShellExecutor shell = pool.createDedicatedRemoteShell( action , action.context.stream , account , this , false );
			
			if( shell != null ) {
				boolean res = false;
				if( shell.isRunning() )
					res = true;
				
				shell.kill( action );
				return( res );
			}
		}
		catch( Throwable e ) {
			action.log( "verify ssh resource" , e );
		}
		return( false );
	}
	
	public boolean nexusVerify( ActionBase action , String repo ) {
		try {
			if( NexusStorage.verifyRepository( action , repo , this ) )
				return( true );
		}
		catch( Throwable e ) {
			action.log( "verify nexus resource" , e );
		}
		return( false );
	}
	
}
