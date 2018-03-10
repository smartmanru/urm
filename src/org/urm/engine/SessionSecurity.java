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
	AuthRoleSet secResourceAny;
	AuthRoleSet secProductAny;
	AuthRoleSet secNetworkAny;
	Map<Integer,AuthRoleSet> secResource;
	Map<Integer,AuthRoleSet> secProduct;
	Map<Integer,AuthRoleSet> secNetwork;
	Map<SpecialRights,Integer> secSpecial;
	
	public SessionSecurity( EngineAuth auth ) {
		this.auth = auth;
		server = false;
		secBase = new AuthRoleSet();
		secResourceAny = new AuthRoleSet();
		secProductAny = new AuthRoleSet();
		secNetworkAny = new AuthRoleSet();
		secResource = new HashMap<Integer,AuthRoleSet>();
		secProduct = new HashMap<Integer,AuthRoleSet>();
		secNetwork = new HashMap<Integer,AuthRoleSet>();
		secSpecial = new HashMap<SpecialRights,Integer>(); 
	}

	public boolean isAdminAny() {
		if( server )
			return( true );
		if( user != null && user.ADMIN )
			return( true );
		return( false );
	}
	
	public boolean isAdminCore() {
		if( isAdminAny() )
			return( true );
		if( checkSpecial( SpecialRights.SPECIAL_ADMCORE ) )
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
					secProductAny.add( group.roles );
				else {
					for( int productId : group.getPermissionProducts() ) {
						AuthRoleSet roles = secProduct.get( productId );
						if( roles == null ) {
							roles = new AuthRoleSet( group.roles );
							secProduct.put( productId , roles );
						}
						else
							roles.add( group.roles );
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
				
				for( SpecialRights sr : group.getPermissionSpecial() )
					secSpecial.put( sr , 0 );
			}
		}
	}

	public synchronized AuthRoleSet getBaseRoles() {
		return( new AuthRoleSet( secBase ) );
	}

	public synchronized AuthRoleSet getResourceRoles( int resourceId ) {
		AuthRoleSet set = new AuthRoleSet( secResourceAny );
		AuthRoleSet roles = secProduct.get( resourceId );
		if( roles != null )
			set.add( roles );
		return( set );
	}
	
	public synchronized AuthRoleSet getProductRoles( int productId ) {
		AuthRoleSet set = new AuthRoleSet( secProductAny );
		AuthRoleSet roles = secProduct.get( productId );
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

	public synchronized boolean checkSpecial( SpecialRights sr ) {
		if( secSpecial.containsKey( sr ) )
			return( true );
		return( false );
	}
	
}
