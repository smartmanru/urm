package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.meta.engine.ServerAuth;
import org.urm.meta.engine.ServerAuth.SpecialRights;
import org.urm.meta.engine.ServerAuthContext;
import org.urm.meta.engine.ServerAuthGroup;
import org.urm.meta.engine.ServerAuthRoleSet;
import org.urm.meta.engine.ServerAuthUser;

public class SessionSecurity {

	ServerAuth auth;
	
	private boolean server;
	private ServerAuthUser user;
	private ServerAuthContext ac;

	ServerAuthRoleSet secBase;
	ServerAuthRoleSet secProductAny;
	ServerAuthRoleSet secNetworkAny;
	Map<String,ServerAuthRoleSet> secProduct;
	Map<String,ServerAuthRoleSet> secNetwork;
	Map<SpecialRights,Integer> secSpecial;
	
	public SessionSecurity( ServerAuth auth ) {
		this.auth = auth;
		server = false;
		secBase = new ServerAuthRoleSet();
		secProductAny = new ServerAuthRoleSet();
		secNetworkAny = new ServerAuthRoleSet();
		secProduct = new HashMap<String,ServerAuthRoleSet>();
		secNetwork = new HashMap<String,ServerAuthRoleSet>();
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
	
	public ServerAuthUser getUser() {
		return( user );
	}

	public void setUser( ServerAuthUser user ) {
		this.user = user;
	}
	
	public ServerAuthContext getContext() {
		return( ac );
	}

	public void setContext( ServerAuthContext ac ) {
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
		
		for( ServerAuthGroup group : auth.getUserGroups( user ) ) {
			if( group.hasUser( user ) ) {
				secBase.add( group.roles );
				
				if( group.anyProducts )
					secProductAny.add( group.roles );
				else {
					for( String product : group.getPermissionProducts() ) {
						ServerAuthRoleSet roles = secProduct.get( product );
						if( roles == null ) {
							roles = new ServerAuthRoleSet( group.roles );
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
						ServerAuthRoleSet roles = secNetwork.get( network );
						if( roles == null ) {
							roles = new ServerAuthRoleSet( group.roles );
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

	public synchronized ServerAuthRoleSet getBaseRoles() {
		return( new ServerAuthRoleSet( secBase ) );
	}

	public synchronized ServerAuthRoleSet getProductRoles( String productName ) {
		ServerAuthRoleSet set = new ServerAuthRoleSet( secProductAny );
		ServerAuthRoleSet roles = secProduct.get( productName );
		if( roles != null )
			set.add( roles );
		return( set );
	}
	
	public synchronized ServerAuthRoleSet getNetworkRoles( String networkName ) {
		ServerAuthRoleSet set = new ServerAuthRoleSet( secNetworkAny );
		ServerAuthRoleSet roles = secNetwork.get( networkName );
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
