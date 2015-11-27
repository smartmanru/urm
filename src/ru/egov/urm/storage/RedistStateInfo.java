package ru.egov.urm.storage;

import java.util.HashMap;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.ShellExecutor;

public class RedistStateInfo {

	private Map<String,String> verData;
	
	public void gather( ActionBase action , MetaEnvServerNode node , String STATEDIR ) throws Exception {
		verData = new HashMap<String,String>(); 
		ShellExecutor shell = action.getShell( node.HOSTLOGIN );
		String items = shell.customGetValue( action , STATEDIR , "if [ `find . -maxdepth 1 -name \"*.ver\" | wc -l` != 0 ]; then grep -H : *.ver; fi" );
		if( items.indexOf( "No such file" ) >= 0 )
			return;
		
		// parse
		for( String s : Common.split( items , "\n" ) ) {
			String verName = Common.getPartBeforeFirst( s , ":" );
			String verInfo = Common.getPartAfterFirst( s , ":" );
			verData.put( verName , verInfo );
		}
	}

	public String getFileInfo( ActionBase action , RemoteFolder stateFolder , String stateFileName , String deployNameNoVersion , String version , String finalName ) throws Exception {
		String md5value = stateFolder.md5value( action , stateFileName );
		String value = version + ":" + md5value + ":" + deployNameNoVersion + ":" + finalName;
		return( value );
	}
	
}
