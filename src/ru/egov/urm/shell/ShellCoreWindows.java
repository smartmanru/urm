package ru.egov.urm.shell;

import java.io.BufferedReader;
import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.Folder;

public class ShellCoreWindows extends ShellCore {

	public ShellCoreWindows( ShellExecutor executor , VarOSTYPE osType , Folder tmpFolder ) {
		super( executor , osType , tmpFolder );
	}

	private String getWinDir( ActionBase action , String dir ) throws Exception {
		return( Common.replace( dir , "/" , "\\" ) );
	}
	
	@Override protected String getExportCmd( ActionBase action ) throws Exception {
		return( "" );
	}

	@Override protected void getProcessAttributes( ActionBase action ) throws Exception {
		skipOutput( action );
		writer.write( "@echo off\r\n" );
		writer.flush();
		skipOutput( action );
	}
	
	@Override public void runCommand( ActionBase action , String cmd , boolean debug ) throws Exception {
		if( !running )
			exitError( action , "attempt to run command in closed session: " + cmd );
			
		cmdCurrent = cmd;

		cmdout.clear();
		cmderr.clear();
		
		String execLine = cmd + " & echo " + finishMarker + "\r\n";
		action.trace( executor.name + " execute: " + cmd );
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "write cmd line=" + execLine );
			
		writer.write( execLine );
		try {
			writer.flush();
		}
		catch( Throwable e ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.log( e );
		}
		
		ShellWaiter waiter = new ShellWaiter( executor , new CommandReaderWindows( debug ) );
		boolean res = waiter.wait( action , action.commandTimeout );
		
