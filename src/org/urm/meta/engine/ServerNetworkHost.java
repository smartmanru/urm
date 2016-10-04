package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.meta.ServerObject;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerNetworkHost extends ServerObject {

	ServerNetwork network;
	Map<String,ServerHostAccount> accountMap;

	String ID;
	String IP;
	VarOSTYPE osType;
	
	public ServerNetworkHost( ServerNetwork network ) {
		super( network );
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

}
