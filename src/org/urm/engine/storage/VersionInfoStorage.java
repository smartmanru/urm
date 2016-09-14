package org.urm.engine.storage;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.action.ActionBase;
import org.urm.engine.shell.ShellExecutor;

public class VersionInfoStorage {

	RedistStorage redist;
	static String VERSION_FILENAME = "version.txt";
	
	public VersionInfoStorage( RedistStorage redist ) {
		this.redist = redist;
	}

	private String getFilePath( ActionBase action ) throws Exception {
		RemoteFolder folder = redist.getRedistStateBaseFolder( action );
		
		String filePath = folder.getFilePath( action , VERSION_FILENAME ); 
		if( !folder.checkFileExists( action , VERSION_FILENAME ) )
			folder.createFileFromString( action , VERSION_FILENAME , "ignore" );
		
		return( filePath );
	}

	public void clearAll( ActionBase action ) throws Exception {
		RemoteFolder folder = redist.getRedistStateBaseFolder( action );
		
		if( folder.checkFileExists( action , VERSION_FILENAME ) ) {
			folder.removeFiles( action , VERSION_FILENAME );
			folder.createFileFromString( action , VERSION_FILENAME , "ignore" );
		}
	}
	
	public String getBaseStatus( ActionBase action , String BASEID ) throws Exception {
		ShellExecutor remoteSession = action.getShell( redist.account );
		String filePath = getFilePath( action );
		String[] lines = remoteSession.grepFile( action , filePath , "id=" + BASEID + ":" );
		if( lines.length == 0 )
			return( "" );
		
		if( lines.length > 1 )
			action.exit2( _Error.DuplicateBaseId2 , "duplicate id=" + BASEID + " in " + filePath , BASEID , filePath );
		
		String F_STATUS = Common.getPartAfterFirst( lines[0] , ":" );
		return( F_STATUS );
	}
	
	public void setBaseStatus( ActionBase action , String BASEID , String VALUE ) throws Exception {
		action.debug( "set base id=" + BASEID + " status=" + VALUE + " ..." );
		ShellExecutor remoteSession = action.getShell( redist.account );
		String filePath = getFilePath( action );
		
		String newLine = "id=" + BASEID + ":" + VALUE;
		remoteSession.replaceFileLine( action , filePath , "id=" + BASEID + ":" , newLine );
	}
	
	public void clearBaseStatus( ActionBase action , String BASEID ) throws Exception {
		ShellExecutor remoteSession = action.getShell( redist.account );
		String filePath = getFilePath( action );
		
		remoteSession.replaceFileLine( action , filePath , "id=" + BASEID + ":" , "" );
	}
	
	public Map<String,String> getBaseList( ActionBase action ) throws Exception {
		ShellExecutor remoteSession = action.getShell( redist.account );
		String filePath = getFilePath( action );
		
		String lines[] = remoteSession.getFileLines( action , filePath );
		
		Map<String,String> map = new HashMap<String,String>();
		for( String line : lines ) {
			if( !line.startsWith( "id=" ) )
				continue;
			
			String BASEID = Common.getPartAfterFirst( Common.getPartBeforeFirst( line , ":" ) , "=" );
			String VALUE = Common.getPartAfterFirst( line , ":" );
			map.put( BASEID , VALUE );
		}
		
		return( map );
	}
	
}
