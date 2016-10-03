package org.urm.engine.meta;

import org.urm.action.ActionBase;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.urm.engine.meta.Meta.VarNODETYPE;
import org.urm.engine.shell.Account;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServerNode extends PropertyController {

	public Meta meta;
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
		HOSTLOGIN = super.getStringPropertyRequired( action , "hostlogin" );
		DEPLOYGROUP = super.getStringProperty( action , "deploygroup" );
		
		if( server.isDatabase( action ) )
			INSTANCE = super.getStringPropertyRequired( action , "instance" );
		
		NODETYPE = super.getStringProperty( action , "type" , "self" );
		nodeType = Meta.getNodeType( NODETYPE , VarNODETYPE.SELF );
		
		OFFLINE = super.getBooleanProperty( action , "offline" );
		STANDBY = super.getBooleanProperty( action , "standby" );
		
		properties.finishRawProperties();
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
		if( !super.isLoaded() )
			return;
		
		properties.saveSplit( doc , root );
	}
	
}
