package org.urm.action;

import org.urm.common.Common;
import org.urm.common.ExitException;
import org.urm.engine.ServerAuthResource;
import org.urm.engine.ServerBuilders;
import org.urm.engine.ServerEngine;
import org.urm.engine.ServerMirror;
import org.urm.engine.ServerMirrorRepository;
import org.urm.engine.ServerProjectBuilder;
import org.urm.engine.ServerResources;
import org.urm.engine.meta.Meta.VarCATEGORY;

public class ActionCore {

	public ServerEngine engine;
	public ActionCore parent;
	
	private static int instanceSequence = 0;
	public int ID;
	public String NAME;

	boolean progressFailed;
	public int progressMax;
	public int progressCurrent;
	public ExitException progressError;
	
	protected ActionCore( ServerEngine engine , ActionCore parent ) {
		this.engine = engine;
		this.parent = parent;
		
		ID = instanceSequence++;
		NAME = this.getClass().getSimpleName();
		
		progressFailed = false;
		progressMax = 0;
		progressCurrent = 0;
	}

	public boolean isFailed() {
		return( progressFailed );
	}
	
	protected void setFailed( ExitException exception ) {
		progressFailed = true;
		progressError = exception;
	}
	
	public boolean isStandalone() {
		return( engine.execrc.standaloneMode );
	}
	
	public String getLocalPath( String path ) throws Exception {
		return( engine.execrc.getLocalPath( path ) );
	}
	
	public String getInternalPath( String path ) throws Exception {
		return( Common.getLinuxPath( path ) );
	}
	
	public ServerProjectBuilder getBuilder( String name ) throws Exception {
		ServerBuilders builders = engine.getBuilders();
		ServerProjectBuilder builder = builders.getBuilder( name );
		return( builder );
	}

	public ServerMirrorRepository getMirror( String name ) throws Exception {
		ServerMirror mirror = engine.getMirror();
		ServerMirrorRepository repo = mirror.findRepository( name );
		return( repo );
	}
	
	public ServerMirrorRepository getServerMirror() throws Exception {
		return( getMirror( "core" ) );
	}
	
	public ServerAuthResource getResource( String name ) throws Exception {
		ServerResources resources = engine.getResources();
		ServerAuthResource res = resources.getResource( name );
		return( res );
	}
	
	public void fail( int errorCode , String s , String[] params ) throws Exception {
		setFailed( new ExitException( errorCode , s , params ) );
	}

	public void fail0( int errorCode , String s ) throws Exception {
		fail( errorCode , s , null );
	}

	public void fail1( int errorCode , String s , String param1 ) throws Exception {
		fail( errorCode , s , new String[] { param1 } );
	}

	public void fail2( int errorCode , String s , String param1 , String param2 ) throws Exception {
		fail( errorCode , s , new String[] { param1 , param2 } );
	}

	public void fail3( int errorCode , String s , String param1 , String param2 , String param3 ) throws Exception {
		fail( errorCode , s , new String[] { param1 , param2 , param3 } );
	}

	public void fail4( int errorCode , String s , String param1 , String param2 , String param3 , String param4 ) throws Exception {
		fail( errorCode , s , new String[] { param1 , param2 , param3 , param4 } );
	}

	public void exit( int errorCode , String s , String[] params ) throws Exception {
		fail( errorCode , s , params );
		throw progressError;
	}

	public void exit0( int errorCode , String s ) throws Exception {
		exit( errorCode , s , null );
	}

	public void exit1( int errorCode , String s , String param1 ) throws Exception {
		exit( errorCode , s , new String[] { param1 } );
	}

	public void exit2( int errorCode , String s , String param1 , String param2 ) throws Exception {
		exit( errorCode , s , new String[] { param1 , param2 } );
	}

	public void exit3( int errorCode , String s , String param1 , String param2 , String param3 ) throws Exception {
		exit( errorCode , s , new String[] { param1 , param2 , param3 } );
	}

	public void exit4( int errorCode , String s , String param1 , String param2 , String param3 , String param4 ) throws Exception {
		exit( errorCode , s , new String[] { param1 , param2 , param3 , param4 } );
	}

	public void exitNotImplemented() throws Exception {
		exit( _Error.NotImplemented0 , "sorry, code is not implemented yet" , null );
	}
	
	public void exitUnexpectedCategory( VarCATEGORY CATEGORY ) throws Exception {
		String category = Common.getEnumLower( CATEGORY );
		exit( _Error.UnexpectedCategory1 , "unexpected category=" + category , new String[] { category } );
	}

	public void exitUnexpectedState() throws Exception {
		exit( _Error.InternalError0 , "unexpected state" , null );
	}
	
}

