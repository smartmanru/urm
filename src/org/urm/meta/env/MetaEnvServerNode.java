package org.urm.meta.env;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumNodeType;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyController;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.shell.Account;
import org.urm.meta.engine.AccountReference;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.engine.NetworkHost;
import org.urm.meta.product.Meta;
import org.urm.meta.product._Error;
import org.urm.meta.EngineObject;
import org.urm.meta.MatchItem;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServerNode extends EngineObject {

	public Meta meta;
	public MetaEnvServer server;

	public ObjectProperties ops;
	public int ID;
	public int POS;
	public DBEnumNodeType NODE_TYPE;
	public MatchItem ACCOUNT;
	public String DEPLOYGROUP;
	public boolean OFFLINE;
	public String DBINSTANCE;
	public boolean DBSTANDBY;
	public int EV;
	
	public static String PROPERTY_NODETYPE = "type";
	public static String PROPERTY_HOSTLOGIN = "account";
	public static String PROPERTY_DEPLOYGROUP = "deploygroup";
	public static String PROPERTY_OFFLINE = "offline";
	public static String PROPERTY_DBINSTANCE = "instance";
	public static String PROPERTY_DBSTANDBY = "standby";
	
	public MetaEnvServerNode( Meta meta , MetaEnvServer server , int POS ) {
		super( server );
		this.meta = meta;
		this.server = server;
		this.POS = POS;
	}

	@Override
	public String getName() {
		return( "" + POS );
	}

	public ObjectProperties getProperties() {
		return( ops );
	}
	
	public void scatterProperties( ActionBase action ) throws Exception {
		action.trace( "load properties of node=" + POS );
		HOSTLOGIN = super.getStringPropertyRequired( action , PROPERTY_HOSTLOGIN );
		DEPLOYGROUP = super.getStringProperty( action , PROPERTY_DEPLOYGROUP );
		
		if( server.isDatabase() )
			DBINSTANCE = super.getStringProperty( action , PROPERTY_DBINSTANCE );
		
		String NODETYPE = super.getStringProperty( action , PROPERTY_NODETYPE , "self" );
		nodeType = Types.getNodeType( NODETYPE , VarNODETYPE.SELF );
		
		OFFLINE = super.getBooleanProperty( action , PROPERTY_OFFLINE );
		DBSTANDBY = super.getBooleanProperty( action , PROPERTY_DBSTANDBY );
		
		super.finishRawProperties();
	}

	public MetaEnvServerNode copy( ActionBase action , Meta meta , MetaEnvServer server ) throws Exception {
		MetaEnvServerNode r = new MetaEnvServerNode( meta , server , POS );
		r.initCopyStarted( this , server.getProperties() );
		r.scatterProperties( action );
		r.resolveLinks( action );
		r.initFinished();
		return( r );
	}
	
	public void setProperties( EngineTransaction transaction , PropertySet props , boolean system ) throws Exception {
		super.updateProperties( transaction , props , system );
		scatterProperties( transaction.getAction() );
	}
	
	public boolean isBroken() {
		return( super.isLoadFailed() );
	}
	
	public void resolveLinks( ActionBase action ) throws Exception {
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			return;

		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		super.loadFromNodeElements( action , node , true );
		super.resolveRawProperties();
	}
	
	public MetaEnvServerNode getProxyNode( ActionBase action ) throws Exception {
		MetaEnvServer proxy = server.proxyServer;
		if( proxy == null )
			action.exit0( _Error.MissingProxyNode0 , "no proxy server to call" );
		
		MetaEnvServerNode node = proxy.getNode( action , POS );
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
		Account account = action.getNodeAccount( this );
		return( account.HOST );
	}
	
	public boolean isSelf( ActionBase action ) throws Exception {
		return( nodeType == VarNODETYPE.SELF );
	}

	public boolean isAdmin( ActionBase action ) throws Exception {
		return( nodeType == VarNODETYPE.ADMIN );
	}

	public boolean isSlave( ActionBase action ) throws Exception {
		return( nodeType == VarNODETYPE.SLAVE );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveSplit( doc , root );
	}
	
	public void createNode( ActionBase action , VarNODETYPE nodeType , Account account ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			return;

		super.setStringProperty( PROPERTY_HOSTLOGIN , account.getHostLogin() );
		super.setStringProperty( PROPERTY_NODETYPE , Common.getEnumLower( nodeType ) );
		super.setBooleanProperty( PROPERTY_OFFLINE , true );
		super.finishProperties( action );
		super.initFinished();
		
		scatterProperties( action );
	}

	public void modifyNode( ActionBase action , int POS , VarNODETYPE nodeType , Account account ) throws Exception {
		this.POS = POS;
		super.setStringProperty( PROPERTY_HOSTLOGIN , account.getHostLogin() );
		super.setStringProperty( PROPERTY_NODETYPE , Common.getEnumLower( nodeType ) );
		scatterProperties( action );
	}

	public void setPos( EngineTransaction transaction , int POS ) {
		this.POS = POS;
	}
	
	public void setOffline( EngineTransaction transaction , boolean offline ) throws Exception {
		super.setSystemBooleanProperty( PROPERTY_OFFLINE , offline );
		scatterProperties( transaction.action );
	}

	public void getApplicationReferences( HostAccount account , List<AccountReference> refs ) {
		if( !checkReferencedByHostAccount( account ) )
			return;
		
		refs.add( new AccountReference( account , this ) );
	}
	
	public boolean checkReferencedByHostAccount( HostAccount account ) {
		Account ha = Account.getDatacenterAccount( server.sg.DC , HOSTLOGIN );
		if( account.host.isEqualsHost( ha ) && account.NAME.equals( ha.USER ) )
			return( true );

		return( false );
	}

	public void deleteHostAccount( EngineTransaction transaction , HostAccount account ) throws Exception {
		if( !checkReferencedByHostAccount( account ) )
			return;

		setOffline( transaction , true );
	}

	public void updateHost( EngineTransaction transaction , NetworkHost host ) throws Exception {
		Account ha = Account.getDatacenterAccount( server.sg.DC , HOSTLOGIN );
		
		ActionBase action = transaction.getAction();
		ha.setHost( action , host );
		super.setStringProperty( PROPERTY_HOSTLOGIN , ha.getHostLogin() );
		scatterProperties( action );
	}

	public HostAccount getHostAccount( ActionBase action ) throws Exception {
		EngineInfrastructure infra = action.getServerInfrastructure();
		return( infra.getHostAccount( ACCOUNT ) );
	}
	
}
