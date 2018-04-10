package org.urm.engine.session;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.action.CommandOptions;
import org.urm.engine.SessionService;
import org.urm.engine._Error;
import org.urm.engine.action.ActionInit;
import org.urm.meta.EngineObject;
import org.urm.meta.engine.AuthContext;
import org.urm.meta.product.Meta;

public class EngineSession extends EngineObject {

	public SessionService controller;
	public RunContext clientrc;
	public RunContext execrc;
	public int sessionId;
	
	public String ENV;
	public String SG;

	public boolean offline = false;
	public boolean product = false;
	public boolean standalone = false;
	public boolean client = false;
	public String timestamp;
	
	public String installPath = "";
	public String masterPath = "";
	public String productName = "";
	public String binPath = "";
	
	private boolean closed;
	
	private Map<String,Meta> productMeta;
	private Map<Integer,Meta> productMetaById;
	private SessionSecurity security;
	
	public EngineSession( SessionService controller , SessionSecurity security , RunContext clientrc , int sessionId , boolean client ) {
		super( null );
		this.controller = controller;
		this.security = security;
		this.clientrc = clientrc;
		this.sessionId = sessionId;
		this.client = client;
		
		this.execrc = controller.engine.execrc;
		this.ENV = clientrc.envName;
		this.SG = clientrc.sgName;
		
		timestamp = Common.getNameTimeStamp();
		productMeta = new HashMap<String,Meta>();
		productMetaById = new HashMap<Integer,Meta>();
		closed = false;
	}

	@Override
	public String getName() {
		return( "server-session" );
	}
	
	public void trace( String s ) {
		controller.engine.trace( s + " [" + "session" + sessionId + "]" );
	}
	
	public void close() throws Exception {
		closed = true;
		releaseMeta( controller.engine.serverAction );
		super.deleteObject();
	}

	public void releaseMeta( ActionInit action ) throws Exception {
		for( String product : Common.getSortedKeys( productMeta ) ) {
			Meta meta = productMeta.get( product );
			controller.releaseSessionProductMetadata( action , meta );
		}
	}
	
	public boolean isClosed() {
		return( closed );
	}
	
	public synchronized Meta findMeta( String productName ) {
		return( productMeta.get( productName ) );
	}

	public synchronized Meta findMeta( int metaId ) {
		return( productMetaById.get( metaId ) );
	}

	public synchronized void addProductMeta( Meta meta ) {
		productMeta.put( meta.name , meta );
		productMetaById.put( meta.getId() , meta );
	}
	
	public synchronized void releaseProductMeta( Meta meta ) {
		productMeta.remove( meta.name );
		productMetaById.remove( meta.getId() );
	}
	
	public void setLoginAuth( AuthContext ac ) {
		security.setContext( ac );
	}
	
	public AuthContext getLoginAuth() {
		return( security.getContext() );
	}
	
	public void setServerLayout( CommandOptions options ) throws Exception {
		installPath = clientrc.installPath;
		if( installPath.isEmpty() )
			Common.exit0( _Error.MasterpathEmpty0 , "masterpath is empty" );
		
		productName = "";
		masterPath = Common.getPath( installPath , "master" );
		binPath = Common.getPath( masterPath , "bin" );
	}

	public void setServerOfflineProductLayout( ActionBase serverAction , CommandOptions options , String name ) throws Exception {
		offline = true;
		
		setServerLayout( options );
		product = true;
		productName = name;
	}
	
	public void setServerRemoteProductLayout( ActionBase serverAction ) throws Exception {
		EngineSession serverSession = serverAction.session;
		
		installPath = serverSession.installPath;
		masterPath = serverSession.masterPath;
		binPath = serverSession.binPath;
		
		product = true;
		productName = clientrc.product;
	}
	
	public void setStandaloneLayout( CommandOptions options ) throws Exception {
		offline = true;
		standalone = true;
		product = true;
		
		product = true;
		productName = clientrc.product;
	}

	public SessionSecurity getSecurity() {
		return( security );
	}
	
}
