package org.urm.engine.vcs;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerMirrorRepository;

public class SubversionMirrorStorage extends MirrorStorage {

	SubversionVCS vcs;
	
	public SubversionMirrorStorage( SubversionVCS vcs , ServerMirrorRepository mirror , LocalFolder customRepoFolder ) {
		super( vcs , mirror , customRepoFolder );
		this.vcs = vcs;
	}

	@Override
	public boolean isEmpty() throws Exception {
		List<String> dirs = new LinkedList<String>();
		List<String> files = new LinkedList<String>();
		
		LocalFolder commitFolder = super.getCommitFolder();
		if( !commitFolder.checkExists( action ) )
			return( true );
		
		commitFolder.getTopDirsAndFiles( action , dirs , files );
		if( !files.isEmpty() )
			return( false );
		if( dirs.isEmpty() )
			return( true );
		if( dirs.size() == 1 && dirs.get(0).equals( ".svn" ) )
			return( true );
		return( false );
	}

	public void createLocalMirror() throws Exception {
		if( !vcs.isValidRepositoryMasterRootPath( mirror , "/" ) )
			action.exit0( _Error.UnableCheckRepositoryPath0 , "Unable to check master repository path" );

		String remotePath = vcs.getRepositoryPath( mirror ); 
		LocalFolder repoFolder = super.getRepoFolder();
		LocalFolder dataFolder = repoFolder.getSubFolder( action , mirror.RESOURCE_DATA );
		
		if( dataFolder.checkExists( action ) )
			action.exit1( _Error.CommitDirectoryAlreadyExists1 , "Commit folder already exists - " + dataFolder.folderPath , dataFolder.folderPath );
			
		dataFolder.getParentFolder( action ).ensureExists( action );
		
		if( vcs.isValidRepositoryMasterPath( mirror , "/" ) ) {
			if( !vcs.checkMirrorEmpty( mirror ) )
				action.exit1( _Error.MirrorDirectoryNotEmpty1 , "Target mirror folder is not empty - " + mirror.RESOURCE_DATA , mirror.RESOURCE_DATA );
		}
		else {
			int status = shell.customGetStatus( action , "svn mkdir -m initial --non-interactive " + vcs.SVNAUTH + " " + remotePath );
			if( status != 0 )
				action.exit1( _Error.UnableCreateRepoFolder1 , "Unable to create repository folder " + remotePath , remotePath );
			String pathTags = Common.getPath( remotePath , "tags" );
			status = shell.customGetStatus( action , "svn mkdir -m initial --non-interactive " + vcs.SVNAUTH + " " + pathTags );
			if( status != 0 )
				action.exit1( _Error.UnableCreateRepoFolder1 , "Unable to create repository folder " + pathTags , pathTags );
		}
		
		String ospath = super.getCommitOSPath();
		int status = shell.customGetStatus( action , "svn co --non-interactive " + vcs.SVNAUTH + " " + remotePath + " " + ospath );
		if( status != 0 )
			action.exit1( _Error.UnableCheckOut1 , "Having problem to check out " + remotePath , remotePath );
	}

	public void createServerMirror() throws Exception {
		if( !vcs.isValidRepositoryMasterPath( mirror , "/" ) )
			action.exit0( _Error.UnableCheckRepositoryPath0 , "Unable to check master repository path" );

		String remotePath = vcs.getRepositoryPath( mirror ); 
		LocalFolder repoFolder = super.getRepoFolder();
		LocalFolder dataFolder = repoFolder.getSubFolder( action , mirror.RESOURCE_DATA );
		
		if( dataFolder.checkExists( action ) )
			action.exit1( _Error.CommitDirectoryAlreadyExists1 , "Commit folder already exists - " + dataFolder.folderPath , dataFolder.folderPath );
			
		dataFolder.getParentFolder( action ).ensureExists( action );
		
		String ospath = super.getCommitOSPath();
		int status = shell.customGetStatus( action , "svn co --non-interactive " + vcs.SVNAUTH + " " + remotePath + " " + ospath );
		if( status != 0 )
			action.exit1( _Error.UnableCheckOut1 , "Having problem to check out " + remotePath , remotePath );
	}

	public void commit( String msg ) throws Exception {
		LocalFolder commitFolder = super.getCommitFolder();
		shell.customCheckStatus( action , commitFolder.folderPath , "svn commit -m " + Common.getQuoted( msg ) + " " + vcs.SVNAUTH );
	}
	
	public void update() throws Exception {
		LocalFolder commitFolder = super.getCommitFolder();
		shell.customCheckStatus( action , commitFolder.folderPath , "svn update " + vcs.SVNAUTH );
	}
	
}
