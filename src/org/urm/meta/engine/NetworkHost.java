package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.shell.Account;
import org.urm.meta.EngineObject;

public class NetworkHost extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_OSTYPE = "os_type";
	public static String PROPERTY_IP = "ip";
	public static String PROPERTY_PORT = "port";
	
	public Network network;
	Map<String,HostAccount> accountMap;

	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumOSType OS_TYPE;
	public String IP;
	public int PORT;
	public int CV;
	
	public NetworkHost( Network network ) {
		super( network );
		this.network = network;
		accountMap = new HashMap<String,HostAccount>();
		ID = -1;
		CV = 0;
	}
	
	@Override
	public String getName() {
		return( NAME );
	}
	
	public NetworkHost copy( Network rn ) throws Exception {
		NetworkHost r = new NetworkHost( rn );
		r.ID = ID;
		r.NAME = NAME;
		r.IP = IP;
		r.PORT = PORT;
		r.OS_TYPE = OS_TYPE;
		r.DESC = DESC;
		r.CV = CV;
		
		for( HostAccount account : accountMap.values() ) {
			HostAccount raccount = account.copy( r );
			r.addAccount( raccount );
		}
		return( r );
	}
	
	public void addAccount( HostAccount account ) {
		accountMap.put( account.NAME , account );
	}
	
	public void removeAccount( HostAccount account ) throws Exception {
		accountMap.remove( account.NAME );
	}
	
	public String[] getFinalAccounts() {
		List<String> list = new LinkedList<String>();
		for( HostAccount account : accountMap.values() ) {
			String item = account.getFinalAccount();
			list.add( item );
		}
		return( Common.getSortedList( list ) );
	}

	public void createHost( String name , String desc , DBEnumOSType osType , String ip , int port ) throws Exception {
		modifyHost( name , desc , osType , ip , port );
	}
	
	public void modifyHost( String name , String desc , DBEnumOSType osType , String ip , int port ) throws Exception {
		this.OS_TYPE = osType;
		this.NAME = ( name == null || name.isEmpty() )? Common.nonull( ip ) : name;
		this.DESC = Common.nonull( desc );
		this.IP = Common.nonull( ip );
		this.PORT = port;
	}

	public String[] getAccountNames() {
		return( Common.getSortedKeys( accountMap ) );
	}

	public HostAccount findAccount( String accountUser ) {
		for( HostAccount account : accountMap.values() ) {
			if( account.NAME.equals( accountUser ) )
				return( account );
		}
		return( null );
	}
	
	public void updateAccount( HostAccount account ) throws Exception {
		Common.changeMapKey( accountMap , account , account.NAME );
	}

	public boolean isEqualsHost( String host ) {
		if( host.isEmpty() )
			return( false );
		if( host.equals( NAME ) )
			return( true );
		if( host.equals( IP ) )
			return( true );
		return( false );
	}
	
	public HostAccount findFinalAccount( String finalAccount ) {
		if( finalAccount.isEmpty() )
			return( null );
		
		Account account = Account.getDatacenterAccount( network.datacenter , finalAccount );
		if( !isEqualsHost( account.HOST ) )
			return( null );
		
		return( findAccount( account.USER ) );
	}

	public String getHost() {
		Account account = Account.getHostAccount( this );
		return( account.HOST );
	}
	
	public boolean isEqualsHost( Account account ) {
		if( isEqualsHost( account.HOST ) || isEqualsHost( account.IP ) )
			return( true );
		return( false );
	}

	public void getApplicationReferences( ActionBase action , List<AccountReference> refs ) {
		for( HostAccount account : accountMap.values() )
			account.getApplicationReferences( action , refs );
	}

	public boolean isEmpty() {
		if( accountMap.isEmpty() )
			return( true );
		return( false );
	}
	
}
