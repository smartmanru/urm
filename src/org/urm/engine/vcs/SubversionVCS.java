package org.urm.engine.vcs;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.Folder;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerMirrorRepository;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaSourceProject;

public class SubversionVCS extends GenericVCS {

	String SVNPATH;
	String SVNAUTH;
	
	public SubversionVCS( ActionBase action , Meta meta , ServerAuthResource res , ShellExecutor shell ) {
		super( action , meta , res , shell );
		this.SVNPATH = res.BASEURL;
		if( res.ac != null )
			this.SVNAUTH = res.ac.getSvnAuth( action );
		else
			this.SVNAUTH = "";
	}
	
	@Override public String getMainBranch() {
		return( "trunk" );
	}

	@Override
	public boolean ignoreDir( String name ) {
		if( name.equals( ".svn" ) )
			return( true );
		return( false );
	}
	
	@Override
	public boolean ignoreFile( String name ) {
		return( false );
	}
	
	@Override public boolean checkout( MetaSourceProject project , LocalFolder PATCHFOLDER , String BRANCH ) throws Exception {
		String CO_PATH;
		String XBRANCH = BRANCH;
		if( !XBRANCH.equals( "trunk" ) )
			XBRANCH = Common.getPath( "branches" , BRANCH );

		String projectPath = getProjectPath( project );
		CO_PATH = Common.getPath( projectPath , XBRANCH );

		String REVISION;
		if( action.isLocalLinux() ) {
			REVISION = shell.customGetValue( action , "svn info " + SVNAUTH + " " + CO_PATH + " | grep Revision | tr -d " + Common.getQuoted( " " ) + 
					" | cut -d " + Common.getQuoted( ":" ) + " -f2" );
		}
		else {
			REVISION = shell.customGetValue( action , "svn info " + SVNAUTH + " " + CO_PATH + " | findstr Revision" );
			REVISION = Common.getListItem( REVISION , ":" , 1 );
		}
		
		REVISION = REVISION.trim();

		action.info( "svn: checkout sources from " + CO_PATH + " (branch=" + BRANCH + ", revision=" + REVISION + ") to " + PATCHFOLDER.folderPath + "..." );
		
		String ospath = action.getOSPath( PATCHFOLDER.folderPath );
		int status = shell.customGetStatus( action , "svn co " + SVNAUTH + " " + CO_PATH + " " + ospath );

		if( status == 0 )
			return( true );
		
		action.error( "svn: having problem to check out " + CO_PATH );
		return( false );
	}

	@Override public boolean commit( MetaSourceProject project , LocalFolder PATCHFOLDER , String COMMENT ) throws Exception {
		if( !PATCHFOLDER.checkExists( action ) ) {
			action.error( "directory " + PATCHFOLDER.folderPath + " does not exist " );
			return( false );
		}
		
		String ospath = action.getOSPath( PATCHFOLDER.folderPath );
		shell.customCheckStatus( action , "svn commit -m " + Common.getQuoted( COMMENT ) + " " + SVNAUTH + " " + ospath );
		return( true );
	}

	@Override public boolean copyBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		// check source status
		String BRANCH1X = BRANCH1;
		if( !BRANCH1X.equals( "trunk" ) )
			BRANCH1X = Common.getPath( "branches" , BRANCH1 );
		
