package org.urm.engine.shell;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.ServerCall;
import org.urm.engine.action.CommandOutput;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerHostAccount;
import org.urm.meta.engine.ServerInfrastructure;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class ShellProcess {

	Shell shell;
	ShellExecutor executor;
	ProcessBuilder builder;
	JSch jsch;
	Session jsession;
	Channel jchannel;
	OutputStream jstdin;
	InputStream jstdout;
	InputStream jstderr;

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
			action.trace( "create local sh process on behalf of " + executor.account.getPrintName() );
		builder = new ProcessBuilder( "sh" );
		return( executor.createProcess( action , this ) );
	}

	public boolean createRemoteLinuxProcessFromWindows( ActionBase action ) throws Exception {
		jsch = new JSch();
		return( executor.createProcess( action , this ) );
	}
	
	public boolean createRemoteLinuxProcessFromWindowsOld( ActionBase action ) throws Exception {
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "create process - plink " + executor.account.getPrintName() );
		
		String keyFile = action.context.CTX_KEYNAME;
		String cmd = "plink -P " + executor.account.PORT;
		if( !keyFile.isEmpty() )
			cmd += " -i " + keyFile;
		
		cmd += " " + executor.account.getHostLogin();
		builder = new ProcessBuilder( Common.createList( Common.splitSpaced( cmd ) ) );
		return( executor.createProcess( action , this ) );
	}

	public boolean createRemoteLinuxProcessFromLinux( ActionBase action ) throws Exception {
		jsch = new JSch();
		return( executor.createProcess( action , this ) );
	}
	
	public boolean createRemoteLinuxProcessFromLinuxOld( ActionBase action ) throws Exception {
		String keyFile = action.context.CTX_KEYNAME;
		Account account = executor.account;
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
		if( jsch != null )
			startJssh( action , rootPath );
	}
		
	public void startBuilder( ActionBase action , String rootPath ) throws Exception {
		if( rootPath != null )
			builder.directory( new File( rootPath ) );
		
		// start OS process
		process = builder.start();
		
		// get process ID
		ShellCoreJNI osapi = executor.pool.getOSAPI();
		if( action.isLocalLinux() )
			processId = osapi.getLinuxProcessId( action , process );
		else
			processId = osapi.getWindowsProcessId( action , process );
		action.debug( "process started: name=" + executor.name + ", id=" + processId );
	}

	public void startJssh( ActionBase action , String rootPath ) throws Exception {
		String hostLogin = shell.account.getHostLogin();
		ServerInfrastructure infra = action.getServerInfrastructure();
		ServerHostAccount account = infra.getFinalAccount( action , hostLogin );
		if( account.AUTHRES.isEmpty() )
			action.exit1( _Error.MissingAuthKey1 , "Missing auth resource to login to " + hostLogin , hostLogin );
		
		ServerAuthResource res = action.getResource( account.AUTHRES );
		res.loadAuthData();
		
		jsession = jsch.getSession( shell.account.USER , shell.account.HOST , shell.account.PORT );
		
		String keyFile = action.context.CTX_KEYNAME;
		if( keyFile.isEmpty() ) {
			if( action.context.env != null )
				keyFile = action.context.env.KEYFILE;
		}
		
		if( !keyFile.isEmpty() )
			jsch.addIdentity( action.context.CTX_KEYNAME );
		else {		
			if( res.ac.isCommon() ) {
				String password = res.ac.getPassword( action );
				jsession.setPassword( password );
			}
			else {
				jsch.addIdentity( "main" , res.ac.PRIVATEKEY.getBytes() , res.ac.PUBLICKEY.getBytes() , null );
			}
		}
		
		jsession.setConfig( "StrictHostKeyChecking" , "no" );
		jsession.connect( 30000 );
		
		ChannelShell channel = ( ChannelShell )jsession.openChannel( "shell" );
		channel.setPty( false );
		jchannel = channel;
		jstdin = jchannel.getOutputStream();
		jstdout = jchannel.getInputStream();
		jstderr = jchannel.getExtInputStream();
		jchannel.connect();
		action.debug( "jssh shell=" + shell.name + " - successfully connected" );		
	}
	
	public OutputStream getOutputStream() throws Exception {
		if( jchannel != null )
			return( jstdin );
		return( process.getOutputStream() );
	}
	
	public InputStream getErrorStream() throws Exception {
		if( jchannel != null )
			return( jstderr );
		return( process.getErrorStream() );
	}
	
	public InputStream getInputStream() throws Exception {
		if( jchannel != null )
			return( jstdout );
		return( process.getInputStream() );
	}

	public void setUnavailable() {
		processId = -1;
	}
	
	public void kill( ActionBase action ) throws Exception {
		if( processId < 0 )
			return;
		
		ShellExecutor master = executor.pool.master;
		if( action.isLocalLinux() )
			master.custom( action , "kill -9 " + processId , CommandOutput.LOGLEVEL_TRACE );
		else
			master.custom( action , "taskkill /T /pid " + processId + " /f" , CommandOutput.LOGLEVEL_TRACE );	
	}

	public void destroy() throws Exception {
		process.destroy();
	}

	public int waitFor( ActionBase action ) throws Exception {
		return( process.waitFor() );
	}
	
	public String prepareExecuteWindowsFromLinux( ActionBase action , String cmd ) throws Exception {
		String execLine = "ssh";
		String keyFile = action.context.CTX_KEYNAME;
		if( !keyFile.isEmpty() )
			execLine += " -i " + keyFile;
		if( executor.account.PORT != 22 )
			execLine += " -P " + executor.account.PORT;

		String cmdWin = Common.replace( cmd , "\\" , "\\\\" );
		cmdWin = Common.replace( cmdWin , "\\\\$" , "\\$" );
		execLine += " " + executor.account.getHostLogin() + " " + Common.getQuoted( "cmd /c chcp 65001 & cmd /c \"echo off & " + cmdWin + "\"" );
		action.trace( executor.name + " execute: " + cmd );
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
		builder.redirectErrorStream( true );
		shell.startProcess( action , this , null , true );
		
		int timeout = action.setTimeoutDefault();
		shell.addInput( action , "echo " + CONNECT_MARKER , true );
		if( !shell.waitForMarker( action , CONNECT_MARKER , true ) ) {
			call.connectFinished( false );
			action.exit1( _Error.UnableConnectHost1 , "unable to connect to " + shell.name , shell.name );
		}
		
		action.setTimeout( timeout );
		call.connectFinished( true );
	}

	public boolean executeCommand( ActionBase action , String input ) throws Exception {
		shell.addInput( action , input , false );
		shell.addInput( action , "echo " + COMMAND_MARKER , true );
		
		// wait for finish
		return( shell.waitForMarker( action , COMMAND_MARKER , false ) );
	}
	
}
