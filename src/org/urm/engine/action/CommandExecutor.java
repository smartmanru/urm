package org.urm.engine.action;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunError;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethodMeta;
import org.urm.engine.ServerEngine;
import org.urm.meta.ServerLoader;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.urm.meta.engine.ServerReleaseLifecycle;
import org.urm.meta.engine.ServerReleaseLifecycles;

public abstract class CommandExecutor {

	public ServerEngine engine;
	public CommandMeta commandInfo;
		
	public Map<String,CommandAction> actionsMap = new HashMap<String,CommandAction>();
	public List<CommandAction> actionsList = new LinkedList<CommandAction>();
	
	protected abstract boolean run( ActionInit action );

	public CommandExecutor( ServerEngine engine , CommandMeta commandInfo ) {
		this.engine = engine;
		this.commandInfo = commandInfo;
	}
	
	public void defineAction( CommandAction action , String name ) throws Exception {
		actionsMap.put( name , action );
		actionsList.add( action );
		
		CommandMethodMeta method = commandInfo.getMethod( name );
		action.setMethod( method );
	}
	
	public CommandAction getAction( String action ) throws Exception {
		CommandAction commandAction = actionsMap.get( action ); 
		if( commandAction == null )
			Common.exit2( _Error.UnknownExecutorAction2 , "unknown action=" + action , commandInfo.name , action );
		return( commandAction );
	}

	public boolean runAction( ActionInit action ) {
		if( run( action ) )
			return( true );
		
		return( false );
	}
	
	public boolean runMethod( ActionInit action , CommandAction method ) {
		try {
			action.debug( "execute " + method.getClass().getSimpleName() + " ..." );
			action.debug( "context: " + action.context.getInfo() );
			method.run( action );
		}
		catch( Throwable e ) {
			action.fail1( _Error.ActionException1 , "Exception in method=" + method.method.name + ": " + e.getMessage() , method.method.name );
			String trace = System.getenv( "TRACE" );
			if( trace != null && trace.equals( "yes" ) )
				e.printStackTrace();
			else {
				RunError ex = Common.getExitException( e );
				if( ex == null || action.context.CTX_SHOWALL )
					action.handle( e );
				else
					action.error( "exception: " + ex.getMessage() );
			}
				
			return( false );
		}
		
		return( true );
	}
	
	public void setActionContext( ActionInit action , CommandContext context ) throws Exception {
		action.setContext( context );
		
		// load initial properties
		action.setLogLevel( context.logLevelLimit );
		
		// create shell pool
		action.setTimeout( context.CTX_TIMEOUT );
	}
	
	public void checkRequired( ActionBase action , String value , String name ) throws Exception {
		if( value == null || value.isEmpty() )
			action.exit1( _Error.NameUndefined1 , name + " is undefined. Exiting" , name );
	}
	
	public String[] getArgList( ActionBase action , int pos ) throws Exception {
		return( action.context.options.getArgList( pos ) );
	}
	
	public void checkNoArgs( ActionBase action , int pos ) throws Exception {
		String[] args = getArgList( action , pos );
		if( pos >= args.length )
			return;
		
		String xargs = Common.getList( args );
		action.exit1( _Error.UnexpectedExtraArguments1 , "unexpected extra arguments: " + Common.getQuoted( xargs ) + "; see help to find syntax" , xargs );
	}
	
	public VarCATEGORY getRequiredCategoryArg( ActionBase action , int pos ) throws Exception {
		VarCATEGORY CATEGORY = getCategoryArg( action , pos );
		if( CATEGORY == null )
			action.exit1( _Error.ArgumentRequired1 , "CATEGORY argument is required" , "CATEGORY" );
		return( CATEGORY );
	}
	
	public VarBUILDMODE getRequiredBuildModeArg( ActionBase action , int pos ) throws Exception {
		String value = getRequiredArg( action , pos , "BUILDMODE" );
		VarBUILDMODE BUILDMODE = null;
		for( VarBUILDMODE x : VarBUILDMODE.values() ) {
			if( value.equals( Common.getEnumLower( x ) ) ) {
				BUILDMODE = x;
				break;
			}
		}
				
		if( BUILDMODE == null )
			action.exit1( _Error.UnknownBuildMode1 , "unknown buildMode=" + value , value );
		return( BUILDMODE );
	}
	
	public VarCATEGORY getCategoryArg( ActionBase action , int pos ) throws Exception {
		if( pos >= action.context.options.getArgCount() )
			return( null );
		
		return( Types.getCategory( getArg( action , pos ) , true ) );
	}
	
	public String getRequiredArg( ActionBase action , int pos , String argName ) throws Exception {
		String value = getArg( action , pos );
		if( value.isEmpty() )
			action.exit1( _Error.ArgumentRequired1 , argName + " is empty" , argName );
		
		return( value );
	}

	public Date getRequiredDateArg( ActionBase action , int pos , String argName ) throws Exception {
		String value = getArg( action , pos );
		if( value.isEmpty() )
			action.exit1( _Error.ArgumentRequired1 , argName + " is empty" , argName );
		
		return( Common.getDateValue( value ) );
	}

	public String getArg( ActionBase action , int pos ) throws Exception {
		return( action.context.options.getArg( pos ) );
	}

	public int getIntArg( ActionBase action , int pos , int defValue ) {
		return( action.context.options.getIntArg( pos , defValue ) );
	}
	
	public Date getDateArg( ActionBase action , int pos ) {
		return( action.context.options.getDateArg( pos ) );
	}

	public ServerReleaseLifecycle getLifecycleArg( ActionBase action , int pos ) throws Exception {
		String value = getArg( action , pos );
		if( value.isEmpty() )
			return( null );
		
		ServerLoader loader = engine.getLoader( action.actionInit );
		ServerReleaseLifecycles lifecycles = loader.getReleaseLifecycles();
		return( lifecycles.getLifecycle( action , value ) );
	}
	
}
