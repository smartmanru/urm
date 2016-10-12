package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.ServerTransaction;
import org.urm.engine.shell.Account;
import org.urm.meta.ServerObject;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerNetworkHost extends ServerObject {

	public ServerNetwork network;
	Map<String,ServerHostAccount> accountMap;

	public String ID;
	public String IP;
	public VarOSTYPE osType;
	public String DESC;
	
	public ServerNetworkHost( ServerNetwork network ) {
		super( network );
		this.network = network;
		accountMap = new HashMap<String,ServerHostAccount>();
	}
	
	public ServerNetworkHost copy( ServerNetwork rn ) throws Exception {
		ServerNetworkHost r = new ServerNetworkHost( rn );
		
		for( ServerHostAccount account : accountMap.values() ) {
			ServerHostAccount raccount = account.copy( r );
			r.addHostAccount( raccount );
		}
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		ID = ConfReader.getAttrValue( root , "id" );
		IP = ConfReader.getAttrValue( root , "ip" );
		String OSTYPE = ConfReader.getAttrValue( root , "ostype" );
		osType = Meta.getOSType( OSTYPE );
		DESC = ConfReader.getAttrValue( root , "desc" );
		
		Node[] list = ConfReader.xmlGetChildren( root , "account" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerHostAccount account = new ServerHostAccount( this );
			account.load( node );
			addHostAccount( account );
		}
	}

	private void addHostAccount( ServerHostAccount account ) {
		accountMap.put( account.ID , account );
	}
	
	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "ip" , IP );
		Common.xmlSetElementAttr( doc , root , "ostype" , Common.getEnumLower( osType ) );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		
		for( ServerHostAccount account : accountMap.values() ) {
			Element element = Common.xmlCreateElement( doc , root , "account" );
			account.save( doc , element );
		}
	}

	public String[] getFinalAccounts() {
		List<String> list = new LinkedList<String>();
		for( ServerHostAccount account : accountMap.values() ) {
			String item = account.getFinalAccount();
			list.add( item );
		}
		return( Common.getSortedList( list ) );
	}

	public void createHost( ServerTransaction transaction  , VarOSTYPE osType , String HOSTNAME , String IP , String DESC ) throws Exception {
		this.osType = osType;
		this.ID = ( HOSTNAME.isEmpty() )? IP : HOSTNAME;
		this.IP = IP;
		this.DESC = DESC;
	}
	
	public void modifyHost( ServerTransaction transaction  , VarOSTYPE osType , String HOSTNAME , String IP , String DESC ) throws Exception {
		this.osType = osType;
		this.ID = ( HOSTNAME.isEmpty() )? IP : HOSTNAME;
		this.IP = IP;
		this.DESC = DESC;
	}

	public String[] getAccounts() {
		return( Common.getSortedKeys( accountMap ) );
	}

	public ServerHostAccount findAccount( String accountUser ) {
		for( ServerHostAccount account : accountMap.values() ) {
			if( account.ID.equals( accountUser ) )
				return( account );
		}
		return( null );
	}
	
	public void createAccount( ServerTransaction transaction , ServerHostAccount account ) throws Exception {
		addHostAccount( account );
	}
	
	public void deleteAccount( ServerTransaction transaction , ServerHostAccount account ) throws Exception {
		accountMap.remove( account.ID );
	}
	
	public void modifyAccount( ServerTransaction transaction , ServerHostAccount account ) {
		String oldId = null;
		for( Entry<String,ServerHostAccount> entry : accountMap.entrySet() ) {
			if( entry.getValue() == account )
				oldId = entry.getKey();
		}
		accountMap.remove( oldId );
		addHostAccount( account );
	}

	public boolean isEqualsHost( String host ) {
		if( host.equals( ID ) )
			return( true );
		if( host.equals( IP ) )
			return( true );
		return( false );
	}
	
	public ServerHostAccount findFinalAccount( String finalAccount ) {
		if( finalAccount.isEmpty() )
			return( null );
		
		Account account = Account.getAnyAccount( finalAccount );
		if( !isEqualsHost( account.HOST ) )
			return( null );
		
		return( findAccount( account.USER ) );
	}

	public boolean isEqualsHost( Account account ) {
		if( isEqualsHost( account.HOST ) || isEqualsHost( account.IP ) )
			return( true );
		return( false );
	}

	public ServerHostAccount createAccount( ServerTransaction transaction , Account hostAccount ) throws Exception {
		ServerHostAccount account = findAccount( hostAccount.USER );
		if( account != null )
			return( account );
				
		account = new ServerHostAccount( this );
		boolean isAdmin = ( hostAccount.isLinux() && hostAccount.USER.equals( "root" ) )? true : false;
		account.createAccount( transaction , hostAccount.USER , isAdmin );
		createAccount( transaction , account );
		return( account );
	}
	
	public void deleteHost( ServerTransaction transaction ) throws Exception {
		super.deleteObject();
	}

	public void getApplicationReferences( List<ServerAccountReference> refs ) {
		for( ServerHostAccount account : accountMap.values() )
			account.getApplicationReferences( refs );
	}
	
}
