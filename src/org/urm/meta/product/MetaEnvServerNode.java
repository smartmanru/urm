package org.urm.meta.product;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.PropertyController;
import org.urm.engine.ServerTransaction;
import org.urm.engine.shell.Account;
import org.urm.meta.engine.ServerAccountReference;
import org.urm.meta.engine.ServerHostAccount;
import org.urm.meta.product.Meta.VarNODETYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServerNode extends PropertyController {

	public Meta meta;
	public MetaEnvServer server;
	
	public int POS;
	public VarNODETYPE nodeType;
	public String HOSTLOGIN;
	public String DEPLOYGROUP;
	public boolean OFFLINE;
	public String DBINSTANCE;
	public boolean DBSTANDBY;
	
	public static String PROPERTY_NODETYPE = "type";
	public static String PROPERTY_HOSTLOGIN = "account";
	public static String PROPERTY_DEPLOYGROUP = "deploygroup";
	public static String PROPERTY_OFFLINE = "offline";
	public static String PROPERTY_DBINSTANCE = "instance";
	public static String PROPERTY_DBSTANDBY = "standby";
	
	public MetaEnvServerNode( Meta meta , MetaEnvServer server , int POS ) {
		super( server , "node" );
		this.meta = meta;
		this.server = server;
		this.POS = POS;
	}

	@Override
	public boolean isValid() {
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		action.trace( "load properties of node=" + POS );
		HOSTLOGIN = super.getStringPropertyRequired( action , PROPERTY_HOSTLOGIN );
		DEPLOYGROUP = super.getStringProperty( action , PROPERTY_DEPLOYGROUP );
		
		if( server.isDatabase() )
			DBINSTANCE = super.getStringProperty( action , PROPERTY_DBINSTANCE );
		
		String NODETYPE = super.getStringProperty( action , PROPERTY_NODETYPE , "self" );
		nodeType = Meta.getNodeType( NODETYPE , VarNODETYPE.SELF );
		
		OFFLINE = super.getBooleanProperty( action , PROPERTY_OFFLINE );
		DBSTANDBY = super.getBooleanProperty( action , PROPERTY_DBSTANDBY );
		
		properties.finishRawProperties();
	}

	public MetaEnvServerNode copy( ActionBase action , Meta meta , MetaEnvServer server ) throws Exception {
		MetaEnvServerNode r = new MetaEnvServerNode( meta , server , POS );
		r.initCopyStarted( this , server.getProperties() );
		r.scatterProperties( action );
		r.resolveLinks( action );
		r.initFinished();
		return( r );
	}
	
	public boolean isBroken() {
		return( super.isLoadFailed() );
	}
	
	public void resolveLinks( ActionBase action ) throws Exception {
	}
	
	public void load( ActionBase action , Node node , boolean loadProps ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			return;

		properties.loadFromNodeAttributes( node );
		scatterProperties( action );
		
		if( loadProps ) {
			properties.loadFromNodeElements( node );
			properties.resolveRawProperties();
		}
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
		if( server.PORT > 0 )
			return( account.HOST + ":" + server.PORT );
		return( account.HOST );
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
		properties.saveSplit( doc , root );
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

	public void setPos( ServerTransaction transaction , int POS ) {
		this.POS = POS;
	}
	
	public void setOffline( ServerTransaction transaction , boolean offline ) throws Exception {
		properties.setBooleanProperty( PROPERTY_OFFLINE , offline );
		scatterProperties( transaction.action );
	}

	public boolean isOffline() {
		return( OFFLINE );
	}

	public void getApplicationReferences( ServerHostAccount account , List<ServerAccountReference> refs ) {
		if( !checkReferencedByHostAccount( account ) )
			return;
		
		refs.add( new ServerAccountReference( account , this ) );
	}
	
	public boolean checkReferencedByHostAccount( ServerHostAccount account ) {
		Account ha = Account.getAnyAccount( HOSTLOGIN );
		if( account.host.isEqualsHost( ha ) && account.ID.equals( ha.USER ) )
			return( true );

		return( false );
	}

	public void deleteHostAccount( ServerTransaction transaction , ServerHostAccount account ) throws Exception {
		if( !checkReferencedByHostAccount( account ) )
			return;

		setOffline( transaction , true );
	}
	
}
