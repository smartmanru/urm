package org.urm.meta.engine;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.shell.Account;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;

public class HostAccount extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_ADMIN = "admin";
	public static String PROPERTY_RESOURCE = "resource";
	
	public NetworkHost host;

	public int ID;
	public String NAME;
	public String DESC;
	public boolean ADMIN;
	public String RESOURCE;
	public int CV;
	
	public HostAccount( NetworkHost host ) {
		super( host );
		this.host = host;
		ADMIN = false;
		ID = -1;
		CV = 0;
	}
	
	@Override
	public String getName() {
		return( NAME );
	}
	
	public HostAccount copy( NetworkHost rh ) throws Exception {
		HostAccount r = new HostAccount( rh );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.ADMIN = ADMIN;
		r.RESOURCE = RESOURCE;
		r.CV = CV;
		return( r );
	}
	
	public String getFinalAccount() {
		return( NAME + "@" + host.NAME );
	}

	public void createAccount( String user , boolean isAdmin , String resource ) throws Exception {
		modifyAccount( user , isAdmin , resource );
	}
	
	public void modifyAccount( String user , boolean isAdmin , String resource ) throws Exception {
		this.NAME = user;
		this.ADMIN = isAdmin;
		this.RESOURCE = resource;
	}

	public void getApplicationReferences( List<AccountReference> refs ) {
		EngineData data = host.network.datacenter.infra.core.data;
		EngineDirectory directory = data.getDirectory();
		for( String productName : directory.getProductNames() ) {
			ProductMeta storage = data.findProductStorage( productName );
			storage.getApplicationReferences( this , refs );
		}
	}

	public Account getHostAccount( ActionBase action ) throws Exception {
		return( Account.getDatacenterAccount( action , host.network.datacenter.NAME , NAME , host.NAME , host.PORT , DBEnumOSType.getVarValue( host.OS_TYPE ) ) );
	}
	
}
