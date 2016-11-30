package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.meta.engine.ServerAuth;
import org.urm.meta.engine.ServerAuthContext;
import org.urm.meta.engine.ServerAuthGroup;
import org.urm.meta.engine.ServerAuthRoleSet;
import org.urm.meta.engine.ServerAuthUser;

public class SessionSecurity {

	ServerAuth auth;
	
	private ServerAuthUser user;
	private ServerAuthContext ac;

	ServerAuthRoleSet secBase;
	ServerAuthRoleSet secProductAny;
	ServerAuthRoleSet secNetworkAny;
	Map<String,ServerAuthRoleSet> secProduct;
	Map<String,ServerAuthRoleSet> secNetwork;
	
	public SessionSecurity( ServerAuth auth ) {
		this.auth = auth;
		secBase = new ServerAuthRoleSet();
		secProductAny = new ServerAuthRoleSet();
		secNetworkAny = new ServerAuthRoleSet();
		secProduct = new HashMap<String,ServerAuthRoleSet>();
		secNetwork = new HashMap<String,ServerAuthRoleSet>();
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
		secBase.clear();
		secProductAny.clear();
		secNetworkAny.clear();
		secProduct.clear();
		secNetwork.clear();
		
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
			}
		}
	}

	public synchronized ServerAuthRoleSet getBaseRoles() {
		return( secBase );
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
	
}
