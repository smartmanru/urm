package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.engine.ServerAuth.SourceType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerAuthGroup {

	ServerAuth auth;
	
	public String NAME;
	Map<String,SourceType> users;
	
	// permissions
	public ServerAuthRoleSet roles;
	public boolean anyProducts;
	public boolean anyNetworks;
	public Map<String,Integer> products;
	public Map<String,Integer> networks;
	
	public ServerAuthGroup( ServerAuth auth ) {
		this.auth = auth;
		users = new HashMap<String,SourceType>();
		
		roles = new ServerAuthRoleSet();
		products = new HashMap<String,Integer>(); 
		networks = new HashMap<String,Integer>(); 
	}

	public void create( ActionBase action , String name ) throws Exception {
		this.NAME = name;
	}
	
	public void rename( ActionBase action , String name ) throws Exception {
		this.NAME = name;
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
	}

	private void loadProductPermissions( Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , "product" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			String product = ConfReader.getAttrValue( node , "name" );
			addProduct( product );
		}
	}
	
	private void loadNetworkPermissions( Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , "network" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			String network = ConfReader.getAttrValue( node , "name" );
			addNetwork( network );
		}
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
		
		for( String product : Common.getSortedKeys( products ) ) {
			Element item = Common.xmlCreateElement( doc , root , "product" );
			Common.xmlSetElementAttr( doc , item , "name" , product );
		}
		
		for( String network : Common.getSortedKeys( networks ) ) {
			Element item = Common.xmlCreateElement( doc , root , "network" );
			Common.xmlSetElementAttr( doc , item , "name" , network );
		}
	}
	
	public void deleteUser( ActionBase action , ServerAuthUser user ) throws Exception {
		users.remove( user.NAME );
	}

	public boolean hasUser( ServerAuthUser user ) {
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
	
}
