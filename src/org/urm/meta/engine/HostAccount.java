package org.urm.meta.engine;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBEnums.*;
import org.urm.engine.EngineTransaction;
import org.urm.engine.shell.Account;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HostAccount extends EngineObject {

	public NetworkHost host;

	public String ID;
	public boolean isAdmin;
	public String AUTHRES;
	
	public HostAccount( NetworkHost host ) {
		super( host );
		this.host = host;
		isAdmin = false;
	}
	
	@Override
	public String getName() {
		return( ID );
	}
	
	public HostAccount copy( NetworkHost rh ) throws Exception {
		HostAccount r = new HostAccount( rh );
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

	public void createAccount( EngineTransaction transaction , String user , boolean isAdmin , String resource ) throws Exception {
		this.ID = user;
		this.isAdmin = isAdmin;
		this.AUTHRES = resource;
	}
	
	public void modifyAccount( EngineTransaction transaction , String user , boolean isAdmin , String resource ) throws Exception {
		this.ID = user;
		this.isAdmin = isAdmin;
		this.AUTHRES = resource;
	}

	public void getApplicationReferences( List<AccountReference> refs ) {
		EngineData data = host.network.datacenter.infra.data;
		EngineDirectory directory = data.getDirectory();
		for( String productName : directory.getProductNames() ) {
			ProductMeta storage = data.findProductStorage( productName );
			storage.getApplicationReferences( this , refs );
		}
	}

	public void deleteAccount( EngineTransaction transaction ) throws Exception {
		super.deleteObject();
	}

	public Account getHostAccount( ActionBase action ) throws Exception {
		return( Account.getDatacenterAccount( action , host.network.datacenter.ID , ID , host.ID , host.PORT , DBEnumOSType.getVarValue( host.osType ) ) );
	}
	
}
