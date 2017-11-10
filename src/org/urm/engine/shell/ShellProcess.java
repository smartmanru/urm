package org.urm.engine.shell;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.EngineCall;
import org.urm.engine.action.CommandOutput;
import org.urm.meta.engine.AuthResource;

public class ShellProcess {

	Shell shell;
	ShellExecutor executor;
	ProcessBuilder builder;
	ShellJssh jssh;

	private Process process = null;
	private int processId = -1;
	
	public final static String CONNECT_MARKER = "URM.CONNECTED";  
	public final static String COMMAND_MARKER = "URM.COMMAND";  

	public ShellProcess( Shell shell ) {
		this.shell = shell;
		this.executor = null;
	}

	public ShellProcess( ShellExecutor executor ) {
		this.shell = executor;
		this.executor = executor;
	}

	public boolean createLocalWindowsProcess( ActionBase action ) throws Exception {
		builder = new ProcessBuilder( "cmd" , "/Q" , "/D" , "/A" , "/V:OFF" );
		return( executor.createProcess( action , this , null ) );
	}
	
	public boolean createLocalLinuxProcess( ActionBase action ) throws Exception {
		builder = new ProcessBuilder( "sh" );
		return( executor.createProcess( action , this , null ) );
	}

	public boolean createRemoteWindowsProcessFromLinux( ActionBase action , AuthResource auth ) throws Exception {
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "create local sh process on behalf of " + shell.account.getPrintName() );
		builder = new ProcessBuilder( "sh" );
		return( executor.createProcess( action , this , auth ) );
	}

	public boolean createRemoteLinuxProcessFromWindows( ActionBase action , AuthResource auth ) throws Exception {
		jssh = new ShellJssh( this , false );
		return( executor.createProcess( action , this , auth ) );
	}
	
	public boolean createRemoteLinuxProcessFromLinux( ActionBase action , AuthResource auth ) throws Exception {
		jssh = new ShellJssh( this , false );
		return( executor.createProcess( action , this , auth ) );
	}
	
	public void start( ActionBase action , String rootPath , AuthResource auth ) throws Exception {
		if( builder != null )
			startBuilder( action , rootPath );
		else
		if( jssh != null )
			jssh.startJsshProcess( action , rootPath , auth );
	}
		
	public void startBuilder( ActionBase action , String rootPath ) throws Exception {
		if( rootPath != null )
			builder.directory( new File( rootPath ) );
		
		// start OS process
		process = builder.start();
		
		// get process ID
		ShellCoreJNI osapi = shell.pool.getOSAPI();
		if( action.isLocalLinux() )
			processId = osapi.getLinuxProcessId( action , process );
		else
			processId = osapi.getWindowsProcessId( action , process );
		action.debug( "process started: name=" + shell.name + ", id=" + processId );
	}

	public OutputStream getOutputStream() throws Exception {
		if( jssh != null )
			return( jssh.getOutputStream() );
		return( process.getOutputStream() );
	}
	
	public InputStream getErrorStream() throws Exception {
		if( jssh != null )
			return( jssh.getErrorStream() );
		return( process.getErrorStream() );
	}
	
	public InputStream getInputStream() throws Exception {
		if( jssh != null )
			return( jssh.getInputStream() );
		return( process.getInputStream() );
	}

	public void setUnavailable() {
		processId = -1;
	}
	
	public void kill( ActionBase action ) throws Exception {
		if( jssh != null ) {
			jssh.kill( action );
			return;
		}
		
		if( processId < 0 )
			return;
		
		ShellExecutor master = shell.pool.master;
		if( action.isLocalLinux() )
			master.custom( action , "pkill -9 -P " + processId + "; kill -9 " + processId , CommandOutput.LOGLEVEL_TRACE );
		else
			master.custom( action , "taskkill /T /pid " + processId + " /f" , CommandOutput.LOGLEVEL_TRACE );	
	}

	public void destroy( ActionBase action ) {
		try {
			if( jssh != null ) {
				jssh.kill( action );
				return;
			}
			
			process.destroy();
		}
		catch( Throwable e ) {
			action.log( "destroy process" , e );
		}
	}

	public int waitForInteractive( ActionBase action ) throws Exception {
		if( jssh != null )
			return( jssh.waitForInteractive( action ) );
		return( process.waitFor() );
	}
	
	public String prepareExecuteWindowsFromLinux( ActionBase action , String cmd ) throws Exception {
		String execLine = "ssh";
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() )
			execLine += " -i " + keyFile;
		if( shell.account.PORT != 22 )
			execLine += " -P " + shell.account.PORT;

		String cmdWin = Common.replace( cmd , "\\" , "\\\\" );
		cmdWin = Common.replace( cmdWin , "\\\\$" , "\\$" );
		execLine += " " + shell.account.getHostLogin() + " " + Common.getQuoted( "cmd /c chcp 65001 & cmd /c \"echo off & " + cmdWin + "\"" );
		action.trace( shell.name + " execute: " + cmd );
		return( execLine );
	}
	
	public void runLocalInteractiveSshLinux( ActionBase action , String KEY ) throws Exception {
		Account account = shell.account;
		String cmd = "ssh " + account.getSshAddr();
		if( !KEY.isEmpty() )
			cmd += " -i " + KEY;
		cmd += " < /dev/tty > /dev/tty 2>&1";
		
		action.trace( account.getPrintName() + " execute: " + cmd );

		builder = new ProcessBuilder( "sh" , "-c" , cmd );
		shell.startProcess( action , this , null , false , null );
	}
	
	public void runLocalInteractiveSshWindows( ActionBase action , String KEY ) throws Exception {
		Account account = shell.account;
		String cmd = "plink -ssh ";
		if( !KEY.isEmpty() )
			cmd += "-i " + KEY + " ";
		if( account.PORT != 22 )
			cmd += "-P " + account.PORT + " ";
		cmd += account.USER + "@" + account.HOST;
		
		action.trace( account.getPrintName() + " execute: " + cmd );
		
		builder = new ProcessBuilder( "cmd" , "/C" , cmd );
		shell.startProcess( action , this , null , false , null );
	}
	
	public void runRemoteInteractiveSshLinux( ActionBase action , String KEY , AuthResource auth ) throws Exception {
		jssh = new ShellJssh( this , true );
		EngineCall call = action.context.call;
		executeRemoteInteractive( action , call , auth );
	}
	
	public void runRemoteInteractiveSshWindows( ActionBase action , String KEY , AuthResource auth ) throws Exception {
		jssh = new ShellJssh( this , true );
		EngineCall call = action.context.call;
		executeRemoteInteractive( action , call , auth );
	}
	
	private void executeRemoteInteractive( ActionBase action , EngineCall call , AuthResource auth ) throws Exception {
		if( builder != null )
			builder.redirectErrorStream( true );
		shell.startProcess( action , this , null , true , auth );
		
		int timeout = action.setTimeoutDefault();
		shell.addInput( action , "echo " + CONNECT_MARKER + "; echo " + CONNECT_MARKER + " >&2" , true );
		
		if( !shell.waitForMarker( action , CONNECT_MARKER , true ) ) {
			call.connectFinished( false );
			action.exit1( _Error.UnableConnectHost1 , "unable to connect to " + shell.name , shell.name );
		}
		
		action.setTimeout( timeout );
		call.connectFinished( true );
	}

	public boolean executeInteractiveCommand( ActionBase action , String input ) throws Exception {
		shell.addInput( action , input , false );
		shell.addInput( action , "echo " + COMMAND_MARKER + "; echo " + COMMAND_MARKER + " >&2" , true );
		
		// wait for finish
		return( shell.waitForMarker( action , COMMAND_MARKER , false ) );
	}

	public static void scpFilesRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( account );
		jsshScp.scpFilesRemoteToLocal( action , srcPath , dstPath );
	}

	public static void scpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( account );
		jsshScp.scpDirContentLocalToRemote( action , srcDirPath , dstDir );
	}

	public static void scpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( account );
		jsshScp.scpDirContentRemoteToLocal( action , srcPath , dstPath );
	}

	public static void scpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( account );
		jsshScp.scpDirLocalToRemote( action , srcDirPath , baseDstDir );
	}

	public static void scpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( account );
		jsshScp.scpDirRemoteToLocal( action , srcPath , dstPath );
	}

	public static void scpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( account );
		jsshScp.scpFilesLocalToRemote( action , srcPath , dstPath );
	}
	
}
