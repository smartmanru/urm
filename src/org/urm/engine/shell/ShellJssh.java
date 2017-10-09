package org.urm.engine.shell;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.engine.EngineAuthResource;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class ShellJssh {

	ShellProcess process;
	boolean interactive;
	Account account;
	
	public JSch jsch;
	public Session jsession;
	public Channel jchannel;
	public OutputStream jstdin;
	public InputStream jstdout;
	public InputStream jstderr;

	public ShellJssh( ShellProcess process , boolean interactive ) {
		this.process = process;
		this.interactive = interactive;
		jsch = new JSch();
	}

	public ShellJssh( Account account ) {
		this.account = account;
		this.interactive = false;
		jsch = new JSch();
	}
	
	private EngineAuthResource getAuthResource( ActionBase action , Account account ) throws Exception {
		EngineAuthResource res = account.getResource( action );
		res.loadAuthData();
		return( res );
	}
	
	public void startJsshProcess( ActionBase action , String rootPath , EngineAuthResource res ) throws Exception {
		Account account = process.shell.account;
		if( res == null )
			res = getAuthResource( action , account );
		
		action.debug( "jssh shell=" + process.shell.name + " - connecting to " + account.USER + "@" + account.HOST + ":" + account.PORT + " ..." );		
		startJsshInternal( action , account , res );
		action.debug( "jssh shell=" + process.shell.name + " - successfully connected" );		
	}

	private void startJsshInternal( ActionBase action , Account account , EngineAuthResource res ) throws Exception {
		startJsshSession( action , account , res );
		startJsshCommandChannel( action );
	}
	
	private void startJsshSession( ActionBase action , Account account , EngineAuthResource res ) throws Exception {
		String hostLogin = account.getHostLogin();
		action.debug( "connecting to account=" + hostLogin + " ..." );
		this.account = account;
		
		String ADDRESS = account.IP;
		if( ADDRESS.isEmpty() )
			ADDRESS = account.HOST;
		jsession = jsch.getSession( account.USER , ADDRESS , account.PORT );
		
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
		
		try {
			jsession.connect( 30000 );
		}
		catch( Throwable e ) {
			action.log( "ssh connect" , e );
			action.exit1( _Error.UnableConnectAccount1 , "Unable to connect to account=" + hostLogin , hostLogin );
		}
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

	public int waitForInteractive( ActionBase action ) throws Exception {
		Channel channel = jchannel;
		if( !process.shell.wc.runWaitInteractive( action ) )
			return( -100 );
		
		return( channel.getExitStatus() );
	}
	
	public boolean scpFilesRemoteToLocal( ActionBase action , String srcPath , String dstPath ) throws Exception {
		scpConnect( action );
		
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

	public boolean scpDirContentLocalToRemote( ActionBase action , String srcDirPath , String dstDir ) throws Exception {
		scpConnect( action );
		
		boolean res = false;
		try {
	    }
		finally {
			kill( action );
		}
		
		return( res );
	}

	public boolean scpDirContentRemoteToLocal( ActionBase action , String srcPath , String dstPath ) throws Exception {
		scpConnect( action );
		
		boolean res = false;
		try {
			RemoteFolder srcDirFolder = action.getRemoteFolder( account , srcPath );
			LocalFolder dstFolder = action.getLocalFolder( dstPath );
			executeScpDirContentRemoteToLocal( action , srcDirFolder , dstFolder );
	    }
		finally {
			kill( action );
		}
		
		return( res );
	}

	public boolean scpDirLocalToRemote( ActionBase action , String srcDirPath , String baseDstDir ) throws Exception {
		scpConnect( action );
		
		boolean res = false;
		try {
			LocalFolder srcDirFolder = action.getLocalFolder( srcDirPath );
			RemoteFolder dstFolder = action.getRemoteFolder( account , baseDstDir );
			executeScpDirContentLocalToRemote( action , srcDirFolder , dstFolder );
	    }
		finally {
			kill( action );
		}
		
		return( res );
	}

	public boolean scpDirRemoteToLocal( ActionBase action , String srcPath , String dstPath ) throws Exception {
		scpConnect( action );
		
		boolean res = false;
		try {
			RemoteFolder srcDirFolder = action.getRemoteFolder( account , srcPath );
			LocalFolder dstFolder = action.getLocalFolder( dstPath );
			LocalFolder dstFolderDir = dstFolder.getParentFolder( action );
			if( dstFolderDir.checkPathExists( action , dstFolder.folderName ) )
				action.exit1( _Error.ScpDestinationAlreadyExists1 , "Destination already exists: " + dstPath , dstPath );
			
			dstFolder.ensureExists( action );
			executeScpDirContentRemoteToLocal( action , srcDirFolder , dstFolder );
	    }
		finally {
			kill( action );
		}
		
		return( res );
	}

	public boolean scpFilesLocalToRemote( ActionBase action , String srcPath , String dstPath ) throws Exception {
		scpConnect( action );
		
		boolean res = false;
		try {
			String srcDir = Common.getDirName( srcPath );
			LocalFolder srcDirFolder = action.getLocalFolder( srcDir );
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
						if( !executeScpNameLocalToRemote( action , srcDirFolder , file , dstPath ) )
							res = false;
					}
				}
			}
			else {
				res = executeScpNameLocalToRemote( action , srcDirFolder , srcNames , dstPath );
			}
	    }
		finally {
			kill( action );
		}
		
		return( res );
	}

	private void scpConnect( ActionBase action ) throws Exception {
		EngineAuthResource res = getAuthResource( action , account );
		startJsshSession( action , account , res );
		startJsshScpChannel( action );
	}

	private boolean executeScpNameRemoteToLocal( ActionBase action , RemoteFolder srcDirFolder , String srcName , String dstPath ) throws Exception {
		String dstDir = Common.getDirName( dstPath );
		File dstName = new File( action.getLocalPath( dstPath ) );
		File fileDstDir = new File( action.getLocalPath( dstDir ) );
		
		boolean res = false;
		if( srcDirFolder.checkFileExists( action , srcName ) ) {
			if( dstName.isDirectory() ) {
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
		else
		if( srcDirFolder.checkFolderExists( action , srcName ) ) {
			if( !fileDstDir.isDirectory() ) {
				action.exit1( _Error.ScpMissingDestinationDirectory1 , "scp: missing destination directory=" + dstDir , dstDir );
			}
			else {
				if( dstName.isFile() ) {
					action.exit1( _Error.ScpDestinationCannotBeFile1 , "scp: cannot copy directory to a file: " + dstPath , dstPath );
				}
				else {
					if( dstName.isDirectory() ) {
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
	
	private boolean executeScpNameLocalToRemote( ActionBase action , LocalFolder srcDirFolder , String srcName , String dstPath ) throws Exception {
		String dstDir = Common.getDirName( dstPath );
		String dstName = Common.getBaseName( dstPath );
		RemoteFolder fileDstDir = action.getRemoteFolder( account , dstDir );
		
		boolean res = false;
		if( srcDirFolder.checkFileExists( action , srcName ) ) {
			if( fileDstDir.checkFolderExists( action , dstName ) ) {
				executeScpFileLocalToRemote( action , srcDirFolder.getFilePath( action , srcName ) , Common.getPath( dstPath , srcName ) );
				res = true;
			}
			else {
				if( fileDstDir.checkExists( action ) ) {
					executeScpFileLocalToRemote( action , srcDirFolder.getFilePath( action , srcName ) , dstPath );
					res = true;
				}
				else {
					action.exit1( _Error.ScpMissingDestinationDirectory1 , "scp: missing destination directory=" + dstDir , dstDir );
				}
			}
		}
		else
		if( srcDirFolder.checkFolderExists( action , srcName ) ) {
			if( !fileDstDir.checkExists( action ) ) {
				action.exit1( _Error.ScpMissingDestinationDirectory1 , "scp: missing destination directory=" + dstDir , dstDir );
			}
			else {
				if( fileDstDir.checkFileExists( action , dstName ) ) {
					action.exit1( _Error.ScpDestinationCannotBeFile1 , "scp: cannot copy directory to a file: " + dstPath , dstPath );
				}
				else {
					if( fileDstDir.checkFolderExists( action , dstName ) ) {
						RemoteFolder folder = action.getRemoteFolder( account , dstPath );
						RemoteFolder folderDst = folder.getSubFolder( action , srcName ); 
						folderDst.ensureExists( action );
						executeScpDirContentLocalToRemote( action , srcDirFolder.getSubFolder( action , srcName ) , folderDst );
						res = true;
					}
					else {
						RemoteFolder folder = action.getRemoteFolder( account , dstPath );
						folder.ensureExists( action );
						executeScpDirContentLocalToRemote( action , srcDirFolder.getSubFolder( action , srcName ) , folder );
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
		channel.get( remotePath , dstPath );
	}

	private void executeScpFileLocalToRemote( ActionBase action , String srcPath , String remotePath ) throws Exception {
		ChannelSftp channel = ( ChannelSftp )jchannel;
		channel.put( srcPath , remotePath );
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

	private void executeScpDirContentLocalToRemote( ActionBase action , LocalFolder srcDirFolder , RemoteFolder dstFolder ) throws Exception {
		List<String> topDirs = new LinkedList<String>();
		List<String> topFiles = new LinkedList<String>();
		srcDirFolder.getTopDirsAndFiles( action , topDirs , topFiles );
		for( String dir : topDirs ) {
			RemoteFolder child = dstFolder.getSubFolder( action , dir );
			child.ensureExists( action );
			executeScpDirContentLocalToRemote( action , srcDirFolder.getSubFolder( action , dir ) , child );
		}
		
		for( String file : topFiles ) {
			String srcFile = srcDirFolder.getFilePath( action , file );
			String dstFile = dstFolder.getFilePath( action , file );
			executeScpFileLocalToRemote( action , srcFile , dstFile );
		}
	}

}
