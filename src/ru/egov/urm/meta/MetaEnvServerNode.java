package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.PropertySet;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.Metadata.VarNODETYPE;
import ru.egov.urm.shell.Account;

public class MetaEnvServerNode {

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
	
	public MetaEnvServerNode( MetaEnvServer server , int POS ) {
		this.server = server;
		this.POS = POS;
	}

	public void load( ActionBase action , Node node , boolean loadProps ) throws Exception {
		properties = new PropertySet( "node" , server.properties );
		properties.loadRawFromAttributes( action , node );
		scatterSystemProperties( action );
		if( loadProps ) {
			properties.loadRawFromElements( action , node );
			properties.moveRawAsStrings( action );
		}
	}
	
	public void scatterSystemProperties( ActionBase action ) throws Exception {
		action.trace( "load properties of node=" + POS );
		HOSTLOGIN = properties.getSystemRequiredStringProperty( action , "hostlogin" );
		DEPLOYGROUP = properties.getSystemStringProperty( action , "deploygroup" , "" );
		
		if( server.isDatabase( action ) )
			INSTANCE = properties.getSystemRequiredStringProperty( action , "instance" );
		
		NODETYPE = properties.getSystemStringProperty( action , "type" , "self" );
		nodeType = action.meta.getNodeType( action , NODETYPE );
		
		OFFLINE = properties.getSystemBooleanProperty( action , "offline" , false );
		STANDBY = properties.getSystemBooleanProperty( action , "standby" , false );
		
		properties.finishRawProperties( action );
	}

	public MetaEnvServerNode getProxyNode( ActionBase action ) throws Exception {
		MetaEnvServer proxy = server.proxyServer;
		if( proxy == null )
			action.exit( "no proxy server to call" );
		
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
