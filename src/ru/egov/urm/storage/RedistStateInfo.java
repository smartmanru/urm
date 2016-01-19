package ru.egov.urm.storage;

import java.util.HashMap;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;

public class RedistStateInfo {

	public boolean exists;
	private Map<String,FileInfo> verData;

	public void gather( ActionBase action , MetaEnvServerNode node , String STATEDIR ) throws Exception {
		verData = new HashMap<String,FileInfo>(); 
		ShellExecutor shell = action.getShell( action.getAccount( node ) );
		if( !shell.checkDirExists( action , STATEDIR ) ) {
			exists = false;
			return;
		}
		
		String items = shell.customGetValue( action , STATEDIR , "if [ `find . -maxdepth 1 -name \"*.ver\" | wc -l` != 0 ]; then grep -H : *.ver; fi" );
		for( String s : Common.split( items , "\n" ) ) {
			String verName = Common.getPartBeforeFirst( s , ":" );
			String verInfo = Common.getPartAfterFirst( s , ":" );
			FileInfo info = new FileInfo();
			info.split( action , verInfo );
			verData.put( verName , info );
		}
		
		exists = true;
	}

	public String[] getKeys( ActionBase action ) throws Exception {
		return( Common.getSortedKeys( verData ) );
	}
	
	public FileInfo getVerData( ActionBase action , String key ) throws Exception {
		FileInfo value = verData.get( key );
		if( value == null )
			action.exit( "unknown key=" + key );
		return( value );
	}
	
	public String getKeyVersion( ActionBase action , String key ) throws Exception {
		FileInfo value = getVerData( action , key );
		return( value.version );
	}
	
	public String getKeyMD5( ActionBase action , String key ) throws Exception {
		FileInfo value = getVerData( action , key );
		return( value.md5value );
	}
	
	public String getKeyDeployName( ActionBase action , String key ) throws Exception {
		FileInfo value = getVerData( action , key );
		return( value.deployNameNoVersion );
	}
	
	public String getKeyFileName( ActionBase action , String key ) throws Exception {
		FileInfo value = getVerData( action , key );
		return( value.finalName );
	}
	
	public static String getValue( ActionBase action , RemoteFolder stateFolder , String stateFileName , String deployNameNoVersion , String version , String finalName ) throws Exception {
		String md5value = stateFolder.md5value( action , stateFileName );
		FileInfo info = new FileInfo( version , md5value , deployNameNoVersion , finalName ); 
		return( info.value( action ) );
	}

	public String getKeyItem( ActionBase action , String key ) throws Exception {
		getVerData( action , key );
		return( Common.getPartBeforeLast( Common.getPartAfterFirst( key , "-" ) , ".ver" ) );
	}
	
	
}
