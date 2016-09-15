package org.urm.engine.vcs;

import org.urm.action.ActionBase;
import org.urm.engine.ServerMirrorRepository;
import org.urm.engine.ServerSettings;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.LocalFolder;

public abstract class MirrorStorage {

	GenericVCS vcs;
	ServerMirrorRepository mirror;
	
	public Account account;
	
	private LocalFolder repoFolder;
	private LocalFolder commitFolder;
	
	protected ShellExecutor shell;
	protected ActionBase action;
	
	public abstract boolean isEmpty() throws Exception;
	
	public MirrorStorage( GenericVCS vcs , ServerMirrorRepository mirror , LocalFolder customRepoFolder ) {
		this.vcs = vcs;
		this.mirror = mirror;
		this.repoFolder = customRepoFolder;
		
		shell = vcs.shell;
		action = vcs.action;
	}
	
	public void create( boolean newStorage , boolean check ) throws Exception {
		LocalFolder repox = repoFolder;
		if( repox == null ) {
			LocalFolder basex = getBaseFolder();
			repox = basex.getSubFolder( action , mirror.getFolderName() );
		}
		
		LocalFolder commitx = repox.getSubFolder( action , mirror.RESOURCE_DATA );
		String commitOSPath = shell.getOSPath( action , commitx.folderPath );
		
		if( newStorage ) {
			if( check ) {
				if( commitx != repox && !repox.checkExists( action ) )
					action.exit1( _Error.MissingRepoMirrorDirectory1 , "Repository path " + commitOSPath + " does not exist" , commitOSPath );
				
				if( commitx.checkExists( action ) )
					action.exit1( _Error.CommitDirectoryAlreadyExists1 , "Commit path " + commitOSPath + " already exists" , commitOSPath );
			}
		
			commitx.getParentFolder( action ).ensureExists( action );
		}
		else {
			if( check ) {
				if( !commitx.checkExists( action ) )
					action.exit1( _Error.CommitDirectoryAlreadyExists1 , "Commit path " + commitOSPath + " should be already created" , commitOSPath );
			}
		}
	
		this.repoFolder = repox;
		this.commitFolder = commitx;
	}
	
	public String getRepoOSPath() throws Exception {
		return( shell.getOSPath( action , repoFolder.folderPath ) );
	}
	
	public String getCommitOSPath() throws Exception {
		return( shell.getOSPath( action , commitFolder.folderPath ) );
	}
	
	public LocalFolder getCommonRepoFolder() throws Exception {
		LocalFolder baseFolder = getBaseFolder();
		LocalFolder repoFolder = baseFolder.getSubFolder( action , mirror.getFolderName() );
		return( repoFolder );
	}

	protected LocalFolder getRepoFolder() throws Exception {
		return( repoFolder );
	}
	
	protected LocalFolder getCommitFolder() throws Exception {
		return( commitFolder );
	}
	
	protected LocalFolder getBaseFolder() throws Exception {
		String mirrorPath; 
		if( action.meta.product == null ) {
			ServerSettings settings = action.engine.getSettings();
			mirrorPath = settings.serverContext.WORK_MIRRORPATH;
		}
		else
			mirrorPath = action.meta.product.CONFIG_MIRRORPATH;
		
		if( mirrorPath.isEmpty() )
			action.exit0( _Error.MissingMirrorPathParameter0 , "Missing configuraion parameter: mirror path" );
		
		return( action.getLocalFolder( mirrorPath ) );
	}

	public void remove() throws Exception {
		if( repoFolder.checkExists( action ) )
			repoFolder.removeThis( action );
	}
	
}
