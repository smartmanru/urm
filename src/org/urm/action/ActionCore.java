package org.urm.action;

import org.urm.common.Common;
import org.urm.common.RunError;
import org.urm.common.RunContext;
import org.urm.engine.ServerBlotterActionItem;
import org.urm.engine.ServerBlotterTreeItem;
import org.urm.engine.ServerEngine;
import org.urm.meta.Types.*;

public class ActionCore {

	public ActionCore parent;
	public ServerEngine engine;
	public ServerBlotterActionItem blotterRootItem;
	public ServerBlotterTreeItem blotterTreeItem;
	public RunContext execrc;
	
	private static int instanceSequence = 0;
	public int ID;
	public String NAME;
	public String INFO;

	private boolean callFailed;
	private boolean progressFailed;
	private int progressMax;
	private int progressCurrent;
	private RunError progressError;
	
	public ActionEventsSource eventSource;
	
	protected ActionCore( ServerEngine engine , ActionCore parent , String INFO ) {
		this.engine = engine;
		this.parent = parent;
		this.execrc = engine.execrc;
		this.INFO = INFO;
		
		ID = instanceSequence++;
		NAME = this.getClass().getSimpleName();
		
		callFailed = false;
		progressFailed = false;
		progressMax = 0;
		progressCurrent = 0;
		
		eventSource = new ActionEventsSource( this );
	}

	public void setBlotterItem( ServerBlotterActionItem blotterItem , ServerBlotterTreeItem blotterTreeItem ) {
		this.blotterRootItem = blotterItem;
		this.blotterTreeItem = blotterTreeItem;
	}
	
	public boolean isCallFailed() {
		return( callFailed );
	}
	
	public boolean isFailed() {
		return( progressFailed );
	}
	
	public boolean isOK() {
		return( ( progressFailed )? false : true );
	}
	
	public int getProgressMax() {
		return( progressMax );
	}
	
	public int getProgressCurrent() {
		return( progressCurrent );
	}

	public void clearCall() {
		callFailed = false;
	}
	
	protected void setFailed( RunError exception ) {
		callFailed = true;
		progressFailed = true;
		progressError = exception;
	}
	
	public RunError getError() {
		if( isCallFailed() )
			return( progressError );
		return( null );
	}
	
	public boolean isStandalone() {
		return( engine.execrc.isStandalone() );
	}
	
	public String getLocalPath( String path ) {
		return( engine.execrc.getLocalPath( path ) );
	}
	
	public String getInternalPath( String path ) throws Exception {
		return( Common.getLinuxPath( path ) );
	}
	
	public void fail( RunError error ) {
		setFailed( error );
		if( parent != null )
			parent.fail( error );
	}
	
	public void fail( int errorCode , String s , String[] params ) {
		setFailed( new RunError( errorCode , s , params ) );
	}

	public void fail0( int errorCode , String s ) {
		fail( errorCode , s , null );
	}

	public void fail1( int errorCode , String s , String param1 ) {
		fail( errorCode , s , new String[] { param1 } );
	}

	public void fail2( int errorCode , String s , String param1 , String param2 ) {
		fail( errorCode , s , new String[] { param1 , param2 } );
	}

	public void fail3( int errorCode , String s , String param1 , String param2 , String param3 ) {
		fail( errorCode , s , new String[] { param1 , param2 , param3 } );
	}

	public void fail4( int errorCode , String s , String param1 , String param2 , String param3 , String param4 ) {
		fail( errorCode , s , new String[] { param1 , param2 , param3 , param4 } );
	}

	public void exit( int errorCode , String s , String[] params ) throws Exception {
		throw new RunError( errorCode , s , params );
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

	public void handle( Throwable e , String s ) {
		if( e.getClass() == RunError.class )
			setFailed( ( RunError )e );
		else
			setFailed( new RunError( _Error.InternalActionError1 , "Internal transaction error: " + s , new String[] { s } ) );
	}
	
	public void handle( Throwable e , int errorCode , String s , String[] params ) throws Exception {
		setFailed( new RunError( e , errorCode , s , params ) );
		throw progressError;
	}

	public void handle0( Throwable e , int errorCode , String s ) throws Exception {
		handle( e , errorCode , s , null );
	}

	public void handle1( Throwable e , int errorCode , String s , String param1 ) throws Exception {
		handle( e , errorCode , s , new String[] { param1 } );
	}

	public void handle2( Throwable e , int errorCode , String s , String param1 , String param2 ) throws Exception {
		handle( e , errorCode , s , new String[] { param1 , param2 } );
	}

	public void handle3( Throwable e , int errorCode , String s , String param1 , String param2 , String param3 ) throws Exception {
		handle( e , errorCode , s , new String[] { param1 , param2 , param3 } );
	}

	public void handle4( Throwable e , int errorCode , String s , String param1 , String param2 , String param3 , String param4 ) throws Exception {
		handle( e , errorCode , s , new String[] { param1 , param2 , param3 , param4 } );
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

