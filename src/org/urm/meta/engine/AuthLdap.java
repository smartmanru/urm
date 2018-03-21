package org.urm.meta.engine;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.urm.common.Common;
import org.urm.engine.AuthService;
import org.urm.engine.data.EngineResources;
import org.urm.engine.properties.ObjectProperties;

public class AuthLdap {

	public static String PROPERTY_LDAPUSE = "useldap";
	public static String PROPERTY_HOST = "host";
	public static String PROPERTY_PORT = "port";
	public static String PROPERTY_LOGIN_RESOURCE = "resource";
	public static String PROPERTY_USERDN = "userdn";
	public static String PROPERTY_USERCLASS = "userclass";
	public static String PROPERTY_USERFILTER = "userfilter";
	public static String PROPERTY_NAMEATTR = "nameattr";
	public static String PROPERTY_DISPLAYNAMEATTR = "displaynameattr";
	public static String PROPERTY_EMAILATTR = "emailattr";
	public static String PROPERTY_PASSWORDATTR = "passwordattr";
	
	AuthService auth;
	
	public boolean ldapUse;
	public String ldapHost;
	public int ldapPort;
	public Integer ldapUserRes;
	public String userDN;
	public String userClass;
	public String userFilter;
	public String userNameAttr;
	public String userDisplayNameAttr;
	public String userEmailAttr;
	public String userPasswordAttr;

	private static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	private final Hashtable<String,String> LDAP_ENV = new Hashtable<>(); // can't use Map
	private final SearchControls SEARCH_CONTROLS = new SearchControls(); // NOTE: Not a thread-safe class
	private String USER_FILTER;
	public String PROVIDER_URL;

	boolean ldapStarted;
	boolean ldapFailed;

	ObjectProperties props;
	
	public AuthLdap( AuthService auth ) {
		this.auth = auth;
		ldapStarted = false;
		ldapFailed = false;
		setNotUse();
	}

	public void setLdapSettings( ObjectProperties props ) throws Exception {
		stop();
		this.props = props;
		
		ldapUse = props.getBooleanProperty( PROPERTY_LDAPUSE );
		if( ldapUse ) {
			ldapHost = props.getStringProperty( PROPERTY_HOST );
			ldapPort = props.getIntProperty( PROPERTY_PORT );
			ldapUserRes = props.getObjectProperty( PROPERTY_LOGIN_RESOURCE );
			userDN = props.getStringProperty( PROPERTY_USERDN );
			userClass = props.getStringProperty( PROPERTY_USERCLASS );
			userFilter = props.getStringProperty( PROPERTY_USERFILTER );
			userNameAttr = props.getStringProperty( PROPERTY_DISPLAYNAMEATTR );
			userDisplayNameAttr = props.getStringProperty( PROPERTY_NAMEATTR );
			userEmailAttr = props.getStringProperty( PROPERTY_EMAILATTR );
			userPasswordAttr = props.getStringProperty( PROPERTY_PASSWORDATTR );
			
			this.PROVIDER_URL = "ldap://" + ldapHost + ":";
			if( ldapPort > 0 )
				this.PROVIDER_URL += ldapPort;
			else
				this.PROVIDER_URL += "389";
			
			if( !userDN.isEmpty() )
				this.PROVIDER_URL += "/" + userDN;
		}
		else
			setNotUse();
	}

	public ObjectProperties getLdapSettings() {
		return( props );
	}
	
	public void setNotUse() {
		ldapStarted = false;
		ldapFailed = false;
		
		ldapUse = false;
		ldapPort = 0;
		ldapHost = "";
		ldapUserRes = null;
		userDN = "";
		userClass = "";
		userFilter = "";
		userNameAttr = "";
		userDisplayNameAttr = "";
		userEmailAttr = "";
		userPasswordAttr = "";
		PROVIDER_URL = null;
	}

	public static void setServerData( ObjectProperties ops , String host , int port , Integer resource ) throws Exception {
		ops.setIntProperty( PROPERTY_PORT , port );
		ops.setStringProperty( PROPERTY_HOST, host );
		ops.setObjectProperty( PROPERTY_LOGIN_RESOURCE , resource );
	}

	public static void setUserData( ObjectProperties ops , String userDN , String userClass , String userFilter , String userNameAttr , String userDisplayNameAttr , String userEmailAttr , String userPasswordAttr ) throws Exception {
		ops.setStringProperty( PROPERTY_USERDN , userDN );
		ops.setStringProperty( PROPERTY_USERCLASS , userClass );
		ops.setStringProperty( PROPERTY_USERFILTER , userFilter );
		ops.setStringProperty( PROPERTY_NAMEATTR , userNameAttr );
		ops.setStringProperty( PROPERTY_DISPLAYNAMEATTR , userDisplayNameAttr );
		ops.setStringProperty( PROPERTY_EMAILATTR , userEmailAttr );
		ops.setStringProperty( PROPERTY_PASSWORDATTR , userPasswordAttr );
	}
	
