package org.urm.engine.shell;

import java.io.InputStream;
import java.io.OutputStream;

import org.urm.action.ActionBase;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerHostAccount;
import org.urm.meta.engine.ServerInfrastructure;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class ShellJssh {

	ShellProcess process;
	Account account;
	
	public JSch jsch;
	public Session jsession;
	public Channel jchannel;
	public OutputStream jstdin;
	public InputStream jstdout;
	public InputStream jstderr;

	public ShellJssh( ShellProcess process ) {
		this.process = process;
		jsch = new JSch();
	}

	public void startJssh( ActionBase action , String rootPath ) throws Exception {
		Account account = process.shell.account;
		startJssh( action , account );
		action.debug( "jssh shell=" + process.shell.name + " - successfully connected" );		
	}

	public void startJssh( ActionBase action , Account account ) throws Exception {
		startJsshSession( action , account );
		startJsshCommandChannel( action );
	}
	
	public void startJsshSession( ActionBase action , Account account ) throws Exception {
		this.account = account;
		
		String hostLogin = account.getHostLogin();
		ServerInfrastructure infra = action.getServerInfrastructure();
		ServerHostAccount hostAccount = infra.getFinalAccount( action , hostLogin );
		if( hostAccount.AUTHRES.isEmpty() )
			action.exit1( _Error.MissingAuthKey1 , "Missing auth resource to login to " + hostLogin , hostLogin );
		
		ServerAuthResource res = action.getResource( hostAccount.AUTHRES );
		res.loadAuthData( action );
		
		jsession = jsch.getSession( account.USER , account.HOST , account.PORT );
		
		String keyFile = action.context.CTX_KEYNAME;
		if( keyFile.isEmpty() ) {
			if( action.context.env != null ) {
				keyFile = action.context.env.KEYFILE;
				if( !keyFile.isEmpty() )
					action.trace( "using key file from environment settings: " + keyFile );
			}
		}
		else {
			action.trace( "using key file from parameter: " + keyFile );
		}
		
		if( !keyFile.isEmpty() )
			jsch.addIdentity( action.context.CTX_KEYNAME );
		else {		
			if( res.ac.isCommon() ) {
				String password = res.ac.getPassword( action );
				if( password.isEmpty() )
					action.exit1( _Error.MissingAuthPasswordData1 , "Missing password data of auth resource: " + res.NAME , res.NAME );
				
				action.trace( "using password from resource=" + res.NAME );
				jsession.setPassword( password );
			}
			else
			if( res.ac.isSshKey() ) {
				if( res.ac.PRIVATEKEY.isEmpty() )
					action.exit1( _Error.MissingAuthKeyData1 , "Missing key data of auth resource: " + res.NAME , res.NAME );
				
				action.trace( "using key pair from resource=" + res.NAME );
				jsch.addIdentity( "main" , res.ac.PRIVATEKEY.getBytes() , res.ac.PUBLICKEY.getBytes() , null );
			}
			else
				action.exit1( _Error.InvalidAuthData1 , "Invalid data of auth resource: " + res.NAME , res.NAME );
		}
		
		jsession.setConfig( "StrictHostKeyChecking" , "no" );
		jsession.connect( 30000 );
	}
	
	public void startJsshCommandChannel( ActionBase action ) throws Exception {
		ChannelShell channel = ( ChannelShell )jsession.openChannel( "shell" );
		channel.setPty( false );
		jchannel = channel;
		jstdin = jchannel.getOutputStream();
		jstdout = jchannel.getInputStream();
		jstderr = jchannel.getExtInputStream();
		jchannel.connect();
	}

	public OutputStream getOutputStream() throws Exception {
		if( jchannel != null )
			return( jstdin );
		return( null );
	}
	
	public InputStream getErrorStream() throws Exception {
		if( jchannel != null )
			return( jstderr );
		return( null );
	}
	
	public InputStream getInputStream() throws Exception {
		if( jchannel != null )
			return( jstdout );
		return( null );
	}

	public void kill( ActionBase action ) throws Exception {
		if( jsch != null ) {
			jstdin = null;
			jstdout = null;
			jstderr = null;
			jchannel.disconnect();
			jsession.disconnect();
			jchannel = null;
			jsession = null;
		}
	}

	public void scpFilesRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
	}

	public void scpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception {
	}

	public void scpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
	}

	public void scpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception {
	}

	public void scpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
	}

	public void scpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
	}
	
}
