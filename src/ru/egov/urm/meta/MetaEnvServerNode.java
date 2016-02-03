package ru.egov.urm.meta;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

import ru.egov.urm.PropertySet;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.Account;

public class MetaEnvServerNode {

	public MetaEnvServer server;
	
	public int POS;
	public String HOSTLOGIN;
	public String DEPLOYGROUP;
	public String INSTANCE;
	public boolean OFFLINE;
	public boolean STANDBY;
	
	public PropertySet properties;
	
	public MetaEnvServerNode( MetaEnvServer server , int POS ) {
		this.server = server;
		this.POS = POS;
	}

	public void load( ActionBase action , Node node , boolean loadProps ) throws Exception {
		properties = new PropertySet( "node" , server.properties );
		properties.loadFromAttributes( action , node );
		scatterSystemProperties( action );
		if( loadProps )
			properties.loadFromElements( action , node );
	}
	
	public void scatterSystemProperties( ActionBase action ) throws Exception {
		List<String> systemProps = new LinkedList<String>();
	
		action.trace( "load properties of node=" + POS );
		HOSTLOGIN = properties.getSystemRequiredProperty( action , "hostlogin" , systemProps );
		DEPLOYGROUP = properties.getSystemProperty( action , "deploygroup" , "" , systemProps );
		
		if( server.TYPE == VarSERVERTYPE.DATABASE )
			INSTANCE = properties.getSystemRequiredProperty( action , "instance" , systemProps );
		
		OFFLINE = properties.getSystemBooleanProperty( action , "offline" , false , systemProps );
		STANDBY = properties.getSystemBooleanProperty( action , "standby" , false , systemProps );
		
		properties.checkUnexpected( action , systemProps );
	}

	public MetaEnvServerNode getProxyNode( ActionBase action ) throws Exception {
		MetaEnvServer proxy = server.proxyServer;
		if( proxy == null )
			action.exit( "no proxy server to call" );
		
		MetaEnvServerNode node = proxy.getNode( action , POS );
		return( node );
	}

	public String getAccessPoint( ActionBase action ) throws Exception {
		Account account = action.getAccount( this ); 
		if( server.PORT > 0 )
			return( account.HOST + ":" + server.PORT );
		return( account.HOST );
	}

	public String getHost( ActionBase action ) throws Exception {
		Account account = action.getAccount( this );
		return( account.HOST );
	}
}
