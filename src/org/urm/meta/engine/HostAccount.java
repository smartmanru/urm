package org.urm.meta.engine;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.engine.shell.Account;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.loader.EngineObject;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

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
	public Integer RESOURCE_ID;
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
		r.RESOURCE_ID = RESOURCE_ID;
		r.CV = CV;
		return( r );
	}
	
	public String getFinalAccount() {
		return( NAME + "@" + host.NAME );
	}

	public Account getAccount() throws Exception {
		return( Account.getHostAccount( this ) );
	}
	
	public void createAccount( String user , String desc , boolean isAdmin , Integer resource_id ) throws Exception {
		modifyAccount( user , desc , isAdmin , resource_id );
	}
	
	public void modifyAccount( String user , String desc , boolean isAdmin , Integer resource_id ) throws Exception {
		this.NAME = user;
		this.DESC = Common.nonull( desc );
		this.ADMIN = isAdmin;
		this.RESOURCE_ID = resource_id;
	}

	public void getApplicationReferences( ActionBase action , List<AccountReference> refs ) {
		EngineDirectory directory = action.getEngineDirectory();
		for( String productName : directory.getProductNames() ) {
			AppProduct product = directory.findProduct( productName );
			EngineProduct ep = product.findEngineProduct();
			EngineProductRevisions epr = ep.getRevisions();
			
			for( ProductMeta storage : epr.getRevisions() ) {
				Meta meta = ep.findSessionMeta( action , storage , true );
				ProductEnvs envs = meta.getEnviroments();
				envs.getApplicationReferences( this , refs );
			}
		}
	}

}
