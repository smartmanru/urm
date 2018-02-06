package org.urm.meta.env;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.shell.Account;
import org.urm.meta.engine.AccountReference;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.product.Meta;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.urm.meta.MatchItem;

public class MetaEnvServerNode extends EngineObject {

	// properties
	public static String PROPERTY_POS = "pos";
	public static String PROPERTY_NODETYPE = "type";
	public static String PROPERTY_HOSTLOGIN = "account";
	public static String PROPERTY_DEPLOYGROUP = "deploygroup";
	public static String PROPERTY_OFFLINE = "offline";
	public static String PROPERTY_DBINSTANCE = "instance";
	public static String PROPERTY_DBSTANDBY = "standby";
	
	public Meta meta;
	public MetaEnvServer server;

	private ObjectProperties ops;
	public int ID;
	public int POS;
	public DBEnumNodeType NODE_TYPE;
	private MatchItem ACCOUNT;
	public String DEPLOYGROUP;
	public boolean OFFLINE;
	public String DBINSTANCE;
	public boolean DBSTANDBY;
	public int EV;
	
	public MetaEnvServerNode( Meta meta , MetaEnvServer server ) {
		super( server );
		this.meta = meta;
		this.server = server;
		ID = -1;
		EV = -1;
	}

	@Override
	public String getName() {
		return( "" + POS );
	}

	public MetaEnvServerNode copy( Meta rmeta , MetaEnvServer rserver ) throws Exception {
		MetaEnvServerNode r = new MetaEnvServerNode( rmeta , rserver );
		
		r.ops = ops.copy( rserver.getProperties() );
		r.ID = ID;
		r.POS = POS;
		r.NODE_TYPE = NODE_TYPE;
		r.ACCOUNT = MatchItem.copy( ACCOUNT );
		r.DEPLOYGROUP = DEPLOYGROUP;
		r.OFFLINE = OFFLINE;
		r.DBINSTANCE = DBINSTANCE;
		r.DBSTANDBY = DBSTANDBY;
		r.EV = EV;
		
		refreshPrimaryProperties();
		
		return( r );
	}
	
	public boolean checkMatched() {
		if( !MatchItem.isMatched( ACCOUNT ) )
			return( false );
		return( true );
	}
	
	public ObjectProperties getProperties() {
		return( ops );
	}
	
	public void refreshPrimaryProperties() throws Exception {
		ops.clearProperties( DBEnumParamEntityType.ENV_NODE_PRIMARY );
		
		ops.setIntProperty( PROPERTY_POS , POS );
		ops.setEnumProperty( PROPERTY_NODETYPE , NODE_TYPE );
		
		if( ACCOUNT != null ) {
			HostAccount account = getHostAccount();
			ops.setStringProperty( PROPERTY_HOSTLOGIN , account.getFinalAccount() );
		}
		
		ops.setStringProperty( PROPERTY_DEPLOYGROUP , DEPLOYGROUP );
		ops.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
		ops.setStringProperty( PROPERTY_DBINSTANCE , DBINSTANCE );
		ops.setBooleanProperty( PROPERTY_DBSTANDBY , DBSTANDBY );
	}

	public void updateCustomSettings() throws Exception {
	}
	
	public MetaEnvServerNode getProxyNode() throws Exception {
		MetaEnvServer proxy = server.getProxyServer();
		if( proxy == null )
			Common.exit0( _Error.MissingProxyNode0 , "no proxy server to call" );
		
		MetaEnvServerNode node = proxy.getNode( POS );
		return( node );
	}

	public String getAccessPoint( ActionBase action ) throws Exception {
		Account account = action.getNodeAccount( this ); 
		String value = "http://" + account.HOST; 
		if( server.PORT > 0 )
			value += ":" + server.PORT;
		return( value );
	}

	public String getHost( ActionBase action ) throws Exception {
		HostAccount account = getHostAccount();
		return( account.host.getHost() );
	}
	
	public boolean isSelf() throws Exception {
		return( NODE_TYPE == DBEnumNodeType.SELF );
	}

	public boolean isAdmin() throws Exception {
		return( NODE_TYPE == DBEnumNodeType.ADMIN );
	}

	public boolean isSlave() throws Exception {
		return( NODE_TYPE == DBEnumNodeType.SLAVE );
	}

	public void createNode( int pos , DBEnumNodeType type , MatchItem account , String deployGroup , boolean offline , String dbInstance , boolean dbStandBy ) throws Exception {
		modifyNode( pos , type , account , deployGroup , offline , dbInstance , dbStandBy );
	}
	
	public void modifyNode( int pos , DBEnumNodeType type , MatchItem account , String deployGroup , boolean offline , String dbInstance , boolean dbStandBy ) throws Exception {
		this.POS = pos;
		this.NODE_TYPE = type;
		this.ACCOUNT = MatchItem.copy( account );
		this.DEPLOYGROUP = deployGroup;
		this.OFFLINE = offline;
		this.DBINSTANCE = dbInstance;
		this.DBSTANDBY = dbStandBy;
		
		refreshPrimaryProperties();
	}

	public void setPos( int pos ) throws Exception {
		this.POS = pos;
		refreshPrimaryProperties();
	}
	
	public void setOffline( boolean offline ) throws Exception {
		this.OFFLINE = offline;
		refreshPrimaryProperties();
	}

	public void getApplicationReferences( HostAccount account , List<AccountReference> refs ) {
		if( !checkReferencedByHostAccount( account ) )
			return;
		
		refs.add( new AccountReference( account , this ) );
	}
	
	public boolean checkReferencedByHostAccount( HostAccount account ) {
		if( MatchItem.equals( ACCOUNT , account.ID ) )
			return( true );
		return( false );
	}

	public void removeHostAccount() throws Exception {
		ACCOUNT = null;
		setOffline( true );
	}

	public HostAccount getHostAccount() throws Exception {
		EngineData data = meta.getEngineData();
		EngineInfrastructure infra = data.getInfrastructure();
		return( infra.getHostAccount( ACCOUNT ) );
	}
	
}
