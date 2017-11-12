package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.db.DBEnumTypes.*;
import org.urm.engine.EngineTransaction;
import org.urm.engine.shell.Account;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class NetworkHost extends EngineObject {

	public Network network;
	Map<String,HostAccount> accountMap;

	public String ID;
	public String IP;
	public int PORT;
	public DBEnumOSType osType;
	public String DESC;
	
	public NetworkHost( Network network ) {
		super( network );
		this.network = network;
		accountMap = new HashMap<String,HostAccount>();
	}
	
	@Override
	public String getName() {
		return( ID );
	}
	
	public NetworkHost copy( Network rn ) throws Exception {
		NetworkHost r = new NetworkHost( rn );
		
		for( HostAccount account : accountMap.values() ) {
			HostAccount raccount = account.copy( r );
			r.addHostAccount( raccount );
		}
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		ID = ConfReader.getAttrValue( root , "id" );
		IP = ConfReader.getAttrValue( root , "ip" );
		PORT = ConfReader.getIntegerAttrValue( root , "port" , 22 );
		String OSTYPE = ConfReader.getAttrValue( root , "ostype" );
		osType = DBEnumOSType.getValue( OSTYPE , false );
		DESC = ConfReader.getAttrValue( root , "desc" );
		
		Node[] list = ConfReader.xmlGetChildren( root , "account" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			HostAccount account = new HostAccount( this );
			account.load( node );
			addHostAccount( account );
		}
	}

	private void addHostAccount( HostAccount account ) {
		accountMap.put( account.ID , account );
	}
	
	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "ip" , IP );
		Common.xmlSetElementAttr( doc , root , "port" , "" + PORT );
		Common.xmlSetElementAttr( doc , root , "ostype" , Common.getEnumLower( osType ) );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		
		for( HostAccount account : accountMap.values() ) {
			Element element = Common.xmlCreateElement( doc , root , "account" );
			account.save( doc , element );
		}
	}

	public String[] getFinalAccounts() {
		List<String> list = new LinkedList<String>();
		for( HostAccount account : accountMap.values() ) {
			String item = account.getFinalAccount();
			list.add( item );
		}
		return( Common.getSortedList( list ) );
	}

	public void createHost( EngineTransaction transaction  , VarOSTYPE osType , String HOSTNAME , String IP , int PORT , String DESC ) throws Exception {
		this.osType = DBEnumOSType.getValue( osType );
		this.ID = ( HOSTNAME.isEmpty() )? IP : HOSTNAME;
		this.IP = IP;
		this.PORT = PORT;
		this.DESC = DESC;
	}
	
	public void modifyHost( EngineTransaction transaction  , VarOSTYPE osType , String HOSTNAME , String IP , int PORT , String DESC ) throws Exception {
		this.osType = DBEnumOSType.getValue( osType );
		this.ID = ( HOSTNAME.isEmpty() )? IP : HOSTNAME;
		this.IP = IP;
		this.PORT = PORT;
		this.DESC = DESC;
	}

	public String[] getAccounts() {
		return( Common.getSortedKeys( accountMap ) );
	}

	public HostAccount findAccount( String accountUser ) {
		for( HostAccount account : accountMap.values() ) {
			if( account.ID.equals( accountUser ) )
				return( account );
		}
		return( null );
	}
	
	public void createAccount( EngineTransaction transaction , HostAccount account ) throws Exception {
		addHostAccount( account );
	}
	
	public void deleteAccount( EngineTransaction transaction , HostAccount account ) throws Exception {
		accountMap.remove( account.ID );
	}
	
	public void modifyAccount( EngineTransaction transaction , HostAccount account ) {
		String oldId = null;
		for( Entry<String,HostAccount> entry : accountMap.entrySet() ) {
			if( entry.getValue() == account )
				oldId = entry.getKey();
		}
		accountMap.remove( oldId );
		addHostAccount( account );
	}

	public boolean isEqualsHost( String host ) {
		if( host.isEmpty() )
			return( false );
		if( host.equals( ID ) )
			return( true );
		if( host.equals( IP ) )
			return( true );
		return( false );
	}
	
	public HostAccount findFinalAccount( String finalAccount ) {
		if( finalAccount.isEmpty() )
			return( null );
		
		Account account = Account.getDatacenterAccount( network.datacenter.ID , finalAccount );
		if( !isEqualsHost( account.HOST ) )
			return( null );
		
		return( findAccount( account.USER ) );
	}

	public boolean isEqualsHost( Account account ) {
		if( isEqualsHost( account.HOST ) || isEqualsHost( account.IP ) )
			return( true );
		return( false );
	}

	public HostAccount createAccount( EngineTransaction transaction , Account hostAccount , AuthResource resource ) throws Exception {
		HostAccount account = findAccount( hostAccount.USER );
		if( account != null )
			return( account );
				
		account = new HostAccount( this );
		boolean isAdmin = ( hostAccount.isLinux() && hostAccount.USER.equals( "root" ) )? true : false;
		
		String ACCRES = ( resource == null )? "" : resource.NAME;
		account.createAccount( transaction , hostAccount.USER , isAdmin , ACCRES );
		createAccount( transaction , account );
		return( account );
	}
	
	public void deleteHost( EngineTransaction transaction ) throws Exception {
		super.deleteObject();
	}

	public void getApplicationReferences( List<AccountReference> refs ) {
		for( HostAccount account : accountMap.values() )
			account.getApplicationReferences( refs );
	}
	
}
