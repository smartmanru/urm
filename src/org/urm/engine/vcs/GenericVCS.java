package org.urm.engine.vcs;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.security.AuthResource;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;

public abstract class GenericVCS {

	ActionBase action;
	Meta meta;
	
	public AuthResource res;
	public ShellExecutor shell;
	public ProjectBuilder builder;
	
	protected GenericVCS( ActionBase action , Meta meta , AuthResource res , ShellExecutor shell , ProjectBuilder builder ) {
		this.action = action;
		this.meta = meta;
		this.res = res;
		this.shell = shell;
		this.builder = builder;
	}
	
	public abstract MirrorCase getMirror( MirrorRepository mirror ) throws Exception;
	public abstract boolean verifyRepository( String repo , String pathToRepo );

	public abstract String getMainBranch();
	public abstract String getSpecialDirectoryRegExp();
	public abstract boolean ignoreDir( String name );
	public abstract boolean ignoreFile( String name );
	
	public abstract String[] getBranches( MetaSourceProject project ) throws Exception;
	public abstract String[] getTags( MetaSourceProject project ) throws Exception;
	public abstract boolean checkout( MetaSourceProject project , LocalFolder PATCHPATH , String BRANCH ) throws Exception;
	public abstract boolean commit( MetaSourceProject project , String BRANCH , LocalFolder PATCHPATH , String MESSAGE ) throws Exception;
	public abstract boolean copyBranchToBranch( MetaSourceProject project , String branchFrom , String branchTo , boolean deleteOld ) throws Exception;
	public abstract boolean renameBranchToBranch( MetaSourceProject project , String branchFrom , String branchTo , boolean deleteOld ) throws Exception;
	public abstract boolean copyTagToTag( MetaSourceProject project , String tagFrom , String tagTo , boolean deleteOld ) throws Exception;
	public abstract boolean renameTagToTag( MetaSourceProject project , String tagFrom , String tagTo , boolean deleteOld ) throws Exception;
	public abstract boolean copyTagToBranch( MetaSourceProject project , String tagFrom , String branchTo , boolean deleteOld ) throws Exception;
	public abstract boolean dropTag( MetaSourceProject project , String tag ) throws Exception;
	public abstract boolean dropBranch( MetaSourceProject project , String branch ) throws Exception;
	public abstract boolean setTag( MetaSourceProject project , String branch , String tag , String branchDate , boolean deleteOld ) throws Exception;
	public abstract boolean export( MetaSourceProject project , LocalFolder PATCHPATH , String branch , String tag , String singlefile ) throws Exception;
	
	public abstract boolean exportRepositoryMasterPath( MirrorRepository mirror , LocalFolder PATCHPATH , String ITEMPATH , String name ) throws Exception;
	public abstract boolean exportRepositoryTagPath( MirrorRepository mirror , LocalFolder PATCHPATH , String TAG , String ITEMPATH , String name ) throws Exception;
	public abstract boolean isValidRepositoryMasterPath( MirrorRepository mirror , String path ) throws Exception;
	public abstract boolean isValidRepositoryTagPath( MirrorRepository mirror , String TAG , String path ) throws Exception;
	public abstract String getInfoMasterPath( MirrorRepository mirror , String ITEMPATH ) throws Exception;
	public abstract boolean createMasterFolder( MirrorRepository mirror , String ITEMPATH , String commitMessage ) throws Exception;
	public abstract boolean moveMasterFiles( MirrorRepository mirror , String srcFolder , String dstFolder , String itemPath , String commitMessage ) throws Exception;
	public abstract String[] listMasterItems( MirrorRepository mirror , String masterFolder ) throws Exception;
	public abstract void deleteMasterFolder( MirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception;
	public abstract void checkoutMasterFolder( MirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder ) throws Exception;
	public abstract void importMasterFolder( MirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) throws Exception;
	public abstract void ensureMasterFolderExists( MirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception;
	public abstract boolean commitMasterFolder( MirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) throws Exception;
	public abstract void createMasterTag( MirrorRepository mirror , String masterFolder , String TAG , String commitMessage ) throws Exception;
	
	public abstract void addFileToCommit( MirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception;
	public abstract void deleteFileToCommit( MirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception;
	public abstract void addDirToCommit( MirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception;
	public abstract void deleteDirToCommit( MirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception;
	
	public static GenericVCS getVCS( ActionBase action , Meta meta , Integer resourceId ) throws Exception {
		return( getVCS( action , meta , resourceId , false , null ) );
	}
	
	public static GenericVCS getVCS( ActionBase action , AuthResource res ) throws Exception {
		return( getVCS( action , null , res , action.shell , null ) );
	}
	
	public static GenericVCS getVCS( ActionBase action , Meta meta , Integer resourceId , boolean noAuth , ProjectBuilder builder ) throws Exception {
		AuthResource res = action.getResource( resourceId );
		if( !noAuth )
			res.loadAuthData();
		
		ShellExecutor shell = action.shell;
		if( builder != null ) {
			if( builder.BUILDER_REMOTE ) {
				Account account = builder.getRemoteAccount( action );
				shell = action.getShell( account );
			}
		}

		return( getVCS( action , meta , res , shell , builder ) );
	}

	private static GenericVCS getVCS( ActionBase action , Meta meta , AuthResource res , ShellExecutor shell , ProjectBuilder builder ) throws Exception {
		res.loadAuthData();
		if( res.isSvn() ) {
			if( res.ac == null )
				return( null );
			if( res.ac.isCommon() ) {
				if( res.ac.USER.isEmpty() || res.ac.PASSWORDSAVE.isEmpty() )
					return( null );
			}
			
			return( new SubversionVCS( action , meta , res , shell , builder ) );
		}
		
		if( res.isGit() )
			return( new GitVCS( action , meta , res , shell , builder ) );
		
		action.exit2( _Error.UnexectedVcsType2 , "unexpected vcs=" + res.NAME + ", type=" + Common.getEnumLower( res.RESOURCE_TYPE ) , res.NAME , Common.getEnumLower( res.RESOURCE_TYPE ) );
		return( null );
	}
	
	public static SubversionVCS getSvnDirect( ActionBase action , Integer resourceId ) throws Exception {
		AuthResource res = action.getResource( resourceId );
		if( !res.isSvn() )
			action.exit1( _Error.NonSvnResource1 , "unexpected non-svn resource=" + res.NAME , res.NAME );
		return( ( SubversionVCS )getVCS( action , null , resourceId ) );
	}

	public static GitVCS getGitDirect( ActionBase action , Integer resourceId ) throws Exception {
		AuthResource res = action.getResource( resourceId );
		if( !res.isSvn() )
			action.exit1( _Error.NonSvnResource1 , "unexpected non-svn resource=" + res.NAME , res.NAME );
		return( ( GitVCS )getVCS( action , null , resourceId ) );
	}

}
