package org.urm.server.vcs;

import java.util.List;

import org.urm.server.ServerSettings;
import org.urm.server.action.ActionBase;
import org.urm.server.shell.Account;
import org.urm.server.storage.LocalFolder;

public class GitMirrorStorage {

	GitVCS vcs;
	String REPOFOLDER;
	
	public Account account;
	public LocalFolder mirrorFolder;
	public boolean windows;
	
	public GitMirrorStorage( GitVCS vcs , String REPOFOLDER , boolean bare ) {
		this.vcs = vcs;
		this.REPOFOLDER = REPOFOLDER; 
	}
	
	private void create( Account account , LocalFolder mirrorFolder ) {
		this.account = account;
		this.mirrorFolder = mirrorFolder;
	}

	public void createLocalMirror( ActionBase action , String REPOSITORY , String reponame , String reporoot , boolean bare ) throws Exception {
		LocalFolder mirrorFolder = getBaseFolder( action );
		if( !mirrorFolder.checkExists( action ) )
			action.exit( "mirror path " + mirrorFolder.folderPath + " does not exist" );
		
		LocalFolder projectFolder = mirrorFolder.getSubFolder( action , REPOFOLDER );
		if( projectFolder.checkExists( action ) )
			action.exit( "mirror path " + projectFolder.folderPath + " already exists" );
	
		projectFolder.getParentFolder( action ).ensureExists( action );
		create( account , projectFolder );
		
		String repoPath = reponame;
		if( reporoot != null && !reporoot.isEmpty() )
			repoPath = reporoot + "/" + repoPath;
		
		vcs.cloneRemoteToLocal( repoPath , REPOSITORY , projectFolder , bare );
	}

	public boolean isEmpty( ActionBase action ) throws Exception {
		List<String> dirs = mirrorFolder.getTopDirs( action );
		List<String> files = mirrorFolder.getTopFiles( action );
		
		if( dirs.size() == 1 && dirs.get(0).equals( ".git" ) ) {
			if( files.isEmpty() || ( files.size() == 1 && files.get(0).equals( "README.md" ) ) )
				return( true );
		}
		
		return( false );
	}
	
	public void createReadMe( ActionBase action , String REPOSITORY ) throws Exception {
		if( mirrorFolder.checkFileExists( action , "README.md" ) )
			return;
				
		mirrorFolder.createFileFromString( action , "README.md" , "# URM REPOSITORY" );
		vcs.addFileToCommit( mirrorFolder , "" , "README.md" );
		if( !vcs.commitMasterFolder( mirrorFolder , REPOSITORY , "" , "first commit" ) )
			action.exit( "unable to commit" );
	}
	
	public void create( ActionBase action , boolean newStorage ) throws Exception {
		LocalFolder storageFolder = getStorageFolder( action );
		
		if( newStorage ) {
			if( storageFolder.checkExists( action ) )
				action.exit( "mirror path " + storageFolder.folderPath + " already exists" );
		}
		else {
			if( !storageFolder.checkExists( action ) )
				action.exit( "mirror path " + storageFolder.folderPath + " should be already created" );
		}
	
		create( account , storageFolder );
	}
	
	public LocalFolder getStorageFolder( ActionBase action ) throws Exception {
		LocalFolder baseFolder = getBaseFolder( action );
		LocalFolder storageFolder = baseFolder.getSubFolder( action , REPOFOLDER );
		return( storageFolder );
	}
	
	private LocalFolder getBaseFolder( ActionBase action ) throws Exception {
		String mirrorPath; 
		if( action.meta.product == null ) {
			ServerSettings settings = action.engine.getSettings();
			mirrorPath = settings.serverContext.WORK_MIRRORPATH;
		}
		else
			mirrorPath = action.meta.product.CONFIG_MIRRORPATH;
		
		if( mirrorPath.isEmpty() )
			action.exit( "missing configuraion parameter: mirror path" );
		
		return( action.getLocalFolder( mirrorPath ) );
	}

	public void remove( ActionBase action ) throws Exception {
		LocalFolder storageFolder = getStorageFolder( action );
		if( storageFolder.checkExists( action ) )
			storageFolder.removeThis( action );
	}
	
}
