package ru.egov.urm.storage;

import java.util.HashMap;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.shell.ShellExecutor;

public class VersionInfoStorage {

	RedistStorage redist;
	
	public VersionInfoStorage( RedistStorage redist ) {
		this.redist = redist;
	}

	private String getFilePath( ActionBase action ) throws Exception {
		RemoteFolder folder = redist.getRedistStateBaseFolder( action );
		String fileName = "version.txt";
		
		String filePath = folder.getFilePath( action , fileName ); 
		if( !folder.checkFileExists( action , fileName ) )
			folder.createFileFromString( action , fileName , "ignore" );
		
		return( filePath );
	}
	
	public String getBaseStatus( ActionBase action , String BASEID ) throws Exception {
		ShellExecutor remoteSession = action.getShell( redist.account );
		String filePath = getFilePath( action );
		String[] lines = remoteSession.grepFile( action , filePath , "id=" + BASEID + ":" );
		if( lines.length == 0 )
			return( "" );
		
		if( lines.length > 1 )
			action.exit( "duplicate id=" + BASEID + " in " + filePath );
		
		String F_STATUS = Common.getPartAfterFirst( lines[0] , ":" );
		return( F_STATUS );
	}
	
	public void setBaseStatus( ActionBase action , String BASEID , String VALUE ) throws Exception {
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