		String projectPath = getProjectPath( project );
		String branch1Path = Common.getPath( projectPath , BRANCH1X );
		if( !checkSvnPathExists( branch1Path ) ) {
			action.error( branch1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		String BRANCH2X = BRANCH2;
		if( !BRANCH2X.equals( "trunk" ) )
			BRANCH2X = Common.getPath( "branches" , BRANCH2 );
		
		String branch2Path = Common.getPath( projectPath , BRANCH2X );
		if( checkSvnPathExists( branch2Path ) ) {
			action.error( "cannot copy branch to branch - target branch already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		shell.customCheckStatus( action , "svn copy " + SVNAUTH + " " + branch1Path + " " + branch2Path + " -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: copy branch" ) );
		return( true );
	}

	@Override public boolean renameBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		// check source status
		String BRANCH1X = BRANCH1;
		if( !BRANCH1X.equals( "trunk" ) )
			BRANCH1X = Common.getPath( "branches" , BRANCH1 );
		
		String projectPath = getProjectPath( project );
		String branch1Path = Common.getPath( projectPath , BRANCH1X );
		if( !checkSvnPathExists( branch1Path ) ) {
			action.error( branch1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		String BRANCH2X = BRANCH2;
		if( !BRANCH2X.equals( "trunk" ) )
			BRANCH2X = Common.getPath( "branches" , BRANCH2 );
		
		String branch2Path = Common.getPath( projectPath , BRANCH2X );
		if( checkSvnPathExists( branch2Path ) ) {
			action.info( "skip rename branch to branch - target branch already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		shell.customCheckStatus( action , "svn rename " + SVNAUTH + " " + branch1Path + " " + branch2Path + " -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: rename branch" ) );
		return( true );
	}

	@Override public boolean copyTagToNewTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		// check source status
		String projectPath = getProjectPath( project );
		String tag1Path = Common.getPath( projectPath , "tags" , TAG1 );
		if( !checkSvnPathExists( tag1Path ) ) {
			action.error( tag1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		String tag2Path = Common.getPath( projectPath , "tags" , TAG2 );
		if( checkSvnPathExists( tag2Path ) ) {
			action.error( "cannot copy tag - target tag already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		shell.customCheckStatus( action , "svn copy " + SVNAUTH + " " + tag1Path + " " + tag2Path + " -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: copy tag" ) );
		return( true );
	}

	@Override public boolean copyTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		// check source status
		String projectPath = getProjectPath( project );
		String tag1Path = Common.getPath( projectPath , "tags" , TAG1 );
		if( !checkSvnPathExists( tag1Path ) ) {
			action.error( tag1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		MetaProductSettings product = meta.getProductSettings( action );
		String tag2Path = Common.getPath( projectPath , "tags" , TAG2 );
		if( checkSvnPathExists( tag2Path ) ) {
			action.info( "drop already existing new tag ..." );
			shell.customCheckStatus( action , "svn delete " + SVNAUTH + " " + tag2Path + " -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: drop tag before svn copy" ) );
		}

		shell.customCheckStatus( action , "svn copy " + SVNAUTH + " " + tag1Path + " " + tag2Path + " -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: copy tag" ) );
		return( true );
	}
	
	@Override public boolean renameTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		// check source status
		String projectPath = getProjectPath( project );
		String tag1Path = Common.getPath( projectPath , "tags" , TAG1 );
		if( !checkSvnPathExists( tag1Path ) ) {
			action.error( tag1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		String tag2Path = Common.getPath( projectPath , "tags" , TAG2 );
		MetaProductSettings product = meta.getProductSettings( action );
		if( checkSvnPathExists( tag2Path ) ) {
			action.info( "drop already existing new tag ..." );
			shell.customCheckStatus( action , "svn delete " + SVNAUTH + " " + tag2Path + " -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: drop tag before svn rename" ) );
		}

		shell.customCheckStatus( action , "svn rename " + SVNAUTH + " " + tag1Path + " " + tag2Path + " -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: rename tag" ) );
		return( true );
	}
	
	@Override public boolean copyTagToNewBranch( MetaSourceProject project , String TAG1 , String BRANCH2 ) throws Exception {
		// check source status
		String projectPath = getProjectPath( project );
		String tag1Path = Common.getPath( projectPath , "tags" , TAG1 );
		if( !checkSvnPathExists( tag1Path ) ) {
			action.error( tag1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		String BRANCH2X = BRANCH2;
		if( !BRANCH2X.equals( "trunk" ) )
			BRANCH2X = Common.getPath( "branches" , BRANCH2 );
		String branch2Path = Common.getPath( projectPath , BRANCH2X );
		if( checkSvnPathExists( branch2Path ) ) {
			action.error( "cannot copy tag to branch - target branch already exists" );
			return( false );
		}

		MetaProductSettings product = meta.getProductSettings( action );
		shell.customCheckStatus( action , "svn copy " + SVNAUTH + " " + tag1Path + " " + branch2Path + " -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: copy tag to branch" ) );
		return( true );
	}
	
	@Override public boolean dropTag( MetaSourceProject project , String TAG ) throws Exception {
		// check status
		String projectPath = getProjectPath( project );
		String tagPath = Common.getPath( projectPath , "tags" , TAG );
		if( !checkSvnPathExists( tagPath ) ) {
			action.error( tagPath + ": svn path does not exist" );
			return( false );
		}
		
		MetaProductSettings product = meta.getProductSettings( action );
		shell.customCheckStatus( action , "svn delete " + SVNAUTH + " " + tagPath + " -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: drop tag" ) );
		return( true );
	}
	
	@Override public boolean dropBranch( MetaSourceProject project , String BRANCH ) throws Exception {
		// check source status
		String projectPath = getProjectPath( project );
		String BRANCHX = BRANCH;
		if( !BRANCHX.equals( "trunk" ) )
			BRANCHX = Common.getPath( "branches" , BRANCH );
		String branchPath = Common.getPath( projectPath , BRANCHX );
		if( !checkSvnPathExists( branchPath ) ) {
			action.error( branchPath + ": svn path does not exist" );
			return( false );
		}
		
		MetaProductSettings product = meta.getProductSettings( action );
		shell.customCheckStatus( action , "svn delete " + SVNAUTH + " " + branchPath + " -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: drop branch" ) );
		return( true );
	}
	
	@Override public boolean export( MetaSourceProject project , LocalFolder PATCHFOLDER , String BRANCH , String TAG , String FILENAME ) throws Exception {
		String projectPath = getProjectPath( project );
		String CO_PATH;
		if( !TAG.isEmpty() )
			CO_PATH = Common.getPath( projectPath , "tags" , TAG );
		else {
			if( BRANCH.equals( "trunk" ) )
				CO_PATH = Common.getPath( projectPath , "trunk" );
			else
				CO_PATH = Common.getPath( projectPath , "branches" , BRANCH );
		}
		
		if( !project.CODEPATH.isEmpty() )
			CO_PATH = Common.getPath( CO_PATH , project.CODEPATH );
		
		if( !FILENAME.isEmpty() )
			CO_PATH = Common.getPath( CO_PATH , FILENAME );

		if( FILENAME.isEmpty() ) {
			Folder BASEDIR = PATCHFOLDER.getParentFolder( action );
			if( !BASEDIR.checkExists( action ) )
				action.exit1( _Error.MissingLocalDirectory1 , "exportFromPath: local directory " + BASEDIR.folderPath + " does not exist" , BASEDIR.folderPath );
			if( PATCHFOLDER.checkExists( action ) )
				action.exit1( _Error.LocalDirectoryShouldNotExist1 , "exportFromPath: local directory " + PATCHFOLDER.folderPath + " should not exist" , PATCHFOLDER.folderPath );
		}
		else {
			if( !PATCHFOLDER.checkExists( action ) )
				action.exit1( _Error.MissingLocalDirectory1 , "exportFromPath: local directory " + PATCHFOLDER.folderPath + " does not exist" , PATCHFOLDER.folderPath );
		}

		String ospath = action.getOSPath( PATCHFOLDER.folderPath );
		shell.customCheckStatus( action , "svn export " + SVNAUTH + " " + CO_PATH + " " + ospath );
		return( true );
	}

	@Override public boolean setTag( MetaSourceProject project , String BRANCH , String TAG , String BRANCHDATE ) throws Exception {
		// check source status
		String projectPath = getProjectPath( project );
		String BRANCHX = BRANCH;
		if( !BRANCHX.equals( "trunk" ) )
			BRANCHX = Common.getPath( "branches" , BRANCH );
		String branchPath = Common.getPath( projectPath , BRANCHX );
		if( !checkSvnPathExists( branchPath ) ) {
			action.error( branchPath + ": svn path does not exist" );
			return( false );
		}
		
		String tagPath = Common.getPath( projectPath , "tags" , TAG );
		MetaProductSettings product = meta.getProductSettings( action );
		if( checkSvnPathExists( tagPath ) ) {
			action.info( "drop already existing tag ..." );
			shell.customCheckStatus( action , "svn delete " + SVNAUTH + " " + tagPath + " -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: drop tag before svn rename" ) );
		}
		
		if( !BRANCHDATE.isEmpty() )
			shell.customCheckStatus( action , "svn copy " + SVNAUTH + " --revision {" + Common.getQuoted( BRANCHDATE ) + "} " + branchPath + " " + tagPath + 
					" -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: set tag on branch head by date " + BRANCHDATE ) );
		else
			shell.customCheckStatus( action , "svn copy " + SVNAUTH + " " + branchPath + " " + tagPath + 
					" -m " + Common.getQuoted( product.CONFIG_ADM_TRACKER + "-0000: set tag on branch head" ) );
		
		return( true );
	}
	
	@Override public boolean isValidRepositoryMasterRootPath( ServerMirrorRepository mirror , String path ) throws Exception {
		String fullPath = Common.getPath( getRepositoryRootPath( mirror ) , path ); 
		return( checkSvnPathExists( fullPath ) );
	}
	
	@Override public boolean isValidRepositoryMasterPath( ServerMirrorRepository mirror , String path ) throws Exception {
		String fullPath = Common.getPath( getRepositoryPath( mirror ) , path ); 
		return( checkSvnPathExists( fullPath ) );
	}
	
	@Override public boolean isValidRepositoryTagPath( ServerMirrorRepository mirror , String TAG , String path ) throws Exception {
		String fullPath = Common.getPath( getRepositoryPath( mirror ) , "tags" , TAG );
		fullPath = Common.getPath( fullPath , path ); 
		return( checkSvnPathExists( fullPath ) );
	}
	
	@Override public boolean exportRepositoryMasterPath( ServerMirrorRepository mirror , LocalFolder PATCHFOLDER , String ITEMPATH , String name ) throws Exception {
		if( !isValidRepositoryMasterPath( mirror , ITEMPATH ) )
			return( false );
			
		if( !PATCHFOLDER.checkExists( action ) )
			action.exit1( _Error.MissingLocalDirectory1 , "exportRepositoryMasterPath: local directory " + PATCHFOLDER.folderPath + " does not exist" , PATCHFOLDER.folderPath );

		String CO_PATH = Common.getPath( getRepositoryPath( mirror ) , ITEMPATH );
		if( name.isEmpty() )
			name = Common.getBaseName( ITEMPATH );
		
		shell.customCheckStatus( action , PATCHFOLDER.folderPath , "svn export " + SVNAUTH + " " + CO_PATH + " " + name + " > " + shell.getOSDevNull()  );
		return( true );
	}

	@Override public boolean exportRepositoryTagPath( ServerMirrorRepository mirror , LocalFolder PATCHFOLDER , String TAG , String ITEMPATH , String name ) throws Exception {
		if( !isValidRepositoryTagPath( mirror , TAG , ITEMPATH ) )
			return( false );
		
		String TAGPATH = Common.getPath( "tags" , TAG , ITEMPATH );
		if( !PATCHFOLDER.checkExists( action ) )
			action.exit1( _Error.MissingLocalDirectory1 , "exportRepositoryTagPath: local directory " + PATCHFOLDER.folderPath + " does not exist" , PATCHFOLDER.folderPath );

		String CO_PATH = Common.getPath( getRepositoryPath( mirror ) , TAGPATH );
		if( name.isEmpty() )
			name = Common.getBaseName( ITEMPATH );
		
		shell.customCheckStatus( action , PATCHFOLDER.folderPath , "svn export " + SVNAUTH + " " + CO_PATH + " " + name + " > " + shell.getOSDevNull() );
		return( true );
	}

	@Override public String getInfoMasterPath( ServerMirrorRepository mirror , String ITEMPATH ) throws Exception {
		String CO_PATH = Common.getPath( getRepositoryPath( mirror ) , ITEMPATH );
		return( CO_PATH );
	}

	@Override public boolean createMasterFolder( ServerMirrorRepository mirror , String ITEMPATH , String commitMessage ) throws Exception {
		String fullPath = Common.getPath( getRepositoryPath( mirror ) , ITEMPATH ); 
		shell.customCheckStatus( action , "svn mkdir " + SVNAUTH + " -m " + Common.getQuoted( commitMessage ) + " --parents " + Common.getQuoted( fullPath ) + " > " + shell.getOSDevNull() );
		return( true );
	}
	
	@Override public boolean moveMasterFiles( ServerMirrorRepository mirror , String srcFolder , String dstFolder , String itemPath , String commitMessage ) throws Exception {
		String F_ITEMDIR = Common.getDirName( itemPath );
		String dstFullPath = Common.getPath( getRepositoryPath( mirror ) , dstFolder , F_ITEMDIR ); 
		String srcFullPath = Common.getPath( getRepositoryPath( mirror ) , srcFolder , itemPath ); 
		shell.customCheckStatus( action , "svn mkdir " + SVNAUTH + " -m " + Common.getQuoted( commitMessage ) + " --parents " + Common.getQuoted( srcFullPath ) + " > " + shell.getOSDevNull() );
		shell.customCheckStatus( action , "svn rename " + SVNAUTH + " -m " + Common.getQuoted( commitMessage ) + " " + Common.getQuoted( srcFullPath ) + " " + Common.getQuoted( dstFullPath ) + " > " + shell.getOSDevNull() );
		return( true );
	}

	@Override public String[] listMasterItems( ServerMirrorRepository mirror , String masterFolder ) throws Exception {
		String fullPath = Common.getPath( getRepositoryPath( mirror ) , masterFolder );
		String s = shell.customGetValue( action , "svn list " + SVNAUTH + " " + fullPath );
		s = Common.replace( s , "/" , "" );
		s = Common.replace( s , "\n" , " " );
		s.trim();
		return( Common.splitSpaced( s ) );
	}

	@Override public void deleteMasterFolder( ServerMirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception {
		String fullPath = Common.getPath( getRepositoryPath( mirror ) , masterFolder );
		shell.customCheckStatus( action , "svn delete -m " + Common.getQuoted( commitMessage ) + " " + SVNAUTH + " " + fullPath );
	}
	
	@Override public void checkoutMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder ) throws Exception {
		String fullPath = Common.getPath( getRepositoryPath( mirror ) , masterFolder );
		String ospath = action.getOSPath( PATCHPATH.folderPath );
		shell.customCheckStatus( action , "svn co " + SVNAUTH + " " + fullPath + " " + ospath );
	}
	
	@Override public void importMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) throws Exception {
		String fullPath = Common.getPath( getRepositoryPath( mirror ) , masterFolder );
		String ospath = action.getOSPath( PATCHPATH.folderPath );
		shell.customCheckStatus( action , "svn import -m " + Common.getQuoted( commitMessage ) + " " + SVNAUTH + " " + ospath + " " + fullPath );
	}
	
	@Override public void ensureMasterFolderExists( ServerMirrorRepository mirror , String masterFolder , String commitMessage ) throws Exception {
		String fullPath = Common.getPath( getRepositoryPath( mirror ) , masterFolder );
		shell.customCheckStatus( action , "svn mkdir --parents -m " + Common.getQuoted( commitMessage ) + " " + SVNAUTH + " " + fullPath );
	}
	
	@Override public boolean commitMasterFolder( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String masterFolder , String commitMessage ) {
		try {
			shell.customCheckStatus( action , Common.getPath( PATCHPATH.folderPath , masterFolder ) , "svn commit -m " + Common.getQuoted( commitMessage ) + " " + SVNAUTH );
			return( true );
		}
		catch( Throwable e ) {
			action.log( "commitMasterFolder" , e );
		}
		return( false );
	}
	
	@Override public void addFileToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		String ospath = action.getOSPath( Common.getPath( folder , file ) );
		shell.customCheckStatus( action , PATCHPATH.folderPath , "svn add " + ospath );
	}
	
	@Override public void deleteFileToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		String ospath = action.getOSPath( Common.getPath( folder , file ) );
		shell.customCheckStatus( action , PATCHPATH.folderPath , "svn delete " + ospath );
	}
	
	@Override public void addDirToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception {
		String ospath = action.getOSPath( folder );
		shell.customCheckStatus( action , PATCHPATH.folderPath , "svn add " + ospath );
	}
	
	@Override public void deleteDirToCommit( ServerMirrorRepository mirror , LocalFolder PATCHPATH , String folder ) throws Exception {
		String ospath = action.getOSPath( folder );
		shell.customCheckStatus( action , PATCHPATH.folderPath , "svn delete " + ospath );
	}

	@Override public void createMasterTag( ServerMirrorRepository mirror , String masterFolder , String TAG , String commitMessage ) throws Exception {
		String fullPathSrc = Common.getPath( getRepositoryPath( mirror ) , masterFolder );
		String fullPathTag = Common.getPath( getRepositoryPath( mirror ) , "tags" , TAG );
		
		// check source status
		if( !checkSvnPathExists( fullPathSrc ) )
			action.exit1( _Error.MissingSvnPath1 , fullPathSrc + ": svn path does not exist" , fullPathSrc );

		// check destination status
		if( checkSvnPathExists( fullPathTag ) ) {
			action.debug( "drop already existing new tag ..." );
			shell.customCheckStatus( action , "svn delete " + SVNAUTH + " " + fullPathTag + " -m " + Common.getQuoted( commitMessage ) );
		}

		action.debug( "create new tag ..." );
		String fullPathTagTarget = Common.getPath( fullPathTag , masterFolder );
		String fullPathTagTargetDir = Common.getDirName( fullPathTagTarget );
		
		shell.customCheckStatus( action , "svn mkdir --parents " + SVNAUTH + " " + fullPathTagTargetDir + " " + " -m " + Common.getQuoted( commitMessage ) );
		shell.customCheckStatus( action , "svn copy " + SVNAUTH + " " + fullPathSrc + " " + fullPathTagTarget + " -m " + Common.getQuoted( commitMessage ) );
	}

	@Override
	public boolean checkMirrorEmpty( ServerMirrorRepository mirror ) throws Exception {
		String[] items = listMasterItems( mirror , mirror.RESOURCE_DATA );
		if( items.length == 0 )
			return( true );
		return( false );
	}

	@Override
	public MirrorStorage createInitialMirror( ServerMirrorRepository mirror ) throws Exception {
		SubversionMirrorStorage storage = getStorage( mirror , true , null );
		storage.createLocalMirror();
		
		return( storage );
	}

	@Override
	public MirrorStorage createServerMirror( ServerMirrorRepository mirror ) throws Exception {
		SubversionMirrorStorage storage = getStorage( mirror , true , null );
		storage.createServerMirror();
		
		return( storage );
	}

	@Override
	public void dropMirror( ServerMirrorRepository mirror ) throws Exception {
		SubversionMirrorStorage storage = getStorage( mirror , false , null );
		storage.remove();
	}
	
	@Override
	public void pushMirror( ServerMirrorRepository mirror ) throws Exception {
		SubversionMirrorStorage storage = getStorage( mirror , false , null );
		storage.commit( "push to origin" );
	}
	
	@Override
	public void refreshMirror( ServerMirrorRepository mirror ) throws Exception {
		SubversionMirrorStorage storage = getStorage( mirror , false , null );
		storage.update();
	}

	@Override
	public MirrorStorage getMirror( ServerMirrorRepository mirror ) throws Exception {
		SubversionMirrorStorage storage = getStorage( mirror , false , null );
		return( storage );
	}

	@Override
	public boolean verifyRepository( String repo , String pathToRepo ) {
		String url = res.BASEURL;
		if( !pathToRepo.isEmpty() )
			url += "/" + pathToRepo;
		url += "/" + repo;
		
		try {
			if( checkSvnPathExists( url ) )
				return( true );
		}
		catch( Throwable e ) {
			action.log( "verify repository" , e );
		}
		return( false );
	}
	
	// implementation
	private SubversionMirrorStorage getStorage( ServerMirrorRepository mirror , boolean create , LocalFolder customRepoFolder ) throws Exception {
		SubversionMirrorStorage storage = new SubversionMirrorStorage( this , mirror , customRepoFolder );
		storage.create( create , false );
		return( storage );
	}
	
	private boolean checkSvnPathExists( String path ) throws Exception {
		int status = shell.customGetStatus( action , "svn info " + SVNAUTH + " " + path + " > " + shell.getOSDevNull() );
		if( status != 0 )
			return( false );
		return( true );
	}

	public String getRepositoryPath( ServerMirrorRepository mirror ) {
		String path = Common.getPath( mirror.RESOURCE_REPO , mirror.RESOURCE_DATA );
		if( !mirror.RESOURCE_ROOT.equals( "/" ) )
			path = Common.getPath( mirror.RESOURCE_ROOT , path );
		return( Common.getPath( SVNPATH , path ) );
	}

	public String getRepositoryRootPath( ServerMirrorRepository mirror ) {
		String path = mirror.RESOURCE_REPO;
		if( !mirror.RESOURCE_ROOT.equals( "/" ) )
			path = Common.getPath( mirror.RESOURCE_ROOT , path );
		return( Common.getPath( SVNPATH , path ) );
	}

	public String getProjectPath( MetaSourceProject project ) throws Exception {
		return( Common.getPath( SVNPATH , project.REPOPATH , project.REPOSITORY ) );
	}
	
	public boolean checkVersioned( ServerMirrorRepository mirror , String path ) throws Exception {
		String value = action.shell.customGetValue( action , "svn status " + path + " --depth empty" );
		if( value.startsWith( "?" ) )
			return( false );
		return( true );
	}
	
	public List<String> getFilesNotInSvn( ServerMirrorRepository mirror , LocalFolder pfMaster ) throws Exception {
		if( !checkVersioned( mirror , pfMaster.folderPath ) )
			action.exit1( _Error.NotUnderVersionControl1 , "folder=" + pfMaster.folderPath + " is not under verson control" , pfMaster.folderPath );
		
		String[] lines = action.shell.customGetLines( action , pfMaster.folderPath , "svn status" );
		List<String> values = new LinkedList<String>();
		for( String s : lines ) {
			if( s.startsWith( "?" ) ) {
				s = s.substring( 1 );
				s = s.trim();
				
				if( action.isLocalWindows() )
					values.add( Common.getLinuxPath( s ) );
				else
					values.add( s );
			}
		}
		return( values );
	}
	
}
