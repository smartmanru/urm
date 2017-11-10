package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.AuthContext;
import org.urm.meta.engine.AuthGroup;
import org.urm.meta.engine.AuthUser;
import org.urm.meta.engine.AuthRoleSet;
import org.urm.meta.engine.EngineAuth.SpecialRights;

public class SessionSecurity {

	EngineAuth auth;
	
	private boolean server;
	private AuthUser user;
	private AuthContext ac;

	AuthRoleSet secBase;
	AuthRoleSet secProductAny;
	AuthRoleSet secNetworkAny;
	Map<String,AuthRoleSet> secProduct;
	Map<String,AuthRoleSet> secNetwork;
	Map<SpecialRights,Integer> secSpecial;
	
	public SessionSecurity( EngineAuth auth ) {
		this.auth = auth;
		server = false;
		secBase = new AuthRoleSet();
		secProductAny = new AuthRoleSet();
		secNetworkAny = new AuthRoleSet();
		secProduct = new HashMap<String,AuthRoleSet>();
		secNetwork = new HashMap<String,AuthRoleSet>();
		secSpecial = new HashMap<SpecialRights,Integer>(); 
	}

	public boolean isAdmin() {
		if( server )
			return( true );
		if( user != null && user.ADMIN )
			return( true );
		return( false );
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
		secProductAny.clear();
		secNetworkAny.clear();
		secProduct.clear();
		secNetwork.clear();
		secSpecial.clear();
		
		for( AuthGroup group : auth.getUserGroups( user ) ) {
			if( group.hasUser( user ) ) {
				secBase.add( group.roles );
				
				if( group.anyProducts )
					secProductAny.add( group.roles );
				else {
					for( String product : group.getPermissionProducts() ) {
						AuthRoleSet roles = secProduct.get( product );
						if( roles == null ) {
							roles = new AuthRoleSet( group.roles );
							secProduct.put( product , roles );
						}
						else
							roles.add( group.roles );
					}
				}
				
				if( group.anyNetworks )
					secNetworkAny.add( group.roles );
				else {
					for( String network : group.getPermissionNetworks() ) {
						AuthRoleSet roles = secNetwork.get( network );
						if( roles == null ) {
							roles = new AuthRoleSet( group.roles );
							secNetwork.put( network , roles );
						}
						else
							roles.add( group.roles );
					}
				}
				
				for( SpecialRights sr : group.getPermissionSpecial() )
					secSpecial.put( sr , 0 );
			}
		}
	}

	public synchronized AuthRoleSet getBaseRoles() {
		return( new AuthRoleSet( secBase ) );
	}

	public synchronized AuthRoleSet getProductRoles( String productName ) {
		AuthRoleSet set = new AuthRoleSet( secProductAny );
		AuthRoleSet roles = secProduct.get( productName );
		if( roles != null )
			set.add( roles );
		return( set );
	}
	
	public synchronized AuthRoleSet getNetworkRoles( String networkName ) {
		AuthRoleSet set = new AuthRoleSet( secNetworkAny );
		AuthRoleSet roles = secNetwork.get( networkName );
		if( roles != null )
			set.add( roles );
		return( set );
	}

	public synchronized boolean checkSpecial( SpecialRights sr ) {
		if( secSpecial.containsKey( sr ) )
			return( true );
		return( false );
	}
	
}