		if( !res )
			exitError( action , "command has been killed" );
	}

	@Override public int runCommandGetStatus( ActionBase action , String cmd , boolean debug ) throws Exception {
		action.exitNotImplemented();
		return( -1 );
	}

	@Override public void runCommandCritical( ActionBase action , String cmd ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public String getDirCmd( ActionBase action , String dir , String cmd ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}
	
	@Override public String getDirCmdIfDir( ActionBase action , String dir , String cmd ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override protected void killProcess( ActionBase action ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdEnsureDirExists( ActionBase action , String dir ) throws Exception {
		String wdir = getWinDir( action , dir );
		runCommand( action , "if not exist " + wdir + " md " + wdir , false );
	}

	@Override public void cmdCreateFileFromString( ActionBase action , String path , String value ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdAppendFileWithString( ActionBase action , String path , String value ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdAppendFileWithFile( ActionBase action , String pathDst , String pathSrc ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public boolean cmdCheckDirExists( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}

	@Override public boolean cmdIsFileEmpty( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}

	@Override public boolean cmdCheckFileExists( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}

	@Override public boolean cmdCheckPathExists( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( false );
	}

	@Override public String cmdFindOneTopWithGrep( ActionBase action , String path , String mask , String grepMask ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}
	
	@Override public String cmdFindOneTop( ActionBase action , String path , String mask ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override public void cmdCreateMD5( ActionBase action , String filepath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdRemoveDirContent( ActionBase action , String dirpath ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdRemoveDir( ActionBase action , String dirpath ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdRecreateDir( ActionBase action , String dirpath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdRemoveFiles( ActionBase action , String dir , String files ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdRemoveFilesWithExclude( ActionBase action , String dir , String files , String exclude ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdUnzipPart( ActionBase action , String unzipDir , String zipFile , String zipPart , String targetDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdMove( ActionBase action , String source , String target ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdExtractTarGz( ActionBase action , String tarFile , String targetFolder ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public String cmdLs( ActionBase action , String path ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}
	
	@Override public void cmdCreateTarGzFromDirContent( ActionBase action , String tarFile , String dir , String content , String exclude ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public String cmdGetFileInfo( ActionBase action , String dir , String dirFile ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override public void cmdCreateJarFromFolder( ActionBase action , String runDir , String jarFile , String folder ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdSetShellVariable( ActionBase action , String var , String value ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdGitAddPomFiles( ActionBase action , String runDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdCd( ActionBase action , String dir ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdCopyFiles( ActionBase action , String dirFrom , String files , String dirTo ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdCopyFile( ActionBase action , String fileFrom , String fileTo ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdCopyFile( ActionBase action , String fileFrom , String targetDir , String finalName , String FOLDER ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdCopyDirContent( ActionBase action , String srcDir , String dstDir ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdCopyDirDirect( ActionBase action , String dirFrom , String dirTo ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdCopyDirToBase( ActionBase action , String dirFrom , String baseDstDir ) throws Exception {
		action.exitNotImplemented();
	}
	
	@Override public void cmdScpFilesRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdScpDirContentRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdScpFilesLocalToRemote( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdScpDirLocalToRemote( ActionBase action , String srcDirPath , Account account , String baseDstDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdScpDirContentLocalToRemote( ActionBase action , String srcDirPath , Account account , String dstDir ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdScpDirRemoteToLocal( ActionBase action , String srcPath , Account account , String dstPath ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdCopyDirFileToFile( ActionBase action , Account account , String dirPath , String fileSrc , String fileDst ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdGetDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public void cmdGetTopDirsAndFiles( ActionBase action , String rootPath , List<String> dirs , List<String> files ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public String cmdGetMD5( ActionBase action , String filePath ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override public String cmdGetFileContentAsString( ActionBase action , String filePath ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override public void cmdAppendExecuteLog( ActionBase action , String msg ) throws Exception {
		String executeLog = getWinDir( action , Common.getPath( executor.rootPath , "execute.log" ) );
		String ts = Common.getLogTimeStamp();
		runCommand( action , "echo " + Common.getQuoted( ts + ": " + msg ) + " >> " + executeLog , false );
	}

	@Override public void cmdAppendUploadLog( ActionBase action , String src , String dst ) throws Exception {
		action.exitNotImplemented();
	}

	@Override public String[] cmdGetFolders( ActionBase action , String rootPath ) throws Exception {
		return( null );
	}

	@Override public String cmdGetFirstFile( ActionBase action , String dir ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}
	
	@Override public String[] cmdFindFiles( ActionBase action , String dir , String mask ) throws Exception {
		action.exitNotImplemented();
		return( null );
	}

	@Override public String cmdGetTarContentMD5( ActionBase action , String filePath ) throws Exception {
		action.exitNotImplemented();
		return( "" );
	}

	@Override public String cmdGetFilesMD5( ActionBase action , String dir , String includeList , String excludeList ) throws Exception {
		action.exitNotImplemented();
		return( null );
	}
	
	public void skipOutput( ActionBase action ) throws Exception {
		skipStreamOutput( action , reader , true );
		skipStreamOutput( action , errreader , false );
	}

	public void skipStreamOutput( ActionBase action , BufferedReader textreader , boolean required ) throws Exception {
		String buffer = "";
		if( action.context.CTX_TRACEINTERNAL )
			action.trace( "skipStreamOutput - start reading ..." );
		
		// wait to read
		if( required ) {
			readBuffer( action , textreader , buffer , '\n' );
			while( textreader.ready() )
				readBuffer( action , textreader , buffer , '\n' );
		}
		else {
			while( textreader.ready() )
				textreader.read();
		}
	}

	class CommandReaderWindows extends WaiterCommand {
		boolean debug;
		
		public CommandReaderWindows( boolean debug ) {
			this.debug = debug;
		}
		
		public void run( ActionBase action ) throws Exception {
			readStreamToMarker( action , reader , cmdout , "" );
		}
		
		private void outStreamLine( ActionBase action , String line ) throws Exception {
			if( debug )
				action.trace( line );
			else
				action.log( line );
		}

		private int checkMarker( String buffer ) {
			int bLen = buffer.length();
			int mLen = finishMarker.length();
			int kB = 0;
			int kM = 0;
			for( ; kM < mLen; ) {
				if( kB >= bLen )
					return( -1 );
				
				char cB = buffer.charAt( kB );
				char cM = finishMarker.charAt( kM );
				if( cB == cM ) {
					kB++;
					kM++;
					continue;
				}
				
				if( cB != '\n' )
					return( -1 );
					
				kB++;
			}
			
			return( kB );
		}

		private String readBufferWin( ActionBase action , BufferedReader textreader , String buffer ) throws Exception {
			String newBuffer = readBuffer( action , textreader , buffer , '\033' );
			if( newBuffer == null )
				return( buffer );
			
			// process escapes
			while( true ) {
				int index = newBuffer.indexOf( '\033' );
				if( index < 0 )
					return( newBuffer );
				
				int index2 = newBuffer.indexOf( index , 'H' );
				if( index2 < 0 ) {
					String s = readBuffer( action , textreader , buffer , '\033' );
					if( s != null )
						newBuffer = s;
					continue;
				}
				
				newBuffer = newBuffer.substring( 0 , index ) + "\n" + newBuffer.substring( index2 + 1 );
			}
		}
		
		private String skipMarker( ActionBase action , BufferedReader textreader , String buffer ) throws Exception {
			String newBuffer = buffer;
			
			for( int k = 0; k < newBuffer.length(); k++ ) {
				int count = checkMarker( buffer );
				if( count >= 0 )
					return( newBuffer.substring( count ) );
					
				newBuffer = readBufferWin( action , textreader , newBuffer );
			}
			
			return( newBuffer );
		}

		private void readStreamToMarker( ActionBase action , BufferedReader textreader , List<String> text , String prompt ) throws Exception {
			String line;
			boolean first = true;
			
			// stream should be <esc><repeat cmd> & echo <marker><esc><out line1><esc><out line2><esc><marker>
			String buffer = skipMarker( action , textreader , "" );
			if( buffer.isEmpty() )
				buffer = readBufferWin( action , textreader , buffer );
			if( buffer.charAt( 0 ) != '\n' )
				action.exit( "unexpected stream" );
			buffer = buffer.substring( 1 );
			
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "readStreamToMarker - start reading ..." );
			
			while ( true ) {
				int index = buffer.indexOf( '\n' );
				if( index < 0 ) {
					if( checkMarker( buffer ) >= 0 )
						return;
					
					String newBuffer = readBufferWin( action , textreader , buffer );
					if( newBuffer != null )
						buffer = newBuffer;
					continue;
				}
				
				line = buffer.substring( 0 , index );
				buffer = buffer.substring( index + 1 );
				
				if( action.context.CTX_TRACEINTERNAL )
					action.trace( "readStreamToMarker - line=" + line.replaceAll("\\p{C}", "?") );
				
				text.add( line );
				if( first && !prompt.isEmpty() ) {
					outStreamLine( action , prompt );
					first = false;
				}
				outStreamLine( action , line );
			}
		}
	}
	
}
