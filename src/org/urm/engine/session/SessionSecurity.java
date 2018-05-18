package org.urm.engine.session;

import java.util.HashMap;
import java.util.Map;

import org.urm.engine.AuthService;
import org.urm.engine.AuthService.SpecialRights;
import org.urm.engine.security.AuthContext;
import org.urm.engine.security.AuthGroup;
import org.urm.engine.security.AuthProductSecurity;
import org.urm.engine.security.AuthRoleSet;
import org.urm.engine.security.AuthUser;
import org.urm.meta.env.MetaEnv;

public class SessionSecurity {

	AuthService auth;
	
	private boolean server;
	private AuthUser user;
	private AuthContext ac;

	AuthRoleSet secBase;
	AuthRoleSet secResourceAny;
	AuthRoleSet secNetworkAny;
	AuthProductSecurity secProductAny;
	Map<Integer,AuthRoleSet> secResource;
	Map<Integer,AuthRoleSet> secNetwork;
	Map<Integer,AuthProductSecurity> secProduct;
	Map<SpecialRights,Integer> secSpecial;
	
	public SessionSecurity( AuthService auth ) {
		this.auth = auth;
		server = false;
		secBase = new AuthRoleSet();
		secResourceAny = new AuthRoleSet();
		secNetworkAny = new AuthRoleSet();
		secProductAny = new AuthProductSecurity();
		secResource = new HashMap<Integer,AuthRoleSet>();
		secNetwork = new HashMap<Integer,AuthRoleSet>();
		secProduct = new HashMap<Integer,AuthProductSecurity>();
		secSpecial = new HashMap<SpecialRights,Integer>(); 
	}

	public AuthRoleSet getAnyResourceRoles() {
		return( secResourceAny );
	}
	
	public AuthRoleSet getAnyNetworkRoles() {
		return( secNetworkAny );
	}
	
	public AuthRoleSet getAnyProductRoles() {
		return( secProductAny.roles );
	}
	
	public boolean getAnyProductSecured() {
		return( secProductAny.specialSecured );
	}
	
	public boolean isCurrentPassword( String password ) {
		if( password.equals( ac.PASSWORDONLINE ) )
			return( true );
		return( false );
	}
	
	public boolean isAdminAny() {
		if( server )
			return( true );
		if( user != null && user.ADMIN )
			return( true );
		return( false );
	}
	
	public boolean isAdminCore() {
		if( isAdminMaster() )
			return( false );
		
		if( isAdminAny() )
			return( true );
		if( checkSpecial( SpecialRights.SPECIAL_ADMCORE ) )
			return( true );
		return( false );
	}

	public boolean isAdminMaster() {
		return( user.isMaster() );
	}
	
	public void setServer() {
		server = true;
	}
	
	public AuthUser getUser() {
		return( user );
	}

	public void setUser( AuthUser user ) {
		this.user = user;
	}
	
	public AuthContext getContext() {
		return( ac );
	}

	public void setContext( AuthContext ac ) {
		this.ac = ac;
	}

	public synchronized void setPermissions() throws Exception {
		if( user == null )
			return;
		
		secBase.clear();
		secResourceAny.clear();
		secProductAny.clear();
		secNetworkAny.clear();
		secResource.clear();
		secProduct.clear();
		secNetwork.clear();
		secSpecial.clear();
		
		for( AuthGroup group : auth.getUserGroups( user ) ) {
			if( group.hasUser( user ) ) {
				secBase.add( group.roles );
				
				if( group.anyResources )
					secResourceAny.add( group.roles );
				else {
					for( int resourceId : group.getPermissionResources() ) {
						AuthRoleSet roles = secResource.get( resourceId );
						if( roles == null ) {
							roles = new AuthRoleSet( group.roles );
							secResource.put( resourceId , roles );
						}
						else
							roles.add( group.roles );
					}
				}
				
				if( group.anyProducts )
					secProductAny.addRoles( group.roles );
				else {
					for( int productId : group.getPermissionProducts() ) {
						AuthProductSecurity ps = secProduct.get( productId );
						if( ps == null ) {
							ps = new AuthProductSecurity();
							secProduct.put( productId , ps );
						}
						
						ps.addRoles( group.roles );
					}
				}
				
				if( group.anyNetworks )
					secNetworkAny.add( group.roles );
				else {
					for( Integer networkId : group.getPermissionNetworks() ) {
						AuthRoleSet roles = secNetwork.get( networkId );
						if( roles == null ) {
							roles = new AuthRoleSet( group.roles );
							secNetwork.put( networkId , roles );
						}
						else
							roles.add( group.roles );
					}
				}
				
				for( SpecialRights sr : group.getPermissionSpecial() ) {
					secSpecial.put( sr , 0 );
					if( group.anyProducts )
						secProductAny.addSpecial( sr );
					else {
						for( int productId : group.getPermissionProducts() ) {
							AuthProductSecurity ps = secProduct.get( productId );
							ps.addSpecial( sr );
						}
					}
				}
			}
		}
	}

	public synchronized AuthRoleSet getBaseRoles() {
		return( new AuthRoleSet( secBase ) );
	}

	public synchronized AuthRoleSet getResourceRoles( int resourceId ) {
		AuthRoleSet set = new AuthRoleSet( secResourceAny );
		AuthRoleSet roles = secResource.get( resourceId );
		if( roles != null )
			set.add( roles );
		return( set );
	}
	
	public synchronized AuthRoleSet getNetworkRoles( int networkId ) {
		AuthRoleSet set = new AuthRoleSet( secNetworkAny );
		AuthRoleSet roles = secNetwork.get( networkId );
		if( roles != null )
			set.add( roles );
		return( set );
	}

	public synchronized AuthRoleSet getProductRoles( int productId ) {
		AuthRoleSet set = new AuthRoleSet( secProductAny.roles );
		AuthProductSecurity ps = secProduct.get( productId );
		if( ps != null )
			set.add( ps.roles );
		return( set );
	}
	
	public synchronized boolean checkSpecial( SpecialRights sr ) {
		if( secSpecial.containsKey( sr ) )
			return( true );
		return( false );
	}

	public synchronized boolean checkEngineSecured() {
		if( user.ADMIN )
			return( true );
		return( false );
	}
	
	public synchronized boolean checkProductSecured( int productId ) {
		if( secProductAny.specialSecured )
			return( true );
		AuthProductSecurity ps = secProduct.get( productId );
		if( ps != null )
			return( ps.specialSecured );
		return( false );
	}

	public synchronized boolean checkEnvSecured( MetaEnv env ) {
		if( !checkProductSecured( env.meta.ep.productId ) )
			return( false );

		AuthRoleSet roles = getProductRoles( env.meta.ep.productId );
		if( env.isProd() && roles.secOpr )
			return( true );

		if( env.isUAT() && ( roles.secRel || roles.secTest ) )
			return( true );
		
		if( env.isDEV() && ( roles.secDev || roles.secTest ) )
			return( true );
		
		return( false );
	}

	public Integer[] getResources() {
		return( secResource.keySet().toArray( new Integer[0] ) );
	}
	
	public Integer[] getProducts() {
		return( secProduct.keySet().toArray( new Integer[0] ) );
	}
	
	public Integer[] getNetworks() {
		return( secNetwork.keySet().toArray( new Integer[0] ) );
	}
	
	public SpecialRights[] getSpecialRights() {
		return( secSpecial.keySet().toArray( new SpecialRights[0] ) );
	}
	
}
