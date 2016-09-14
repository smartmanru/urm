package org.urm.engine.meta;

import org.urm.action.ActionBase;
import org.urm.common.PropertySet;
import org.urm.engine.meta.Meta.VarNODETYPE;
import org.urm.engine.shell.Account;
import org.w3c.dom.Node;

public class MetaEnvServerNode {

	protected Meta meta;
	public MetaEnvServer server;
	
	public int POS;
	public String HOSTLOGIN;
	public String DEPLOYGROUP;
	public String INSTANCE;
	public boolean OFFLINE;
	public boolean STANDBY;
	public String NODETYPE;
	private VarNODETYPE nodeType;
	
	public PropertySet properties;
	
	public MetaEnvServerNode( Meta meta , MetaEnvServer server , int POS ) {
		this.meta = meta;
		this.server = server;
		this.POS = POS;
	}

	public void load( ActionBase action , Node node , boolean loadProps ) throws Exception {
		properties = new PropertySet( "node" , server.properties );
		properties.loadRawFromNodeAttributes( node );
		scatterSystemProperties( action );
		
		if( loadProps ) {
			properties.loadRawFromNodeElements( node );
			properties.resolveRawProperties();
		}
	}
	
	public void scatterSystemProperties( ActionBase action ) throws Exception {
		action.trace( "load properties of node=" + POS );
		HOSTLOGIN = properties.getSystemRequiredStringProperty( "hostlogin" );
		DEPLOYGROUP = properties.getSystemStringProperty( "deploygroup" , "" );
		
		if( server.isDatabase( action ) )
			INSTANCE = properties.getSystemRequiredStringProperty( "instance" );
		
		NODETYPE = properties.getSystemStringProperty( "type" , "self" );
		nodeType = meta.getNodeType( NODETYPE , VarNODETYPE.SELF );
		
		OFFLINE = properties.getSystemBooleanProperty( "offline" , false );
		STANDBY = properties.getSystemBooleanProperty( "standby" , false );
		
		properties.finishRawProperties();
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
}
