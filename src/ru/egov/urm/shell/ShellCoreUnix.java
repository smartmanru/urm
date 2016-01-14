package ru.egov.urm.shell;

import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.run.ActionBase;

public class ShellCoreUnix extends ShellCore {

	public ShellCoreUnix( ShellExecutor executor , int commandTimeoutDefault , VarOSTYPE osType ) {
		super( executor , commandTimeoutDefault , osType );
	}

	@Override protected String getExportCmd( ActionBase action ) throws Exception {
		if( action.meta == null || action.meta.product == null )
			return( "" );
		
		Map<String,String> exports = action.meta.product.getExportProperties( action );
		String cmd = "";
		for( String key : exports.keySet() ) {
			if( !cmd.isEmpty() )
				cmd += "; ";
			cmd += "export " + key + "=" + exports.get( key );
		}
		return( cmd );
	}

	@Override protected void getProcessAttributes( ActionBase action ) throws Exception {
		processId = runCommandGetValueCheckDebug( action , "echo $$" );
		homePath = runCommandGetValueCheckDebug( action , "echo $HOME" );
	}
	
	@Override public void runCommand( ActionBase action , String cmd , boolean debug ) throws Exception {
		if( !running )
			exitError( action , "attempt to run command in closed session: " + cmd );
			
		cmdCurrent = cmd;

		cmdout.clear();
		cmderr.clear();
		
		String execLine = cmd + "; echo " + finishMarker + " >&2; echo " + finishMarker + "\n";
		action.trace( executor.name + " execute: " + cmd );
		writer.write( execLine );
		try {
			writer.flush();
		}
		catch( Throwable e ) {
		}
		
		ShellWaiter waiter = new ShellWaiter( executor , new CommandReader( debug ) );
		boolean res = waiter.wait( action , commandTimeout );
		commandTimeout = commandTimeoutDefault;
		
		if( !res )
			exitError( action , "command has been killed" );
	}

	@Override public int runCommandGetStatus( ActionBase action , String cmd , boolean debug ) throws Exception {
		runCommand( action , cmd + "; echo COMMAND_STATUS=$?" , debug );
		
		if( cmdout.size() > 0 ) {
			String last = cmdout.get( cmdout.size() - 1 );
			if( last.startsWith( "COMMAND_STATUS=" ) ) {
				String ss = last.substring( "COMMAND_STATUS=".length() );
				int value = Integer.parseInt( ss );
				cmdout.remove( cmdout.size() - 1 );
				return( value );
			}
		}
				
		exitError( action , "unable to obtain command status" );
		return( -1 );
	}

	@Override public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception {
		return( "( if [ -d " + Common.getQuoted( dir ) + " ]; then cd " + dir + "; " + cmd + "; else echo invalid directory: " + dir + " >&2; fi )" );
	}
	
	@Override public String getDirCmdIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		return( "( if [ -d " + Common.getQuoted( dir ) + " ]; then cd " + dir + "; " + cmd + "; fi )" );
	}

	@Override protected void killProcess( ActionBase action ) throws Exception {
		executor.pool.master.custom( action , "pkill -9 -P " + processId );
	}
	
}
