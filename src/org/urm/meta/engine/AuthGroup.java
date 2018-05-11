package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.engine.EngineAuth.SourceType;
import org.urm.meta.engine.EngineAuth.SpecialRights;

public class AuthGroup {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_ANY_RESOURCES = "any_resources";
	public static String PROPERTY_ANY_PRODUCTS = "any_products";
	public static String PROPERTY_ANY_NETWORKS = "any_networks";
	public static String PROPERTY_ROLEDEV = "roledev";
	public static String PROPERTY_ROLEREL = "rolerel";
	public static String PROPERTY_ROLETEST = "roletest";
	public static String PROPERTY_ROLEOPR = "roleopr";
	public static String PROPERTY_ROLEINFRA = "roleinfra";
	public static String PROPERTY_SPECIAL_ADMCORE = "specialrights_admcore";
	public static String PROPERTY_SPECIAL_BASEADM = "specialrights_baseadm";
	public static String PROPERTY_SPECIAL_BASEITEMS = "specialrights_baseitems";
	
	EngineAuth auth;
	
	public int ID;
	public String NAME;
	public String DESC;
	Map<Integer,SourceType> users;
	
	// permissions
	public AuthRoleSet roles;
	public boolean anyResources;
	public boolean anyProducts;
	public boolean anyNetworks;
	private List<Integer> resources;
	private List<Integer> products;
	private List<Integer> networks;
	private List<SpecialRights> specials;
	public int UV;
	
	public AuthGroup( EngineAuth auth ) {
		this.auth = auth;
		users = new HashMap<Integer,SourceType>();
		
		roles = new AuthRoleSet();
		resources = new LinkedList<Integer>();
		products = new LinkedList<Integer>();
		networks = new LinkedList<Integer>();
		specials = new LinkedList<SpecialRights>();
	}

	public void createGroup( String name , String desc ) {
		modifyGroup( name , desc );
	}
	
	public void modifyGroup( String name , String desc ) {
		this.NAME = name;
		this.DESC = Common.nonull( desc );
	}
	
	public boolean hasResource( Integer resource ) {
		if( resources.contains( resource ) )
			return( true );
		return( false );
	}
	
	public void removeResource( Integer resource ) {
		resources.remove( resource );
	}
	
	public boolean hasProduct( Integer product ) {
		if( products.contains( product ) )
			return( true );
		return( false );
	}
	
	public void removeProduct( Integer product ) {
		products.remove( product );
	}
	
	public boolean hasNetwork( Integer network ) {
		if( networks.contains( network ) )
			return( true );
		return( false );
	}
	
	public void removeNetwork( Integer network ) {
		networks.remove( network );
	}
	
	public void addLocalUser( Integer user ) {
		if( !users.containsKey( user ) )
			users.put( user , SourceType.SOURCE_LOCAL );
	}
	
	public void addLdapUser( Integer user ) {
		if( !users.containsKey( user ) )
			users.put( user , SourceType.SOURCE_LDAP );
	}

	public void addResource( Integer resource ) {
		if( !resources.contains( resource ) )
			resources.add( resource );
	}
	
	public void addProduct( Integer product ) {
		if( !products.contains( product ) )
			products.add( product );
	}
	
	public void addNetwork( Integer network ) {
		if( !networks.contains( network ) )
			networks.add( network );
	}
	
	public void removeUser( AuthUser user ) {
		users.remove( user.ID );
	}

	public String[] getUsers( SourceType type ) {
		Map<String,Integer> map = new HashMap<String,Integer>();
		for( Integer userId : users.keySet() ) {
			SourceType userType = users.get( userId );
			if( type == null || type == userType ) {
				AuthUser user = null;
				if( userType == SourceType.SOURCE_LOCAL )
					user = auth.findLocalUser( userId );
				else
					user = auth.findLdapUser( userId );
				if( user != null )
					map.put( user.NAME , userId );
			}
		}
		return( Common.getSortedKeys( map ) );
	}
	
	public boolean hasUser( AuthUser user ) {
		if( users.containsKey( user.ID ) )
			return( true );
		return( false );
	}

	public boolean hasUser( String name ) {
		AuthUser user = auth.findUser( name );
		if( user == null )
			return( false );
		
		if( users.containsKey( user.ID ) )
			return( true );
		return( false );
	}

	public Integer[] getPermissionResources() {
		return( resources.toArray( new Integer[0] ) );
	}
	
	public Integer[] getPermissionProducts() {
		return( products.toArray( new Integer[0] ) );
	}
	
	public Integer[] getPermissionNetworks() {
		return( networks.toArray( new Integer[0] ) );
	}

	public SpecialRights[] getPermissionSpecial() {
		return( specials.toArray( new SpecialRights[0] ) );
	}

	public boolean checkSpecialPermission( SpecialRights check ) {
		for( SpecialRights r : specials ) {
			if( r == check )
				return( true );
		}
		return( false );
	}
	
	public boolean isPermissionSpecialAll() {
		for( SpecialRights r : SpecialRights.values() ) {
			boolean check = false;
			for( SpecialRights p : specials ) {
				if( p == r ) {
					check = true;
					break;
				}
			}
			
			if( !check )
				return( false );
		}
		return( true );
	}
	
	public void addUser( SourceType source , AuthUser user ) {
		if( !users.containsKey( user.ID ) )
			users.put( user.ID , source );
	}

	public void removeUser( Integer user ) {
		users.remove( user );
	}

	public SourceType getUserSource( int user ) {
		return( users.get( user ) );
	}

	public void setGroupPermissions( AuthRoleSet roles , boolean allResources , Integer[] resourceList , boolean allProd , Integer[] productList , boolean allNet , Integer[] networkList , boolean allSpecial , SpecialRights[] specialList ) throws Exception {
		this.roles.set( roles );
		this.anyResources = allResources;
		
		resources.clear();
		this.anyResources = allResources;
		if( anyResources == false && resourceList != null ) {
			for( Integer resourceId : resourceList )
				resources.add( resourceId );
		}
		
		this.anyProducts = allProd;
		if( anyProducts == false && productList != null ) {
			for( Integer productId : productList )
				products.add( productId );
		}
		
		this.anyNetworks = allNet;
		if( anyNetworks == false && networkList != null ) {
			for( Integer networkId : networkList )
				networks.add( networkId );
		}

		specials.clear();
		if( allSpecial ) {
			for( SpecialRights specialId : SpecialRights.values() )
				specials.add( specialId );
		}
		else {
			if( specialList != null ) {
				for( SpecialRights specialId : specialList )
					specials.add( specialId );
			}
		}
	}
	
}