	private void create() throws Exception {
		if( !ldapUse )
			return;
		
		// The LDAP server's address
		if( ldapHost.isEmpty() )
			return;
		
		// Filter to find user's record; note that {} is replaced by the user-supplied ID
		this.USER_FILTER = getUserFilter( false , userFilter );
			
		// Specify we want to use the LDAP protocol for this directory
		LDAP_ENV.put( Context.INITIAL_CONTEXT_FACTORY , INITIAL_CONTEXT_FACTORY );
		// URL to the LDAP server
		LDAP_ENV.put( Context.PROVIDER_URL , PROVIDER_URL );
		// "simple" if a read-only search account is required; "none" if anonymous
		LDAP_ENV.put( Context.SECURITY_AUTHENTICATION , "simple" );
		// The read-only account's DN, if not anonymous.  e.g. "uid=admin,ou=system"
		if( ldapUserRes == null )
			return;
		
		EngineResources resources = auth.engine.getData().getResources();
		AuthResource res = resources.getResource( ldapUserRes );
		res.loadAuthData();
		if( !res.ac.isCommon() )
			return;
		
		LDAP_ENV.put( Context.SECURITY_PRINCIPAL , res.ac.USER );
		// The read-only account's password, if not anonymous
		LDAP_ENV.put( Context.SECURITY_CREDENTIALS , res.ac.PASSWORDSAVE ); 
		// Search filters will access their context's entire subtree...
		SEARCH_CONTROLS.setSearchScope( SearchControls.SUBTREE_SCOPE );
	}

	public void start() throws Exception {
		if( ldapStarted )
			return;
		
		if( !ldapUse )
			return;
		
		create();
		ldapStarted = true;
		ldapFailed = false;
	}
	
	public void stop() {
		ldapStarted = false;
		ldapFailed = false;
		USER_FILTER = null;
		LDAP_ENV.clear();
	}
	
	private String getUserFilter( boolean allUsers , String userFilter ) {
		String filter = Common.nonull( userFilter );
		if( filter.isEmpty() ) {
			if( userClass.isEmpty() ) {
				if( allUsers )
					filter = "(" + userNameAttr + "=*)";
				else
					filter = "(" + userNameAttr + "={})";
			}
			else {
				if( allUsers )
					filter = "(&(objectClass=" + userClass + ")(" + userNameAttr + "=*))";
				else
					filter = "(&(objectClass=" + userClass + ")(" + userNameAttr + "={}))";
			}
		}
		return( filter );
	}
	
	public SearchResult findUser( String username ) throws Exception {
		String dnFilter = USER_FILTER.replace( "{}", username );
		DirContext ctx = new InitialDirContext( LDAP_ENV );  // throws NamingException
		try {
			NamingEnumeration<SearchResult> results = ctx.search( "" , dnFilter , SEARCH_CONTROLS ); // throws NamingException
			if( results.hasMore() )			{
				SearchResult searchResult = results.nextElement();
				if( results.hasMore() ) { 
					throw new NamingException( "Unexpectedly found multiple records for that user." ); 
				}
				
				return( searchResult );
			}
			
			return( null );
		}
		finally { 
			try { 
				ctx.close(); 
			} 
			catch( NamingException e ) { 
				auth.engine.serverAction.log( "find LDAP user=" + username , e );
			} 
		}
	}

	public AuthContext getAuthContext( SearchResult res ) throws Exception {
		AuthContext ac = new AuthContext( auth );
		ac.createLdap( getAttr( res , userNameAttr ) );
		return( ac );
	}
	
	public SearchResult verifyLogin( String username , String password ) {
		try {
			SearchResult user = findUser( username );
			if( user == null )
				return( null );
			
			if( !verifyLogin( user , password ) )
				return( null );
			return( user );
		}
		catch( Throwable e ) {
			auth.engine.serverAction.log( "verify user=" + username , e );
		}
		return( null );
	}
	
	public boolean verifyLogin( SearchResult user , String password ) {
		if( !startUse() )
			return( false );
		
		String dn = user.getNameInNamespace();
		Hashtable<String,String> env = new Hashtable<>( LDAP_ENV );
		env.put( Context.SECURITY_AUTHENTICATION , "simple" );
		env.put( Context.SECURITY_PRINCIPAL , dn );
		env.put( Context.SECURITY_CREDENTIALS , password );

		try {
			DirContext ctx = new InitialDirContext( env );
			ctx.close();
		}
		catch( Throwable e ) {
			return( false );
		}
		return( true );
	}
	
	public String getAttr( SearchResult res , String attrName ) throws Exception {
		if( attrName.isEmpty() )
			return( "" );
		Attribute attr = res.getAttributes().get( attrName );
		if( attr == null )
			return( "" );
		Object obj = attr.get();
		if( obj == null )
			return( null );
		return( obj.toString() );
	}

	private boolean startUse() {
		if( !ldapUse )
			return( false );
		
		try {
			if( !ldapStarted ) {
				if( ldapFailed )
					return( false );
				
				start();
			}
			return( true );
		}
		catch( Throwable e ) {
			auth.engine.log( "start using LDAP" , e );
			ldapFailed = true;
			return( false );
		}
	}
	
	public AuthLdapUser getLdapUserData( String username ) throws Exception {
		if( !startUse() )
			return( null );
		
		SearchResult res = findUser( username );
		AuthLdapUser user = new AuthLdapUser( username , getAttr( res , userEmailAttr ) , getAttr( res , userDisplayNameAttr ) );
		return( user );
	}

	public String[] getUserList() throws Exception {
		if( !startUse() )
			return( new String[0] );
		
		String dnFilter = getUserFilter( true , "" );
		DirContext ctx = new InitialDirContext( LDAP_ENV );

		List<String> users = new LinkedList<String>(); 
		try {
			NamingEnumeration<SearchResult> results = ctx.search( "" , dnFilter , SEARCH_CONTROLS ); // throws NamingException
			while( results.hasMore() ) {
				SearchResult res = results.nextElement();
				String name = getAttr( res , userNameAttr );
				if( !name.isEmpty() )
					users.add( name );
			}
		}
		finally { 
			try { 
				ctx.close(); 
			} 
			catch( NamingException e ) { 
				auth.engine.log( "find LDAP users" , e );
			} 
		}
		
		return( Common.getSortedList( users ) );
	}

}
