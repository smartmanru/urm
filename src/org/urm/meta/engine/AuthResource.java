package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.shell.EngineShellPool;
import org.urm.engine.storage.NexusStorage;
import org.urm.engine.vcs.GenericVCS;
import org.urm.meta.EngineObject;
import org.urm.db.core.DBEnums.*;

public class AuthResource extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_RESOURCE_TYPE = "type";
	public static String PROPERTY_BASEURL = "baseurl";
	public static String PROPERTY_VERIFIED = "verified";
	
	public EngineResources resources;

	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumResourceType RESOURCE_TYPE;
	public String BASEURL;
	public boolean VERIFIED;
	public int CV;
	
	public AuthContext ac;
	
	public AuthResource( EngineResources resources ) {
		super( resources );
		this.resources = resources;
		ID = -1;
		CV = 0;
		VERIFIED = false;
	}

	@Override
	public String getName() {
		return( NAME );
	}

	public static Integer getId( AuthResource resource ) {
		if( resource == null )
			return( null );
		return( resource.ID );
	}
	
	public AuthResource copy( EngineResources resources ) throws Exception {
		AuthResource r = new AuthResource( resources );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.RESOURCE_TYPE = RESOURCE_TYPE;
		r.BASEURL = BASEURL;
		r.VERIFIED = VERIFIED;
		r.CV = CV;
		
		if( ac != null )
			r.ac = ac.copy();
		return( r );
	}
	
	public boolean isVCS() {
		if( RESOURCE_TYPE == DBEnumResourceType.SVN || RESOURCE_TYPE == DBEnumResourceType.GIT )
			return( true );
		return( false );
	}
	
	public boolean isSvn() {
		if( RESOURCE_TYPE == DBEnumResourceType.SVN )
			return( true );
		return( false );
	}
	
	public boolean isGit() {
		if( RESOURCE_TYPE == DBEnumResourceType.GIT )
			return( true );
		return( false );
	}
	
	public boolean isNexus() {
		if( RESOURCE_TYPE == DBEnumResourceType.NEXUS )
			return( true );
		return( false );
	}

	public boolean isSshKey() {
		if( RESOURCE_TYPE == DBEnumResourceType.SSH )
			return( true );
		return( false );
	}
	
	public boolean isCredentials() {
		if( RESOURCE_TYPE == DBEnumResourceType.CREDENTIALS )
			return( true );
		return( false );
	}

	public void createResource( String name , String desc , DBEnumResourceType type , String baseurl ) throws Exception {
		modifyResource( name , desc , type , baseurl );
	}
	
	public void modifyResource( String name , String desc , DBEnumResourceType type , String baseurl ) throws Exception {
		NAME = name;
		DESC = desc;
		
		if( type != RESOURCE_TYPE ||
			baseurl.equals( BASEURL ) == false )
			VERIFIED = false;
			
		RESOURCE_TYPE = type;
		BASEURL = baseurl;
		
		ac = null;
	}

	public void setVerified( boolean verified ) throws Exception {
		this.VERIFIED = verified;
	}
	
	public void setAuthData( AuthContext acdata ) throws Exception {
		EngineAuth auth = resources.engine.getAuth();
		String authKey = auth.getAuthKey( EngineAuth.AUTH_GROUP_RESOURCE , NAME );
		ac = auth.loadAuthData( authKey );
		ac.setData( acdata );
		
		this.VERIFIED = false;
	}
	
	public void saveAuthData() throws Exception {
		EngineAuth auth = resources.engine.getAuth();
		if( ac != null ) {
			String authKey = auth.getAuthKey( EngineAuth.AUTH_GROUP_RESOURCE , NAME );
			auth.saveAuthData( authKey , ac );
		}
	}
	
	public void loadAuthData() throws Exception {
		if( ac != null )
			return;
		
		EngineAuth auth = resources.engine.getAuth();
		String authKey = auth.getAuthKey( EngineAuth.AUTH_GROUP_RESOURCE , NAME );
		ac = auth.loadAuthData( authKey );
	}

	public void createResource() throws Exception {
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
	
	public boolean sshVerify( ActionBase action , DBEnumOSType osType , String host , int port , String user ) {
		try {
			loadAuthData();
			Account account = Account.getResourceAccount( action , this , user , host , port , osType );
			EngineShellPool pool = action.engine.shellPool;
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
