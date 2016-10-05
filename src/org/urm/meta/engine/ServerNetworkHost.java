package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.engine.ServerTransaction;
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

	public void createHost( ServerTransaction transaction  , VarOSTYPE osType , String HOSTNAME , String IP ) throws Exception {
		this.osType = osType;
		this.ID = ( HOSTNAME.isEmpty() )? IP : HOSTNAME;
		this.IP = IP;
	}
	
	public void modifyHost( ServerTransaction transaction  , VarOSTYPE osType , String HOSTNAME , String IP ) throws Exception {
		this.osType = osType;
		this.ID = ( HOSTNAME.isEmpty() )? IP : HOSTNAME;
		this.IP = IP;
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
}
