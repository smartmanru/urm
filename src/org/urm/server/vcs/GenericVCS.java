package org.urm.server.vcs;

import org.urm.server.ServerAuthResource;
import org.urm.server.ServerProjectBuilder;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaProductBuildSettings;
import org.urm.server.meta.MetaSourceProject;
import org.urm.server.meta.Meta;
import org.urm.server.shell.Account;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.Folder;
import org.urm.server.storage.LocalFolder;

public abstract class GenericVCS {

	ActionBase action;
	public ServerAuthResource res;
	ShellExecutor shell;
	Meta meta;
	
	protected GenericVCS( ActionBase action , ServerAuthResource res , ShellExecutor shell ) {
		this.action = action;
		this.res = res;
		this.shell = shell;
		this.meta = action.meta;
	}
	
	public abstract String getMainBranch();
	public abstract boolean checkout( LocalFolder PATCHPATH , MetaSourceProject project , String BRANCH ) throws Exception;
	public abstract boolean commit( LocalFolder PATCHPATH , MetaSourceProject project , String MESSAGE ) throws Exception;
	public abstract boolean copyBranchToNewBranch( MetaSourceProject project , String branchFrom , String branchTo ) throws Exception;
	public abstract boolean renameBranchToNewBranch( MetaSourceProject project , String branchFrom , String branchTo ) throws Exception;
	public abstract boolean copyTagToNewTag( MetaSourceProject project , String tagFrom , String tagTo ) throws Exception;
	public abstract boolean copyTagToTag( MetaSourceProject project , String tagFrom , String tagTo ) throws Exception;
	public abstract boolean renameTagToTag( MetaSourceProject project , String tagFrom , String tagTo ) throws Exception;
	public abstract boolean copyTagToNewBranch( MetaSourceProject project , String tagFrom , String branchTo ) throws Exception;
	public abstract boolean dropTag( MetaSourceProject project , String tag ) throws Exception;
	public abstract boolean dropBranch( MetaSourceProject project , String branch ) throws Exception;
	public abstract boolean export( Folder PATCHPATH , MetaSourceProject project , String branch , String tag , String singlefile ) throws Exception;
	public abstract boolean exportRepositoryMasterPath( LocalFolder PATCHPATH , String repository , String ITEMPATH , String name ) throws Exception;
	public abstract boolean exportRepositoryTagPath( LocalFolder PATCHPATH , String repository , String TAG , String ITEMPATH , String name ) throws Exception;
	public abstract boolean setTag( MetaSourceProject project , String branch , String tag , String branchDate ) throws Exception;
	public abstract boolean isValidRepositoryMasterPath( String repository , String path ) throws Exception;
	public abstract boolean isValidRepositoryTagPath( String repository , String TAG , String path ) throws Exception;
	public abstract String getInfoMasterPath( String repository , String ITEMPATH ) throws Exception;
	public abstract boolean createMasterFolder( String repository , String ITEMPATH , String commitMessage ) throws Exception;
	public abstract boolean moveMasterFiles( String repository , String srcFolder , String dstFolder , String itemPath , String commitMessage ) throws Exception;
	public abstract String[] listMasterItems( String repository , String masterFolder ) throws Exception;
	public abstract void deleteMasterFolder( String repository , String masterFolder , String commitMessage ) throws Exception;
	public abstract void checkoutMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder ) throws Exception;
	public abstract void importMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder , String commitMessage ) throws Exception;
	public abstract void ensureMasterFolderExists( String repository , String masterFolder , String commitMessage ) throws Exception;
	public abstract boolean commitMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder , String commitMessage ) throws Exception;
	public abstract void addFileToCommit( LocalFolder PATCHPATH , String folder , String file ) throws Exception;
	public abstract void deleteFileToCommit( LocalFolder PATCHPATH , String folder , String file ) throws Exception;
	public abstract void addDirToCommit( LocalFolder PATCHPATH , String folder ) throws Exception;
	public abstract void deleteDirToCommit( LocalFolder PATCHPATH , String folder ) throws Exception;
	public abstract void createMasterTag( String repository , String masterFolder , String TAG , String commitMessage ) throws Exception;

	public static GenericVCS getVCS( ActionBase action , String vcs , boolean build ) throws Exception {
		ServerAuthResource res = action.getResource( vcs );
		res.loadAuthData();
		
		ShellExecutor shell = action.shell;
		if( build ) {
			MetaProductBuildSettings settings = action.meta.product.getBuildSettings( action );
			if( !settings.CONFIG_BUILDER_REMOTE.isEmpty() ) {
				ServerProjectBuilder builder = action.getBuilder( settings.CONFIG_BUILDER_REMOTE );
				Account account = builder.getAccount( action );
				shell = action.getShell( account );
			}
		}
		
		if( res.isSvn() )
			return( new SubversionVCS( action , res , shell ) );
		
		if( res.isGit() )
			return( new GitVCS( action , res , shell ) );
		
		action.exit( "unexected vcs=" + vcs + ", type=" + res.TYPE );
		return( null );
	}

	public static SubversionVCS getSvnDirect( ActionBase action , String resource ) throws Exception {
		ServerAuthResource res = action.getResource( resource );
		if( !res.isSvn() )
			action.exit( "unexpected non-svn resource=" + resource );
		return( ( SubversionVCS )getVCS( action , resource , false ) );
	}

	public static GitVCS getGitDirect( ActionBase action , String resource ) throws Exception {
		ServerAuthResource res = action.getResource( resource );
		if( !res.isGit() )
			action.exit( "unexpected non-git resource=" + resource );
		return( ( GitVCS )getVCS( action , resource , false ) );
	}

}
