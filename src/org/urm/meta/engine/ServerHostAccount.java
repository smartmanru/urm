package org.urm.meta.engine;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.ServerTransaction;
import org.urm.engine.shell.Account;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerObject;
import org.urm.meta.ServerProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerHostAccount extends ServerObject {

	public ServerNetworkHost host;

	public String ID;
	public boolean isAdmin;
	public String AUTHRES;
	
	public ServerHostAccount( ServerNetworkHost host ) {
		super( host );
		this.host = host;
		isAdmin = false;
	}
	
	@Override
	public String getName() {
		return( ID );
	}
	
	public ServerHostAccount copy( ServerNetworkHost rh ) throws Exception {
		ServerHostAccount r = new ServerHostAccount( rh );
		r.ID = ID;
		r.isAdmin = isAdmin;
		r.AUTHRES = AUTHRES;
		return( r );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		ID = ConfReader.getAttrValue( root , "id" );
		isAdmin = ConfReader.getBooleanAttrValue( root , "admin" , true );
		AUTHRES = ConfReader.getAttrValue( root , "resource" );
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "admin" , Common.getBooleanValue( isAdmin ) );
		Common.xmlSetElementAttr( doc , root , "resource" , AUTHRES );
	}

	public String getFinalAccount() {
		return( ID + "@" + host.ID );
	}

	public void createAccount( ServerTransaction transaction , String user , boolean isAdmin , String resource ) throws Exception {
		this.ID = user;
		this.isAdmin = isAdmin;
		this.AUTHRES = resource;
	}
	
	public void modifyAccount( ServerTransaction transaction , String user , boolean isAdmin , String resource ) throws Exception {
		this.ID = user;
		this.isAdmin = isAdmin;
		this.AUTHRES = resource;
	}

	public void getApplicationReferences( List<ServerAccountReference> refs ) {
		ServerLoader loader = host.network.datacenter.infra.loader;
		ServerRegistry registry = loader.getRegistry();
		for( String productName : registry.directory.getProducts() ) {
			ServerProductMeta storage = loader.findProductStorage( productName );
			storage.getApplicationReferences( this , refs );
		}
	}

	public void deleteAccount( ServerTransaction transaction ) throws Exception {
		super.deleteObject();
	}

	public Account getHostAccount( ActionBase action ) throws Exception {
		return( Account.getDatacenterAccount( action , host.network.datacenter.ID , ID , host.ID , host.PORT , host.osType ) );
	}
	
}
