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

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.action.ActionInit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AuthLdap {

	EngineAuth auth;
	
	public boolean ldapUse;
	public String ldapHost;
	public int ldapPort;
	public String ldapUserRes;
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
	
	public AuthLdap( EngineAuth auth ) {
		this.auth = auth;
		ldapStarted = false;
		setNotUse();
	}

	public void setNotUse() {
		ldapUse = false;
		ldapPort = 0;
		ldapHost = "";
		ldapUserRes = "";
		userDN = "";
		userClass = "";
		userFilter = "";
		userNameAttr = "";
		userDisplayNameAttr = "";
		userEmailAttr = "";
		userPasswordAttr = "";
	}

	public void setServer( String host , int port , String res ) {
		ldapUse = true;
		ldapPort = port;
		ldapHost = host;
		ldapUserRes = res;
	}

	public void setUserData( String userDN , String userClass , String userFilter , String userNameAttr , String userDisplayNameAttr , String userEmailAttr , String userPasswordAttr ) {
		this.userDN = userDN;
		this.userClass = userClass;
		this.userFilter = userFilter;
		this.userNameAttr = userNameAttr;
		this.userDisplayNameAttr = userDisplayNameAttr;
		this.userEmailAttr = userEmailAttr;
		this.userPasswordAttr = userPasswordAttr;
	}
	
	public void load( Node root ) throws Exception {
		ldapUse = ConfReader.getBooleanAttrValue( root , "useldap" , false );
		if( ldapUse ) {
			ldapHost = ConfReader.getAttrValue( root , "host" );
			ldapPort = ConfReader.getIntegerAttrValue( root , "port" , 0 );
			ldapUserRes = ConfReader.getAttrValue( root , "resource" );
			userDN = ConfReader.getAttrValue( root , "userdn" );
			userClass = ConfReader.getAttrValue( root , "userclass" );
			userFilter = ConfReader.getAttrValue( root , "userfilter" );
			userNameAttr = ConfReader.getAttrValue( root , "nameattr" );
			userDisplayNameAttr = ConfReader.getAttrValue( root , "displaynameattr" );
			userEmailAttr = ConfReader.getAttrValue( root , "emailattr" );
			userPasswordAttr = ConfReader.getAttrValue( root , "passwordattr" );
		}
	}
	
	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "useldap" , Common.getBooleanValue( ldapUse ) );
		if( ldapUse ) {
			Common.xmlSetElementAttr( doc , root , "host" , ldapHost );
			Common.xmlSetElementAttr( doc , root , "port" , "" + ldapPort );
			Common.xmlSetElementAttr( doc , root , "resource" , ldapUserRes );
			Common.xmlSetElementAttr( doc , root , "userdn" , userDN );
			Common.xmlSetElementAttr( doc , root , "userclass" , userClass );
			Common.xmlSetElementAttr( doc , root , "userfilter" , userFilter );
			Common.xmlSetElementAttr( doc , root , "nameattr" , userNameAttr );
			Common.xmlSetElementAttr( doc , root , "displaynameattr" , userDisplayNameAttr );
			Common.xmlSetElementAttr( doc , root , "emailattr" , userEmailAttr );
			Common.xmlSetElementAttr( doc , root , "passwordattr" , userPasswordAttr );
		}
	}

	public void create() throws Exception {
		if( !ldapUse )
			return;
		
		// The LDAP server's address
		if( ldapHost.isEmpty() )
			return;
		
		this.PROVIDER_URL = "ldap://" + ldapHost + ":";
		if( ldapPort > 0 )
			this.PROVIDER_URL += ldapPort;
		else
			this.PROVIDER_URL += "389";
		
		if( !userDN.isEmpty() )
			this.PROVIDER_URL += "/" + userDN;
		
		// Filter to find user's record; note that {} is replaced by the user-supplied ID
		this.USER_FILTER = getUserFilter( false , userFilter );
			
		// Specify we want to use the LDAP protocol for this directory
		LDAP_ENV.put( Context.INITIAL_CONTEXT_FACTORY , INITIAL_CONTEXT_FACTORY );
		// URL to the LDAP server
		LDAP_ENV.put( Context.PROVIDER_URL , PROVIDER_URL );
		// "simple" if a read-only search account is required; "none" if anonymous
		LDAP_ENV.put( Context.SECURITY_AUTHENTICATION , "simple" );
		// The read-only account's DN, if not anonymous.  e.g. "uid=admin,ou=system"
		if( ldapUserRes.isEmpty() )
			return;
		
		AuthResource res = auth.getResource( ldapUserRes );
		res.loadAuthData();
		if( !res.ac.isCommon() )
			return;
		
		LDAP_ENV.put( Context.SECURITY_PRINCIPAL , res.ac.USER );
		// The read-only account's password, if not anonymous
		LDAP_ENV.put( Context.SECURITY_CREDENTIALS , res.ac.PASSWORDSAVE ); 
		// Search filters will access their context's entire subtree...
		SEARCH_CONTROLS.setSearchScope( SearchControls.SUBTREE_SCOPE );
	}

	public void start( ActionInit action ) throws Exception {
		create();
		ldapStarted = true;
	}
	
	public void stop( ActionInit action ) throws Exception {
		ldapStarted = false;
	}
	
	private String getUserFilter( boolean allUsers , String userFilter ) {
		String filter = userFilter;
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
	
	public AuthUser getLdapUserData( ActionBase action , String username ) throws Exception {
		if( !ldapUse )
			return( null );
		
		if( !ldapStarted )
			start( action.actionInit );
		
		SearchResult res = findUser( username );
		AuthUser user = new AuthUser( auth );
		user.create( action , false , username , getAttr( res , userEmailAttr ) , getAttr( res , userDisplayNameAttr ) , false );
		return( user );
	}

	public String[] getUserList() throws Exception {
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
				auth.engine.serverAction.log( "find LDAP users" , e );
			} 
		}
		
		return( Common.getSortedList( users ) );
	}

}
