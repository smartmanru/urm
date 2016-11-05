package org.urm.engine.shell;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerHostAccount;
import org.urm.meta.engine.ServerInfrastructure;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
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

	public void startJsshScpChannel( ActionBase action ) throws Exception {
		ChannelSftp channel = ( ChannelSftp )jsession.openChannel( "sftp" );
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
			if( jchannel != null )
				jchannel.disconnect();
			if( jsession != null )
				jsession.disconnect();
			jchannel = null;
			jsession = null;
		}
	}

	public boolean scpFilesRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		scpConnect( action , account );
		
		boolean res = false;
		try {
			String srcDir = Common.getDirName( srcPath );
			RemoteFolder srcDirFolder = action.getRemoteFolder( account , srcDir );
			String srcNames = Common.getBaseName( srcPath );
			boolean isMask = Common.isBasenameMask( srcNames );

			String[] maskFiles = null;
			if( isMask ) {
				maskFiles = srcDirFolder.findFiles( action , srcNames );
				if( maskFiles.length == 0 ) {
					action.trace( "scp: missing source files, path=" + srcPath );
					res = true;
				}
				else {
					res = true;
					for( String file : maskFiles ) {
						if( !executeScpNameRemoteToLocal( action , srcDirFolder , file , dstPath ) )
							res = false;
					}
				}
			}
			else {
				res = executeScpNameRemoteToLocal( action , srcDirFolder , srcNames , dstPath );
			}
	    }
		finally {
			kill( action );
		}
		
		return( res );
	}

	public void scpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception {
		scpConnect( action , account );
	}

	public void scpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		scpConnect( action , account );
	}

	public void scpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception {
		scpConnect( action , account );
	}

	public void scpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		scpConnect( action , account );
	}

	public void scpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		scpConnect( action , account );
	}

	private void scpConnect( ActionBase action , Account account ) throws Exception {
		startJsshSession( action , account );
		startJsshScpChannel( action );
	}

	private boolean executeScpNameRemoteToLocal( ActionBase action , RemoteFolder srcDirFolder , String srcName , String dstPath ) throws Exception {
		String dstDir = Common.getDirName( dstPath );
		File fileDst = new File( action.getLocalPath( dstPath ) );
		File fileDstDir = new File( action.getLocalPath( dstDir ) );
		
		boolean res = false;
		if( srcDirFolder.checkFileExists( action , srcName ) ) {
			if( fileDst.isDirectory() ) {
				executeScpFileRemoteToLocal( action , srcDirFolder.getFilePath( action , srcName ) , Common.getPath( dstPath , srcName ) );
				res = true;
			}
			else {
				if( fileDstDir.isDirectory() ) {
					executeScpFileRemoteToLocal( action , srcDirFolder.getFilePath( action , srcName ) , dstPath );
					res = true;
				}
				else {
					action.exit1( _Error.ScpMissingDestinationDirectory1 , "scp: missing destination directory=" + dstDir , dstDir );
				}
			}
		}
		
		if( srcDirFolder.checkFolderExists( action , srcName ) ) {
			if( !fileDstDir.isDirectory() ) {
				action.exit1( _Error.ScpMissingDestinationDirectory1 , "scp: missing destination directory=" + dstDir , dstDir );
			}
			else {
				if( fileDst.isFile() ) {
					action.exit1( _Error.ScpDestinationCannotBeFile1 , "scp: cannot copy directory to a file: " + dstPath , dstPath );
				}
				else {
					if( fileDst.isDirectory() ) {
						LocalFolder folder = action.getLocalFolder( dstPath );
						LocalFolder folderDst = folder.getSubFolder( action , srcName ); 
						folderDst.ensureExists( action );
						executeScpDirContentRemoteToLocal( action , srcDirFolder.getSubFolder( action , srcName ) , folderDst );
						res = true;
					}
					else {
						LocalFolder folder = action.getLocalFolder( dstPath );
						folder.ensureExists( action );
						executeScpDirContentRemoteToLocal( action , srcDirFolder.getSubFolder( action , srcName ) , folder );
						res = true;
					}
				}
			}
		}
		else
			action.trace( "scp: missing source files, path=" + srcDirFolder.getFilePath( action , srcName ) );
		
		return( res );
	}
	
	private void executeScpFileRemoteToLocal( ActionBase action , String remotePath , String dstPath ) throws Exception {
		ChannelSftp channel = ( ChannelSftp )jchannel; 		
		InputStream out = channel.get( remotePath );

		File localFile = new File( action.getLocalPath( dstPath ) );
		byte[] buffer = new byte[1024];
		BufferedInputStream bis = new BufferedInputStream( out );
		OutputStream os = new FileOutputStream( localFile );
		BufferedOutputStream bos = new BufferedOutputStream( os );
		int readCount;
		while( ( readCount = bis.read( buffer ) ) > 0 )
			bos.write( buffer , 0 , readCount );
		bis.close();
		bos.close();
	}

	private void executeScpDirContentRemoteToLocal( ActionBase action , RemoteFolder srcDirFolder , LocalFolder dstFolder ) throws Exception {
		List<String> topDirs = new LinkedList<String>();
		List<String> topFiles = new LinkedList<String>();
		srcDirFolder.getTopDirsAndFiles( action , topDirs , topFiles );
		for( String dir : topDirs ) {
			LocalFolder child = dstFolder.getSubFolder( action , dir );
			child.ensureExists( action );
			executeScpDirContentRemoteToLocal( action , srcDirFolder.getSubFolder( action , dir ) , child );
		}
		
		for( String file : topFiles ) {
			String srcFile = srcDirFolder.getFilePath( action , file );
			String dstFile = dstFolder.getFilePath( action , file );
			executeScpFileRemoteToLocal( action , srcFile , dstFile );
		}
	}
	
}
