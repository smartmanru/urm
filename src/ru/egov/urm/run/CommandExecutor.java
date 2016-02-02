package ru.egov.urm.run;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.ExitException;
import ru.egov.urm.meta.Metadata;

public abstract class CommandExecutor {

	public CommandAction commandAction;
	public CommandBuilder builder;
	public Metadata meta;
	public boolean manualActions;
	public CommandOptions options;
		
	Map<String,CommandAction> actionsMap = new HashMap<String,CommandAction>();
	List<CommandAction> actionsList = new LinkedList<CommandAction>();
	public boolean executorFailed;
	
	public abstract boolean run( ActionInit action );
	public boolean setManualOptions( CommandOptions options ) { return( false ); };
	
	public CommandExecutor( CommandBuilder builder ) {
		this.builder = builder;
		this.manualActions = false;
		this.options = new CommandOptions();
		this.executorFailed = false; 
	}
	
	public CommandOptions getOptions() { 
		return( options );
	};
	
	public boolean isFailed() {
		return( executorFailed );
	}
	
	protected void setFailed() {
		executorFailed = true;
	}
	
	public boolean isOK() {
		return( ( executorFailed )? false : true );
	}
	
	public void print( String s ) {
		System.out.println( s );
	}
	
	public void printhelp( String s ) {
		print( "# " + s );
	}
	
	public void defineAction( CommandAction action ) {
		actionsMap.put( action.name , action );
		actionsList.add( action );
	}
	
	public CommandAction getAction( String action ) {
		return( actionsMap.get( action ) ); 
	}
	
	public void showTopHelp() {
		printhelp( "URM HELP" );
		printhelp( "Available actions are:" );
		for( CommandAction action : actionsList ) {
			String spacing = Common.replicate( " " , 50 - action.name.length() ); 
			printhelp( "\t" + action.name + spacing + action.help );
		}
		
		printhelp( "" );
		
		CommandOptions options = getOptions();
		options.showTopHelp( this );
	}

	public void showActionHelp( CommandAction action ) {
		printhelp( "URM HELP" );
		
		printhelp( "Action: " + action.name );
		printhelp( "Function: " + action.help );
		printhelp( "Syntax: " + action.syntax );
		printhelp( "" );
		
		// show action options
		CommandOptions options = getOptions();
		options.showActionOptionsHelp( action );
	}
	
	private boolean checkValidOptions( CommandAction commandAction ) {
		List<CommandVar> opts = options.getOptionsSet();
		for( CommandVar var : opts ) {
			if( !commandAction.isOptionApplicable( var ) ) {
				print( "option " + var.varName + " is not applicable for action " + commandAction.name );
				return( false );
			}
		}

		// check defined options
		for( String varUsed : commandAction.vars ) {
			if( !options.isValidVar( varUsed ) ) {
				print( "unknown command var=" + varUsed );
				return( false );
			}
		}
		
		return( true );
	}
	
	public boolean setAction( String actionName , String firstArg ) {
		// check action
		if( options.command.isEmpty() || actionName.isEmpty() || actionName.equals( "help" ) ) {
			if( !firstArg.isEmpty() ) {
				commandAction = actionsMap.get( firstArg );
				if( commandAction == null ) {
					print( "unknown action=" + firstArg );
					return( false );
				}
				
				showActionHelp( commandAction );
			}
			else
				showTopHelp();
			
			return( false );
		}

		commandAction = actionsMap.get( actionName );
		if( commandAction == null ) {
			print( "unknown action=" + actionName );
			return( false );
		}

		if( firstArg.equals( "help" ) ) {
			showActionHelp( commandAction );
			return( false );
		}
		
		if( !checkValidOptions( commandAction ) )
			return( false );
		
		options.action = commandAction.name;
		
		return( true );
	}
	
	public boolean setOptions( CommandOptions options ) throws Exception {
		this.options = options;
		
		if( manualActions ) {
			if( !setManualOptions( options ) )
				return( false );
			
			return( true );
		}
		
		String actionName = options.action;
		String firstArg = options.getArg( 0 );
		if( !setAction( actionName , firstArg ) )
			return( false );
		
		return( true );
	}

	public boolean runMethod( ActionInit action , CommandAction method ) {
		try {
			action.debug( "execute " + method.getClass().getSimpleName() + " ..." );
			action.context.logDebug( action );
			method.run( action );
		}
		catch( Throwable e ) {
			ExitException ex = Common.getExitException( e );
			if( ex == null || action.context.CTX_SHOWALL )
				action.log( e );
			else
				action.log( ex.getMessage() );
				
			return( false );
		}
		
		return( true );
	}
	
	public ActionInit prepare( CommandContext context , Metadata meta ) throws Exception {
		this.meta = meta;
		
		// create output
		CommandOptions options = getOptions();
		
		// start local shell
		CommandOutput output = new CommandOutput();
		ActionInit action = new ActionInit( this , context , options , output , meta );
		
		// scatter into variables
		options.updateProperties( action );
		output.setOptions( context.CTX_SHOWALL , context.CTX_TRACE );
		
		// print
		if( context.CTX_SHOWALL )
			options.printRunningOptions();
		
		// load product properties
		meta.loadProduct( action );

		// create shell pool
		context.createPool( action );
		
		// create work folder
		action.createWorkFolder();
		
		return( action );
	}
	
	public void finish( ActionBase action ) throws Exception {
		action.finish();
	}
	
	public void checkRequired( ActionBase action , String value , String name ) throws Exception {
		if( value == null || value.isEmpty() )
			exit( action , name + " is undefined. Exiting" );
	}
	
	public void exit( ActionBase action , String message ) throws Exception {
		action.exit( message );
	}
	
	public boolean isOptionApplicaple( CommandVar var ) {
		for( CommandAction action : actionsMap.values() )
			if( action.isOptionApplicable( var ) )
				return( true );
		return( false );
	}
}
