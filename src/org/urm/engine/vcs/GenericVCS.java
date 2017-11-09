package org.urm.engine.vcs;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.EngineAuthResource;
import org.urm.meta.engine.EngineBuilders;
import org.urm.meta.engine.EngineMirrorRepository;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;

public abstract class GenericVCS {

	ActionBase action;
	Meta meta;
	
	public EngineAuthResource res;
	ShellExecutor shell;
	
	protected GenericVCS( ActionBase action , Meta meta , EngineAuthResource res , ShellExecutor shell ) {
		this.action = action;
		this.meta = meta;
		this.res = res;
		this.shell = shell;
	}
	
	public abstract MirrorCase getMirror( EngineMirrorRepository mirror ) throws Exception;
	public abstract boolean verifyRepository( String repo , String pathToRepo );

	public abstract String getMainBranch();
	public abstract boolean ignoreDir( String name );
	public abstract boolean ignoreFile( String name );
	
	public abstract boolean checkout( MetaSourceProject project , LocalFolder PATCHPATH , String BRANCH ) throws Exception;
	public abstract boolean commit( MetaSourceProject project , String BRANCH , LocalFolder PATCHPATH , String MESSAGE ) throws Exception;
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
	
	public abstract boolean exportRepositoryMasterPath( EngineMirrorRepository mirror , LocalFolder PATCHPATH , String ITEMPATH , String name ) throws Exception;
	public abstract boolean exportRepositoryTagPath( EngineMirrorRepository mirror , LocalFolder PATCHPATH , String TAG , String ITEMPATH , String name ) throws Exception;
	public abstract boolean isValidRepositoryMasterRootPath( EngineMirrorRepository mirror , String path ) throws Exception;
	public abstract boolean isValidRepositoryMasterPath( EngineMirrorRepository mirror , String path ) throws Exception;
	public abstract boolean isValidRepositoryTagPath( EngineMirrorRepository mirror , String TAG , String path ) throws Exception;
	public abstract String getInfoMasterPath( EngineMirrorRepository mirror , String ITEMPATH ) throws Exception;
	public abstract boolean createMasterFolder( EngineMirrorRepository mirror , String ITEMPATH , String commitMessage ) throws Exception;
	public abstract boolean moveMasterFiles( EngineMirrorRepository mirror , String srcFolder , String dstFolder , String itemPath , String commitMessage ) throws Exception;
	public abstract String[] listMasterItems( EngineMirrorRepository mirror , String masterFolder ) throws Exception;
	public abstract void deleteMasterFolder( EngineMirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception;
	public abstract void checkoutMasterFolder( EngineMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder ) throws Exception;
	public abstract void importMasterFolder( EngineMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) throws Exception;
	public abstract void ensureMasterFolderExists( EngineMirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception;
	public abstract boolean commitMasterFolder( EngineMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) throws Exception;
	public abstract void createMasterTag( EngineMirrorRepository mirror , String masterFolder , String TAG , String commitMessage ) throws Exception;
	
	public abstract void addFileToCommit( EngineMirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception;
	public abstract void deleteFileToCommit( EngineMirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception;
	public abstract void addDirToCommit( EngineMirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception;
	public abstract void deleteDirToCommit( EngineMirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception;
	
	public static GenericVCS getVCS( ActionBase action , Meta meta , String vcs ) throws Exception {
		return( getVCS( action , meta , vcs , "" , false ) );
	}
	
	public static GenericVCS getVCS( ActionBase action , EngineAuthResource res ) throws Exception {
		return( getVCS( action , null , res , action.shell ) );
	}
	
	public static GenericVCS getVCS( ActionBase action , Meta meta , String vcs , String BUILDER , boolean noAuth ) throws Exception {
		EngineAuthResource res = action.getResource( vcs );
		if( !noAuth )
			res.loadAuthData();
		
		ShellExecutor shell = action.shell;
		if( !BUILDER.isEmpty() ) {
			EngineBuilders builders = action.getServerBuilders();
			ProjectBuilder builder = builders.getBuilder( BUILDER );
			if( builder.REMOTE ) {
				Account account = builder.getRemoteAccount( action );
				shell = action.getShell( account );
			}
		}

		return( getVCS( action , meta , res , shell ) );
	}

	private static GenericVCS getVCS( ActionBase action , Meta meta , EngineAuthResource res , ShellExecutor shell ) throws Exception {
		res.loadAuthData();
		if( res.isSvn() )
			return( new SubversionVCS( action , meta , res , shell ) );
		
		if( res.isGit() )
			return( new GitVCS( action , meta , res , shell ) );
		
		action.exit2( _Error.UnexectedVcsType2 , "unexected vcs=" + res.NAME + ", type=" + Common.getEnumLower( res.rcType ) , res.NAME , Common.getEnumLower( res.rcType ) );
		return( null );
	}
	
	public static SubversionVCS getSvnDirect( ActionBase action , String resource ) throws Exception {
		EngineAuthResource res = action.getResource( resource );
		if( !res.isSvn() )
			action.exit1( _Error.NonSvnResource1 , "unexpected non-svn resource=" + resource , resource );
		return( ( SubversionVCS )getVCS( action , null , resource ) );
	}

	public static GitVCS getGitDirect( ActionBase action , String resource ) throws Exception {
		EngineAuthResource res = action.getResource( resource );
		if( !res.isGit() )
			action.exit1( _Error.NonGitResource1 , "unexpected non-git resource=" + resource , resource );
		return( ( GitVCS )getVCS( action , null , resource ) );
	}

}
