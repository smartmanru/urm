package org.urm.engine.vcs;

import org.urm.action.ActionBase;
import org.urm.engine.ServerMirrorRepository;
import org.urm.engine.ServerSettings;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.LocalFolder;

public class MirrorStorage {

	GenericVCS vcs;
	ServerMirrorRepository mirror;
	
	public Account account;
	public LocalFolder mirrorFolder;
	
	protected ShellExecutor shell;
	protected ActionBase action;
	
	public MirrorStorage( GenericVCS vcs , ServerMirrorRepository mirror ) {
		this.vcs = vcs;
		this.mirror = mirror;
		shell = vcs.shell;
		action = vcs.action;
	}
	
	public void create( boolean newStorage , boolean check ) throws Exception {
		LocalFolder mirrorFolder = getBaseFolder( action );
		LocalFolder storageFolder = mirrorFolder.getSubFolder( action , mirror.getFolderName() );

		if( newStorage ) {
			if( check ) {
				if( !mirrorFolder.checkExists( action ) )
					action.exit1( _Error.MissingMirrorDirectory1 , "mirror path " + mirrorFolder.folderPath + " does not exist" , mirrorFolder.folderPath );
				
				if( storageFolder.checkExists( action ) )
					action.exit1( _Error.MirrorDirectoryAlreadyExists1 , "mirror path " + storageFolder.folderPath + " already exists" , storageFolder.folderPath );
			}
		
			storageFolder.getParentFolder( action ).ensureExists( action );
		}
		else {
			if( check ) {
				if( !storageFolder.checkExists( action ) )
					action.exit1( _Error.MissingMirrorDirectory1 , "mirror path " + storageFolder.folderPath + " should be already created" , storageFolder.folderPath );
			}
		}
	
		this.mirrorFolder = storageFolder;
	}
	
	public String getMirrorOSPath() throws Exception {
		return( shell.getOSPath( action , mirrorFolder.folderPath ) );
	}
	
	public LocalFolder getStorageFolder( ActionBase action ) throws Exception {
		LocalFolder baseFolder = getBaseFolder( action );
		LocalFolder storageFolder = baseFolder.getSubFolder( action , mirror.getFolderName() );
		return( storageFolder );
	}
	
	protected LocalFolder getBaseFolder( ActionBase action ) throws Exception {
		String mirrorPath; 
		if( action.meta.product == null ) {
			ServerSettings settings = action.engine.getSettings();
			mirrorPath = settings.serverContext.WORK_MIRRORPATH;
		}
		else
			mirrorPath = action.meta.product.CONFIG_MIRRORPATH;
		
		if( mirrorPath.isEmpty() )
			action.exit0( _Error.MissingMirrorPathParameter0 , "missing configuraion parameter: mirror path" );
		
		return( action.getLocalFolder( mirrorPath ) );
	}

	public void remove( ActionBase action ) throws Exception {
		LocalFolder storageFolder = getStorageFolder( action );
		if( storageFolder.checkExists( action ) )
			storageFolder.removeThis( action );
	}
	
}
