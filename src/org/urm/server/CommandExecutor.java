package org.urm.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ExitException;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethod;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionInit;
import org.urm.server.action.CommandAction;
import org.urm.server.action.CommandContext;
import org.urm.server.action.CommandOutput;
import org.urm.server.meta.Metadata;
import org.urm.server.meta.Metadata.VarBUILDMODE;
import org.urm.server.meta.Metadata.VarCATEGORY;

public abstract class CommandExecutor {

	public CommandMeta commandInfo;
		
	public Map<String,CommandAction> actionsMap = new HashMap<String,CommandAction>();
	public List<CommandAction> actionsList = new LinkedList<CommandAction>();
	
	public abstract boolean run( ActionInit action );
	
	public CommandExecutor( CommandMeta commandInfo ) {
		this.commandInfo = commandInfo;
	}
	
	public void defineAction( CommandAction action , String name ) throws Exception {
		actionsMap.put( name , action );
		actionsList.add( action );
		
		CommandMethod method = commandInfo.getAction( name );
		action.setMethod( method );
	}
	
	public CommandAction getAction( String action ) {
		return( actionsMap.get( action ) ); 
	}

	public boolean runMethod( ActionInit action , CommandAction method ) {
		try {
			action.debug( "execute " + method.getClass().getSimpleName() + " ..." );
			action.context.logDebug( action );
			method.run( action );
		}
		catch( Throwable e ) {
			String trace = System.getenv( "TRACE" );
			if( trace != null && trace.equals( "yes" ) )
				e.printStackTrace();
			else {
				ExitException ex = Common.getExitException( e );
				if( ex == null || action.context.CTX_SHOWALL )
					action.log( e );
				else
					action.error( "exception: " + ex.getMessage() );
			}
				
			return( false );
		}
		
		return( true );
	}
	
	public ActionInit prepare( CommandContext context , Metadata meta , String actionName ) throws Exception {
		// start local shell
		CommandOutput output = new CommandOutput();
		CommandAction commandAction = actionsMap.get( actionName );
		ActionInit action = new ActionInit( this , context , output , meta , commandAction , actionName );
		
		// load product properties
		context.update( action );
		
		// create shell pool
		context.createPool( action );
		
		// create work folder
		action.createWorkFolder();
		action.tee();
		
		// print
		if( context.CTX_SHOWALL ) {
			String info = context.options.getRunningOptions();
			action.commentExecutor( info );
		}
		
		return( action );
	}
	
	public void finish( ActionBase action ) throws Exception {
		action.stopAllOutputs();
		action.deleteWorkFolder();
	}
	
	public void checkRequired( ActionBase action , String value , String name ) throws Exception {
		if( value == null || value.isEmpty() )
			exit( action , name + " is undefined. Exiting" );
	}
	
	public void exit( ActionBase action , String message ) throws Exception {
		action.exit( message );
	}

	public String[] getArgList( ActionBase action , int pos ) throws Exception {
		return( action.context.options.getArgList( pos ) );
	}
	
	public void checkNoArgs( ActionBase action , int pos ) throws Exception {
		String[] args = getArgList( action , pos );
		if( pos >= args.length )
			return;
		
		action.exit( "unexpected extra arguments: " + Common.getQuoted( Common.getList( args ) ) + "; see help to find syntax" );
	}
	
	public VarCATEGORY getRequiredCategoryArg( ActionBase action , int pos ) throws Exception {
		VarCATEGORY CATEGORY = getCategoryArg( action , pos );
		if( CATEGORY == null )
			action.exit( "CATEGORY argument is required" );
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
			action.exit( "unknown buildMode=" + value );
		return( BUILDMODE );
	}
	
	public VarCATEGORY getCategoryArg( ActionBase action , int pos ) throws Exception {
		if( pos >= action.context.options.getArgCount() )
			return( null );
		
		return( action.meta.getCategory( action , getArg( action , pos ) ) );
	}
	
	public String getRequiredArg( ActionBase action , int pos , String argName ) throws Exception {
		String value = getArg( action , pos );
		if( value.isEmpty() )
			action.exit( argName + " is empty" );
		
		return( value );
	}

	public String getArg( ActionBase action , int pos ) throws Exception {
		return( action.context.options.getArg( pos ) );
	}

	public int getIntArg( ActionBase action , int pos , int defValue ) {
		return( action.context.options.getIntArg( pos , defValue ) );
	}
	
}
