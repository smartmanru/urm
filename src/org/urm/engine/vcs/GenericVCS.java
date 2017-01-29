package org.urm.engine.vcs;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerBuilders;
import org.urm.meta.engine.ServerMirrorRepository;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;

public abstract class GenericVCS {

	ActionBase action;
	Meta meta;
	
	public ServerAuthResource res;
	ShellExecutor shell;
	
	protected GenericVCS( ActionBase action , Meta meta , ServerAuthResource res , ShellExecutor shell ) {
		this.action = action;
		this.meta = meta;
		this.res = res;
		this.shell = shell;
	}
	
	public abstract MirrorStorage createInitialMirror( ServerMirrorRepository mirror ) throws Exception;
	public abstract MirrorStorage createServerMirror( ServerMirrorRepository mirror ) throws Exception;
	public abstract boolean checkMirrorEmpty( ServerMirrorRepository mirror ) throws Exception;
	public abstract void dropMirror( ServerMirrorRepository mirror ) throws Exception;
	public abstract void pushMirror( ServerMirrorRepository mirror ) throws Exception;
	public abstract void refreshMirror( ServerMirrorRepository mirror ) throws Exception;
	public abstract MirrorStorage getMirror( ServerMirrorRepository mirror ) throws Exception;
	public abstract boolean verifyRepository( String repo , String pathToRepo );

	public abstract String getMainBranch();
	public abstract boolean ignoreDir( String name );
	public abstract boolean ignoreFile( String name );
	
	public abstract boolean checkout( MetaSourceProject project , LocalFolder PATCHPATH , String BRANCH ) throws Exception;
	public abstract boolean commit( MetaSourceProject project , LocalFolder PATCHPATH , String MESSAGE ) throws Exception;
	public abstract boolean copyBranchToNewBranch( MetaSourceProject project , String branchFrom , String branchTo ) throws Exception;
	public abstract boolean renameBranchToNewBranch( MetaSourceProject project , String branchFrom , String branchTo ) throws Exception;
	public abstract boolean copyTagToNewTag( MetaSourceProject project , String tagFrom , String tagTo ) throws Exception;
	public abstract boolean copyTagToTag( MetaSourceProject project , String tagFrom , String tagTo ) throws Exception;
	public abstract boolean renameTagToTag( MetaSourceProject project , String tagFrom , String tagTo ) throws Exception;
	public abstract boolean copyTagToNewBranch( MetaSourceProject project , String tagFrom , String branchTo ) throws Exception;
	public abstract boolean dropTag( MetaSourceProject project , String tag ) throws Exception;
	public abstract boolean dropBranch( MetaSourceProject project , String branch ) throws Exception;
	public abstract boolean export( MetaSourceProject project , LocalFolder PATCHPATH , String branch , String tag , String singlefile ) throws Exception;
	public abstract boolean setTag( MetaSourceProject project , String branch , String tag , String branchDate ) throws Exception;
	
	public abstract boolean exportRepositoryMasterPath( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String ITEMPATH , String name ) throws Exception;
	public abstract boolean exportRepositoryTagPath( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String TAG , String ITEMPATH , String name ) throws Exception;
	public abstract boolean isValidRepositoryMasterRootPath( ServerMirrorRepository mirror , String path ) throws Exception;
	public abstract boolean isValidRepositoryMasterPath( ServerMirrorRepository mirror , String path ) throws Exception;
	public abstract boolean isValidRepositoryTagPath( ServerMirrorRepository mirror , String TAG , String path ) throws Exception;
	public abstract String getInfoMasterPath( ServerMirrorRepository mirror , String ITEMPATH ) throws Exception;
	public abstract boolean createMasterFolder( ServerMirrorRepository mirror , String ITEMPATH , String commitMessage ) throws Exception;
	public abstract boolean moveMasterFiles( ServerMirrorRepository mirror , String srcFolder , String dstFolder , String itemPath , String commitMessage ) throws Exception;
	public abstract String[] listMasterItems( ServerMirrorRepository mirror , String masterFolder ) throws Exception;
	public abstract void deleteMasterFolder( ServerMirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception;
	public abstract void checkoutMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder ) throws Exception;
	public abstract void importMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) throws Exception;
	public abstract void ensureMasterFolderExists( ServerMirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception;
	public abstract boolean commitMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) throws Exception;
	public abstract void createMasterTag( ServerMirrorRepository mirror , String masterFolder , String TAG , String commitMessage ) throws Exception;
	
	public abstract void addFileToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception;
	public abstract void deleteFileToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception;
	public abstract void addDirToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception;
	public abstract void deleteDirToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception;
	
	public static GenericVCS getVCS( ActionBase action , Meta meta , String vcs ) throws Exception {
		return( getVCS( action , meta , vcs , "" , false ) );
	}
	
	public static GenericVCS getVCS( ActionBase action , ServerAuthResource res ) throws Exception {
		return( getVCS( action , null , res , action.shell ) );
	}
	
	public static GenericVCS getVCS( ActionBase action , Meta meta , String vcs , String BUILDER , boolean noAuth ) throws Exception {
		ServerAuthResource res = action.getResource( vcs );
		if( !noAuth )
			res.loadAuthData( action );
		
		ShellExecutor shell = action.shell;
		if( !BUILDER.isEmpty() ) {
			ServerBuilders builders = action.getBuilders();
			ServerProjectBuilder builder = builders.getBuilder( BUILDER );
			if( builder.remote ) {
				Account account = builder.getRemoteAccount( action );
				shell = action.getShell( account );
			}
		}

		return( getVCS( action , meta , res , shell ) );
	}

	private static GenericVCS getVCS( ActionBase action , Meta meta , ServerAuthResource res , ShellExecutor shell ) throws Exception {
		res.loadAuthData( action );
		if( res.isSvn() )
			return( new SubversionVCS( action , meta , res , shell ) );
		
		if( res.isGit() )
			return( new GitVCS( action , meta , res , shell ) );
		
		action.exit2( _Error.UnexectedVcsType2 , "unexected vcs=" + res.NAME + ", type=" + Common.getEnumLower( res.rcType ) , res.NAME , Common.getEnumLower( res.rcType ) );
		return( null );
	}
	
	public static SubversionVCS getSvnDirect( ActionBase action , String resource ) throws Exception {
		ServerAuthResource res = action.getResource( resource );
		if( !res.isSvn() )
			action.exit1( _Error.NonSvnResource1 , "unexpected non-svn resource=" + resource , resource );
		return( ( SubversionVCS )getVCS( action , null , resource ) );
	}

	public static GitVCS getGitDirect( ActionBase action , String resource ) throws Exception {
		ServerAuthResource res = action.getResource( resource );
		if( !res.isGit() )
			action.exit1( _Error.NonGitResource1 , "unexpected non-git resource=" + resource , resource );
		return( ( GitVCS )getVCS( action , null , resource ) );
	}

}
