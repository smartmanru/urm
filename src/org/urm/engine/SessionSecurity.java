package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.EngineAuthContext;
import org.urm.meta.engine.EngineAuthGroup;
import org.urm.meta.engine.EngineAuthUser;
import org.urm.meta.engine.EngineAuthRoleSet;
import org.urm.meta.engine.EngineAuth.SpecialRights;

public class SessionSecurity {

	EngineAuth auth;
	
	private boolean server;
	private EngineAuthUser user;
	private EngineAuthContext ac;

	EngineAuthRoleSet secBase;
	EngineAuthRoleSet secProductAny;
	EngineAuthRoleSet secNetworkAny;
	Map<String,EngineAuthRoleSet> secProduct;
	Map<String,EngineAuthRoleSet> secNetwork;
	Map<SpecialRights,Integer> secSpecial;
	
	public SessionSecurity( EngineAuth auth ) {
		this.auth = auth;
		server = false;
		secBase = new EngineAuthRoleSet();
		secProductAny = new EngineAuthRoleSet();
		secNetworkAny = new EngineAuthRoleSet();
		secProduct = new HashMap<String,EngineAuthRoleSet>();
		secNetwork = new HashMap<String,EngineAuthRoleSet>();
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
	
	public EngineAuthUser getUser() {
		return( user );
	}

	public void setUser( EngineAuthUser user ) {
		this.user = user;
	}
	
	public EngineAuthContext getContext() {
		return( ac );
	}

	public void setContext( EngineAuthContext ac ) {
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
		
		for( EngineAuthGroup group : auth.getUserGroups( user ) ) {
			if( group.hasUser( user ) ) {
				secBase.add( group.roles );
				
				if( group.anyProducts )
					secProductAny.add( group.roles );
				else {
					for( String product : group.getPermissionProducts() ) {
						EngineAuthRoleSet roles = secProduct.get( product );
						if( roles == null ) {
							roles = new EngineAuthRoleSet( group.roles );
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
						EngineAuthRoleSet roles = secNetwork.get( network );
						if( roles == null ) {
							roles = new EngineAuthRoleSet( group.roles );
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

	public synchronized EngineAuthRoleSet getBaseRoles() {
		return( new EngineAuthRoleSet( secBase ) );
	}

	public synchronized EngineAuthRoleSet getProductRoles( String productName ) {
		EngineAuthRoleSet set = new EngineAuthRoleSet( secProductAny );
		EngineAuthRoleSet roles = secProduct.get( productName );
		if( roles != null )
			set.add( roles );
		return( set );
	}
	
	public synchronized EngineAuthRoleSet getNetworkRoles( String networkName ) {
		EngineAuthRoleSet set = new EngineAuthRoleSet( secNetworkAny );
		EngineAuthRoleSet roles = secNetwork.get( networkName );
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
