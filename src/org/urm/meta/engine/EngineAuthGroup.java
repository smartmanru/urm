package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.engine.EngineAuth.SourceType;
import org.urm.meta.engine.EngineAuth.SpecialRights;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineAuthGroup {

	EngineAuth auth;
	
	public String NAME;
	Map<String,SourceType> users;
	
	// permissions
	public EngineAuthRoleSet roles;
	public boolean anyProducts;
	public boolean anyNetworks;
	public Map<String,Integer> products;
	public Map<String,Integer> networks;
	public SpecialRights[] special;
	
	public EngineAuthGroup( EngineAuth auth ) {
		this.auth = auth;
		users = new HashMap<String,SourceType>();
		
		roles = new EngineAuthRoleSet();
		products = new HashMap<String,Integer>(); 
		networks = new HashMap<String,Integer>();
		special = new SpecialRights[0];
	}

	public void create( ActionBase action , String name ) throws Exception {
		this.NAME = name;
	}
	
	public void rename( ActionBase action , String name ) throws Exception {
		this.NAME = name;
	}
	
	public boolean hasProduct( String product ) {
		if( products.containsKey( product ) )
			return( true );
		return( false );
	}
	
	public void removeProduct( ActionBase action , String product ) {
		products.remove( product );
	}
	
	public void removeNetwork( ActionBase action , String network ) {
		networks.remove( network );
	}
	
	public boolean hasNetwork( String network ) {
		if( networks.containsKey( network ) )
			return( true );
		return( false );
	}
	
	public void loadGroup( Node root ) throws Exception {
		NAME = ConfReader.getAttrValue( root , "name" );
		loadLocalUsers( root );
		loadLdapUsers( root );
		loadPermissions( root );
	}
	
	private void loadLocalUsers( Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , "localuser" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			String user = ConfReader.getAttrValue( node , "name" );
			addLocalUser( user );
		}
	}

	private void loadLdapUsers( Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , "ldapuser" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			String user = ConfReader.getAttrValue( node , "name" );
			addLdapUser( user );
		}
	}

	private void addLocalUser( String user ) {
		if( users.get( user ) == null )
			users.put( user , SourceType.SOURCE_LOCAL );
	}
	
	private void addLdapUser( String user ) {
		if( users.get( user ) == null )
			users.put( user , SourceType.SOURCE_LDAP );
	}

	private void loadPermissions( Node root ) throws Exception {
		Node pm = ConfReader.xmlGetFirstChild( root , "permissions" );
		if( pm == null )
			return;
		
		roles.loadPermissions( pm );
		loadProductPermissions( pm );
		loadNetworkPermissions( pm );
		loadSpecialPermissions( pm );
	}

	private void loadProductPermissions( Node root ) throws Exception {
		anyProducts = ConfReader.getBooleanAttrValue( root , "anyproduct" , false );
		Node[] list = ConfReader.xmlGetChildren( root , "product" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			String product = ConfReader.getAttrValue( node , "name" );
			addProduct( product );
		}
	}
	
	private void loadNetworkPermissions( Node root ) throws Exception {
		anyNetworks = ConfReader.getBooleanAttrValue( root , "anynetwork" , false );
		Node[] list = ConfReader.xmlGetChildren( root , "network" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			String network = ConfReader.getAttrValue( node , "name" );
			addNetwork( network );
		}
	}
	
	private void loadSpecialPermissions( Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , "special" );
		if( list == null )
			return;
		
		List<SpecialRights> rights = new LinkedList<SpecialRights>();
		for( Node node : list ) {
			String specialName = ConfReader.getAttrValue( node , "name" );
			for( SpecialRights sr : SpecialRights.values() ) {
				if( specialName.equals( Common.getEnumLower( sr ) ) ) {
					rights.add( sr );
					break;
				}
			}
		}
		special = rights.toArray( new SpecialRights[0] );
	}
	
	private void addProduct( String product ) {
		products.put( product , 0 );
	}
	
	private void addNetwork( String network ) {
		networks.put( network , 0 );
	}
	
	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , NAME );
		for( String user : Common.getSortedKeys( users ) ) {
			SourceType type = users.get( user );
			Element item = null;
			if( type == SourceType.SOURCE_LOCAL )
				item = Common.xmlCreateElement( doc , root , "localuser" );
			else
			if( type == SourceType.SOURCE_LDAP )
				item = Common.xmlCreateElement( doc , root , "ldapuser" );
			Common.xmlSetElementAttr( doc , item , "name" , user );
		}
		
		Element permissions = Common.xmlCreateElement( doc , root , "permissions" );
		savePermissions( doc , permissions );
	}

	public void savePermissions( Document doc , Element root ) throws Exception {
		roles.savePermissions( doc , root );
		
		Common.xmlSetElementAttr( doc , root , "anyproduct" , Common.getBooleanValue( anyProducts ) );
		for( String product : Common.getSortedKeys( products ) ) {
			Element item = Common.xmlCreateElement( doc , root , "product" );
			Common.xmlSetElementAttr( doc , item , "name" , product );
		}
		
		Common.xmlSetElementAttr( doc , root , "anynetwork" , Common.getBooleanValue( anyNetworks ) );
		for( String network : Common.getSortedKeys( networks ) ) {
			Element item = Common.xmlCreateElement( doc , root , "network" );
			Common.xmlSetElementAttr( doc , item , "name" , network );
		}
		
		for( SpecialRights checkRight : special ) {
			Element item = Common.xmlCreateElement( doc , root , "special" );
			Common.xmlSetElementAttr( doc , item , "name" , Common.getEnumLower( checkRight ) );
		}
	}
	
	public void deleteUser( ActionBase action , EngineAuthUser user ) throws Exception {
		users.remove( user.NAME );
	}

	public String[] getUsers( SourceType type ) {
		List<String> list = new LinkedList<String>();
		for( String user : Common.getSortedKeys( users ) ) {
			SourceType userType = users.get( user );
			if( type == null || type == userType )
				list.add( user );
		}
		return( list.toArray( new String[0] ) );
	}
	
	public boolean hasUser( EngineAuthUser user ) {
		if( users.containsKey( user.NAME ) )
			return( true );
		return( false );
	}

	public String[] getPermissionProducts() {
		return( Common.getSortedKeys( products ) );
	}
	
	public String[] getPermissionNetworks() {
		return( Common.getSortedKeys( networks ) );
	}

	public SpecialRights[] getPermissionSpecial() {
		return( special );
	}
	
	public boolean isPermissionSpecialAll() {
		for( SpecialRights r : SpecialRights.values() ) {
			boolean check = false;
			for( SpecialRights p : special ) {
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
	
	public void addUser( ActionBase action , SourceType source , EngineAuthUser user ) throws Exception {
		if( !users.containsKey( user.NAME ) )
			users.put( user.NAME , source );
	}

	public void removeUser( ActionBase action , String user ) throws Exception {
		users.remove( user );
	}

	public SourceType getUserSource( String user ) {
		return( users.get( user ) );
	}

	public void setGroupPermissions( ActionBase action , EngineAuthRoleSet roles , boolean allProd , String[] products , boolean allNet , String[] networks , SpecialRights[] special ) throws Exception {
		this.roles.set( roles );
		this.anyProducts = allProd;
		for( String product : products )
			addProduct( product );
		this.anyNetworks = allNet;
		for( String network : networks )
			addNetwork( network );
		this.special = special;
	}
	
}
