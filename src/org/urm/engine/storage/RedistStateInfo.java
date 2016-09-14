package org.urm.engine.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.meta.MetaDistrBinaryItem;
import org.urm.engine.meta.MetaDistrConfItem;
import org.urm.engine.meta.MetaEnvServerNode;
import org.urm.engine.meta.Meta.VarCONTENTTYPE;
import org.urm.engine.shell.ShellExecutor;

public class RedistStateInfo {

	public boolean exists;
	private Map<String,FileInfo> verData;

	public void gather( ActionBase action , MetaEnvServerNode node , VarCONTENTTYPE CONTENTTYPE , String STATEDIR ) throws Exception {
		verData = new HashMap<String,FileInfo>(); 
		ShellExecutor shell = action.getShell( action.getNodeAccount( node ) );
		if( !shell.checkDirExists( action , STATEDIR ) ) {
			exists = false;
			return;
		}
		
		Map<String,List<String>> items = shell.getFilesContent( action , STATEDIR , "*.ver" );
		for( String verFile : items.keySet() ) {
			String verName = Common.getPartBeforeLast( verFile , ".ver" );
			List<String> data = items.get( verFile );
			if( data.size() != 1 )
				action.exit1( _Error.InvalidStateFile1 , "invalid state file=" + verName , verName );
			
			String verInfo = data.get( 0 );
			FileInfo info = createFileInfo( action , CONTENTTYPE , verName , verInfo );
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
			action.exit1( _Error.UnknownVersionKey1 , "unknown key=" + key , key );
		return( value );
	}
	
	public FileInfo findVerData( ActionBase action , String key ) throws Exception {
		return( verData.get( key ) );
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
		return( value.deployBaseName );
	}
	
	public String getKeyFileName( ActionBase action , String key ) throws Exception {
		FileInfo value = getVerData( action , key );
		return( value.deployFinalName );
	}
	
	public static FileInfo getFileInfo( ActionBase action , MetaDistrBinaryItem item , RemoteFolder stateFolder , String stateFileName , String deployNameNoVersion , String version , String finalName ) throws Exception {
		String md5value = stateFolder.getFileMD5( action , stateFileName );
		FileInfo info = new FileInfo( item , version , md5value , deployNameNoVersion , finalName ); 
		return( info );
	}

	public static FileInfo getFileInfo( ActionBase action , MetaDistrConfItem item , RemoteFolder stateFolder , String stateFileName , String version , boolean partial ) throws Exception {
		String md5value = stateFolder.getFileMD5( action , stateFileName );
		FileInfo info = new FileInfo( item , version , md5value , partial ); 
		return( info );
	}

	public String getKeyItem( ActionBase action , String key ) throws Exception {
		getVerData( action , key );
		return( key );
	}
	
	private FileInfo createFileInfo( ActionBase action , VarCONTENTTYPE CONTENTTYPE , String verName , String verInfo ) throws Exception {
		String baseitem = verName;
		if( action.meta.isBinaryContent( action , CONTENTTYPE ) ) {
			MetaDistrBinaryItem item = action.meta.distr.getBinaryItem( action , baseitem );
			FileInfo info = new FileInfo();
			info.set( action , item , verInfo );
			return( info );
		}
		
		if( action.meta.isConfContent( action , CONTENTTYPE ) ) {
			MetaDistrConfItem item = action.meta.distr.getConfItem( action , baseitem );
			FileInfo info = new FileInfo();
			info.set( action , item , verInfo );
			return( info );
		}
		
		action.exitUnexpectedState();
		return( null );
	}
	
	public boolean needUpdate( ActionBase action , MetaDistrBinaryItem item , Dist dist , String fileName , String deployBaseName , String deployFinalName ) throws Exception {
		if( action.context.CTX_FORCE )
			return( true );

		// get current state if any
		FileInfo info = verData.get( item.KEY );
		if( info == null ) {
			action.debug( "redist item=" + item.KEY + " - nothing found in live" );
			return( true );
		}
		
		// check deploy name changed
		if( !info.deployBaseName.equals( deployBaseName ) ) {
			action.debug( "redist item=" + item.KEY + " - deploy basename has been changed" );
			return( true );
		}
		
		// check md5
		String ms5value = dist.getDistItemMD5( action , item , fileName );
		if( !ms5value.equals( info.md5value ) ) {
			action.debug( "redist item=" + item.KEY + " - md5 differs (" + info.md5value + "/" + ms5value + ")" );
			return( true );
		}

		// check deploy name
		if( !info.deployFinalName.equals( deployFinalName ) ) {
			// check version change ignored
			if( action.context.CTX_IGNOREVERSION ) {
				action.debug( "redist item=" + item.KEY + " - skip deploy, version change only" );
				return( false );
			}

			action.debug( "redist item=" + item.KEY + " - deploy version has been changed" );
			return( true );
		}
		
		action.debug( "redist item=" + item.KEY + " - skip deploy, no changes" );
		return( false );
	}

	public boolean needUpdate( ActionBase action , MetaDistrBinaryItem item , String filePath , String deployBaseName , String RELEASEVER , String deployFinalName ) throws Exception {
		if( action.context.CTX_FORCE )
			return( true );

		// get current state if any
		FileInfo info = verData.get( item.KEY );
		if( info == null ) {
			action.debug( "redist item=" + item.KEY + " - nothing found in live" );
			return( true );
		}
		
		// check deploy name changed
		if( info.deployBaseName == null || info.deployBaseName.equals( deployBaseName ) == false ) {
			action.debug( "redist item=" + item.KEY + " - deploy basename has been changed" );
			return( true );
		}
		
		// check md5
		String ms5value = action.shell.getMD5( action , filePath );
		if( info.md5value == null || ms5value.equals( info.md5value ) == false ) {
			action.debug( "redist item=" + item.KEY + " - md5 differs (" + info.md5value + "/" + ms5value + ")" );
			return( true );
		}

		// check deploy name
		if( info.deployFinalName == null || info.deployFinalName.equals( deployFinalName ) == false ) {
			// check version change ignored
			if( action.context.CTX_IGNOREVERSION ) {
				action.debug( "redist item=" + item.KEY + " - only deploy version changed. Skipped." );
				return( false );
			}

			action.debug( "redist item=" + item.KEY + " - deploy name has been changed" );
			return( true );
		}
		
		action.debug( "redist item=" + item.KEY + " - no changes. Skipped." );
		return( false );
	}

}