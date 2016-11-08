package org.urm.engine.shell;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.ServerCall;
import org.urm.engine.action.CommandOutput;

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
		return( executor.createProcess( action , this ) );
	}
	
	public boolean createLocalLinuxProcess( ActionBase action ) throws Exception {
		builder = new ProcessBuilder( "sh" );
		return( executor.createProcess( action , this ) );
	}

	public boolean createRemoteWindowsProcessFromLinux( ActionBase action ) throws Exception {
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "create local sh process on behalf of " + shell.account.getPrintName() );
		builder = new ProcessBuilder( "sh" );
		return( executor.createProcess( action , this ) );
	}

	public boolean createRemoteLinuxProcessFromWindows( ActionBase action ) throws Exception {
		jssh = new ShellJssh( this , false );
		return( executor.createProcess( action , this ) );
	}
	
	public boolean createRemoteLinuxProcessFromWindowsOld( ActionBase action ) throws Exception {
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "create process - plink " + shell.account.getPrintName() );
		
		String keyFile = action.context.CTX_KEYNAME;
		String cmd = "plink -P " + shell.account.PORT;
		if( !keyFile.isEmpty() )
			cmd += " -i " + keyFile;
		
		cmd += " " + shell.account.getHostLogin();
		builder = new ProcessBuilder( Common.createList( Common.splitSpaced( cmd ) ) );
		return( executor.createProcess( action , this ) );
	}

	public boolean createRemoteLinuxProcessFromLinux( ActionBase action ) throws Exception {
		jssh = new ShellJssh( this , false );
		return( executor.createProcess( action , this ) );
	}
	
	public boolean createRemoteLinuxProcessFromLinuxOld( ActionBase action ) throws Exception {
		String keyFile = action.context.CTX_KEYNAME;
		Account account = shell.account;
		if( !keyFile.isEmpty() ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "create process - ssh -T " + account.getSshAddr() + " -i " + keyFile );
			if( account.PORT == 22 )
				builder = new ProcessBuilder( "ssh" , "-T" , account.getHostLogin() , "-i" , keyFile );
			else
				builder = new ProcessBuilder( "ssh" , "-T" , "-p" , "" + account.PORT , account.getHostLogin() , "-i" , keyFile );
		}
		else {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "create process - ssh -T " + account.getSshAddr() );
			if( account.PORT == 22 )
				builder = new ProcessBuilder( "ssh" , "-T" , "-o" , "PasswordAuthentication=no" , account.getSshAddr() );
			else
				builder = new ProcessBuilder( "ssh" , "-T" , "-p" , "" + account.PORT , "-o" , "PasswordAuthentication=no" , account.getSshAddr() );
		}
		return( executor.createProcess( action , this ) );
	}
	
	public void start( ActionBase action , String rootPath ) throws Exception {
		if( builder != null )
			startBuilder( action , rootPath );
		else
		if( jssh != null )
			jssh.startJssh( action , rootPath );
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
			master.custom( action , "kill -9 " + processId , CommandOutput.LOGLEVEL_TRACE );
		else
			master.custom( action , "taskkill /T /pid " + processId + " /f" , CommandOutput.LOGLEVEL_TRACE );	
	}

	public void destroy( ActionBase action ) throws Exception {
		if( jssh != null ) {
			jssh.kill( action );
			return;
		}
		
		process.destroy();
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
		shell.startProcess( action , this , null , false );
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
		shell.startProcess( action , this , null , false );
	}
	
	public void runRemoteInteractiveSshLinux( ActionBase action , String KEY ) throws Exception {
		jssh = new ShellJssh( this , true );
		ServerCall call = action.context.call;
		executeRemoteInteractive( action , call );
	}
	
	public void runRemoteInteractiveSshLinuxOld( ActionBase action , String KEY ) throws Exception {
		Account account = shell.account;
		String cmd = "ssh -T " + account.getSshAddr();
		if( !KEY.isEmpty() )
			cmd += " -i " + KEY;
		
		action.trace( account.getPrintName() + " execute: " + cmd );
		
		ServerCall call = action.context.call;
		builder = new ProcessBuilder( "sh" , "-c" , cmd );
		
		executeRemoteInteractive( action , call );
	}

	public void runRemoteInteractiveSshWindows( ActionBase action , String KEY ) throws Exception {
		jssh = new ShellJssh( this , true );
		ServerCall call = action.context.call;
		executeRemoteInteractive( action , call );
	}
	
	public void runRemoteInteractiveSshWindowsOld( ActionBase action , String KEY ) throws Exception {
		Account account = shell.account;
		String cmd = "plink ";
		if( !KEY.isEmpty() )
			cmd += "-i " + KEY + " ";
		if( account.PORT != 22 )
			cmd += "-P " + account.PORT + " ";
		cmd += account.USER + "@" + account.HOST;
		
		action.trace( account.getPrintName() + " execute: " + cmd );
		
		ServerCall call = action.context.call;
		builder = new ProcessBuilder( "cmd" , "/C" , cmd );
		
		executeRemoteInteractive( action , call );
	}
	
	private void executeRemoteInteractive( ActionBase action , ServerCall call ) throws Exception {
		if( builder != null )
			builder.redirectErrorStream( true );
		shell.startProcess( action , this , null , true );
		
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

	public boolean isNativeScp( Account account ) {
		if( account.isLinux() )
			return( true );
		return( false );
	}

	public void scpFilesRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( this , false );
		jsshScp.scpFilesRemoteToLocal( action , srcPath , account , dstPath );
	}

	public void scpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( this , false );
		jsshScp.scpDirContentLocalToRemote( action , srcDirPath , account , dstDir );
	}

	public void scpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( this , false );
		jsshScp.scpDirContentRemoteToLocal( action , srcPath , account , dstPath );
	}

	public void scpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( this , false );
		jsshScp.scpDirLocalToRemote( action , srcDirPath , account , baseDstDir );
	}

	public void scpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( this , false );
		jsshScp.scpDirRemoteToLocal( action , srcPath , account , dstPath );
	}

	public void scpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		ShellJssh jsshScp = new ShellJssh( this , false );
		jsshScp.scpFilesLocalToRemote( action , srcPath , account , dstPath );
	}
	
}
