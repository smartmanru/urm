package org.urm.server.vcs;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaSourceProject;
import org.urm.server.storage.Folder;
import org.urm.server.storage.LocalFolder;

public class SubversionVCS extends GenericVCS {

	String SVNPATH;
	String SVNAUTH;
	
	public SubversionVCS( ActionBase action , String SVNPATH , String SVNAUTH ) {
		super( action );
		this.SVNPATH = SVNPATH;
		this.SVNAUTH = SVNAUTH;
	}
	
	@Override public String getMainBranch() {
		return( "trunk" );
	}
	
	@Override public boolean checkout( LocalFolder PATCHFOLDER , MetaSourceProject project , String BRANCH ) throws Exception {
		String CO_PATH;
		String XBRANCH = BRANCH;
		if( !XBRANCH.equals( "trunk" ) )
			XBRANCH = "branches/" + BRANCH;
		
		CO_PATH = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/" + XBRANCH;

		String REVISION;
		if( action.isLinux() ) {
			REVISION = session.customGetValue( action , "svn info --non-interactive " + SVNAUTH + " " + CO_PATH + " | grep Revision | tr -d " + Common.getQuoted( " " ) + 
					" | cut -d " + Common.getQuoted( ":" ) + " -f2" );
		}
		else {
			REVISION = session.customGetValue( action , "svn info --non-interactive " + SVNAUTH + " " + CO_PATH + " | findstr Revision" );
			REVISION = Common.getListItem( REVISION , ":" , 1 );
		}
		
		REVISION = REVISION.trim();

		action.info( "svn: checkout sources from " + CO_PATH + " (branch=" + BRANCH + ", revision=" + REVISION + ") to " + PATCHFOLDER.folderPath + "..." );
		
		String ospath = action.getOSPath( PATCHFOLDER.folderPath );
		int status = session.customGetStatus( action , "svn co --non-interactive " + SVNAUTH + " " + CO_PATH + " " + ospath );

		if( status == 0 )
			return( true );
		
		action.error( "svn: having problem to check out " + CO_PATH );
		return( false );
	}

	@Override public boolean commit( LocalFolder PATCHFOLDER , MetaSourceProject project , String COMMENT ) throws Exception {
		if( !PATCHFOLDER.checkExists( action ) ) {
			action.error( "directory " + PATCHFOLDER.folderPath + " does not exist " );
			return( false );
		}
		
		String ospath = action.getOSPath( PATCHFOLDER.folderPath );
		session.customCheckStatus( action , "svn commit -m " + Common.getQuoted( COMMENT ) + " " + SVNAUTH + " " + ospath );
		return( true );
	}

	@Override public boolean copyBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		// check source status
		String BRANCH1X = BRANCH1;
		if( !BRANCH1X.equals( "trunk" ) )
			BRANCH1X = "branches/" + BRANCH1;
		
		String branch1Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/" + BRANCH1X;
		if( !checkSvnPathExists( branch1Path ) ) {
			action.error( branch1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		String BRANCH2X = BRANCH2;
		if( !BRANCH2X.equals( "trunk" ) )
			BRANCH2X = "branches/" + BRANCH2;
		
		String branch2Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/" + BRANCH2X;
		if( checkSvnPathExists( branch2Path ) ) {
			action.error( "cannot copy branch to branch - target branch already exists" );
			return( false );
		}

		session.customCheckStatus( action , "svn copy " + SVNAUTH + " " + branch1Path + " " + branch2Path + " -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: copy branch" ) );
		return( true );
	}

	@Override public boolean renameBranchToNewBranch( MetaSourceProject project , String BRANCH1 , String BRANCH2 ) throws Exception {
		// check source status
		String BRANCH1X = BRANCH1;
		if( !BRANCH1X.equals( "trunk" ) )
			BRANCH1X = "branches/" + BRANCH1;
		
		String branch1Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/" + BRANCH1X;
		if( !checkSvnPathExists( branch1Path ) ) {
			action.error( branch1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		String BRANCH2X = BRANCH2;
		if( !BRANCH2X.equals( "trunk" ) )
			BRANCH2X = "branches/" + BRANCH2;
		
		String branch2Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/" + BRANCH2X;
		if( checkSvnPathExists( branch2Path ) ) {
			action.info( "skip rename branch to branch - target branch already exists" );
			return( false );
		}

		session.customCheckStatus( action , "svn rename " + SVNAUTH + " " + branch1Path + " " + branch2Path + " -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: rename branch" ) );
		return( true );
	}

	@Override public boolean copyTagToNewTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		// check source status
		String tag1Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/tags/" + TAG1;
		if( !checkSvnPathExists( tag1Path ) ) {
			action.error( tag1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		String tag2Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/tags/" + TAG2;
		if( checkSvnPathExists( tag2Path ) ) {
			action.error( "cannot copy tag - target tag already exists" );
			return( false );
		}

		session.customCheckStatus( action , "svn copy " + SVNAUTH + " " + tag1Path + " " + tag2Path + " -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: copy tag" ) );
		return( true );
	}

	@Override public boolean copyTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		// check source status
		String tag1Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/tags/" + TAG1;
		if( !checkSvnPathExists( tag1Path ) ) {
			action.error( tag1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		String tag2Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/tags/" + TAG2;
		if( checkSvnPathExists( tag2Path ) ) {
			action.info( "drop already existing new tag ..." );
			session.customCheckStatus( action , "svn delete " + SVNAUTH + " " + tag2Path + " -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: drop tag before svn copy" ) );
		}

		session.customCheckStatus( action , "svn copy " + SVNAUTH + " " + tag1Path + " " + tag2Path + " -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: copy tag" ) );
		return( true );
	}
	
	@Override public boolean renameTagToTag( MetaSourceProject project , String TAG1 , String TAG2 ) throws Exception {
		// check source status
		String tag1Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/tags/" + TAG1;
		if( !checkSvnPathExists( tag1Path ) ) {
			action.error( tag1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		String tag2Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/tags/" + TAG2;
		if( checkSvnPathExists( tag2Path ) ) {
			action.info( "drop already existing new tag ..." );
			session.customCheckStatus( action , "svn delete " + SVNAUTH + " " + tag2Path + " -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: drop tag before svn rename" ) );
		}

		session.customCheckStatus( action , "svn rename " + SVNAUTH + " " + tag1Path + " " + tag2Path + " -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: rename tag" ) );
		return( true );
	}
	
	@Override public boolean copyTagToNewBranch( MetaSourceProject project , String TAG1 , String BRANCH2 ) throws Exception {
		// check source status
		String tag1Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/tags/" + TAG1;
		if( !checkSvnPathExists( tag1Path ) ) {
			action.error( tag1Path + ": svn path does not exist" );
			return( false );
		}

		// check destination status
		String BRANCH2X = BRANCH2;
		if( !BRANCH2X.equals( "trunk" ) )
			BRANCH2X = "branches/" + BRANCH2;
		String branch2Path = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/" + BRANCH2X;
		if( checkSvnPathExists( branch2Path ) ) {
			action.error( "cannot copy tag to branch - target branch already exists" );
			return( false );
		}

		session.customCheckStatus( action , "svn copy " + SVNAUTH + " " + tag1Path + " " + branch2Path + " -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: copy tag to branch" ) );
		return( true );
	}
	
	@Override public boolean dropTag( MetaSourceProject project , String TAG ) throws Exception {
		// check status
		String tagPath = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/tags/" + TAG;
		if( !checkSvnPathExists( tagPath ) ) {
			action.error( tagPath + ": svn path does not exist" );
			return( false );
		}
		
		session.customCheckStatus( action , "svn delete " + SVNAUTH + " " + tagPath + " -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: drop tag" ) );
		return( true );
	}
	
	@Override public boolean dropBranch( MetaSourceProject project , String BRANCH ) throws Exception {
		// check source status
		String BRANCHX = BRANCH;
		if( !BRANCHX.equals( "trunk" ) )
			BRANCHX = "branches/" + BRANCH;
		String branchPath = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/" + BRANCHX;
		if( !checkSvnPathExists( branchPath ) ) {
			action.error( branchPath + ": svn path does not exist" );
			return( false );
		}
		
		session.customCheckStatus( action , "svn delete " + SVNAUTH + " " + branchPath + " -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: drop branch" ) );
		return( true );
	}
	
	@Override public boolean export( Folder PATCHFOLDER , MetaSourceProject project , String BRANCH , String TAG , String FILENAME ) throws Exception {
		String CO_PATH;
		if( !TAG.isEmpty() )
			CO_PATH = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/tags/" + TAG;
		else {
			if( BRANCH.equals( "trunk" ) )
				CO_PATH = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "trunk";
			else
				CO_PATH = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "branches/" + BRANCH;
		}
		
		if( !project.CODEPATH.isEmpty() )
			CO_PATH += "/" + project.CODEPATH;
		
		if( !FILENAME.isEmpty() )
			CO_PATH += "/" + FILENAME;

		if( FILENAME.isEmpty() ) {
			Folder BASEDIR = PATCHFOLDER.getParentFolder( action );
			if( !BASEDIR.checkExists( action ) )
				action.exit( "exportFromPath: local directory " + BASEDIR + " does not exist" );
			if( PATCHFOLDER.checkExists( action ) )
				action.exit( "exportFromPath: local directory " + PATCHFOLDER.folderPath + " should not exist" );
		}
		else {
			if( !PATCHFOLDER.checkExists( action ) )
				action.exit( "exportFromPath: local directory " + PATCHFOLDER.folderPath + " does not exist" );
		}

		String ospath = action.getOSPath( PATCHFOLDER.folderPath );
		session.customCheckStatus( action , "svn export --non-interactive " + SVNAUTH + " " + CO_PATH + " " + ospath );
		return( true );
	}

	@Override public boolean setTag( MetaSourceProject project , String BRANCH , String TAG , String BRANCHDATE ) throws Exception {
		// check source status
		String BRANCHX = BRANCH;
		if( !BRANCHX.equals( "trunk" ) )
			BRANCHX = "branches/" + BRANCH;
		String branchPath = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/" + BRANCHX;
		if( !checkSvnPathExists( branchPath ) ) {
			action.error( branchPath + ": svn path does not exist" );
			return( false );
		}
		
		String tagPath = SVNPATH + "/" + project.PATH + "/" + project.REPOSITORY + "/tags/" + TAG;
		if( checkSvnPathExists( tagPath ) ) {
			action.info( "drop already existing tag ..." );
			session.customCheckStatus( action , "svn delete " + SVNAUTH + " " + tagPath + " -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: drop tag before svn rename" ) );
		}
		
		if( !BRANCHDATE.isEmpty() )
			session.customCheckStatus( action , "svn copy " + SVNAUTH + " --revision {" + Common.getQuoted( BRANCHDATE ) + "} " + branchPath + " " + tagPath + 
					" -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: set tag on branch head by date " + BRANCHDATE ) );
		else
			session.customCheckStatus( action , "svn copy " + SVNAUTH + " " + branchPath + " " + tagPath + 
					" -m " + Common.getQuoted( meta.product.CONFIG_ADM_TRACKER + "-0000: set tag on branch head" ) );
		
		return( true );
	}
	
	// implementation
	private boolean checkSvnPathExists( String path ) throws Exception {
		int status = session.customGetStatus( action , "svn info " + SVNAUTH + " " + path + " > " + session.getOSDevNull() );
		if( status != 0 )
			return( false );
		return( true );
	}
	
	@Override public boolean isValidRepositoryMasterPath( String repository , String path ) throws Exception {
		String fullPath = SVNPATH + "/" + repository + "/" + path; 
		return( checkSvnPathExists( fullPath ) );
	}
	
	@Override public boolean isValidRepositoryTagPath( String repository , String TAG , String path ) throws Exception {
		String fullPath = SVNPATH + "/" + repository + "/tags/" + TAG + "/" + path; 
		return( checkSvnPathExists( fullPath ) );
	}
	
	@Override public boolean exportRepositoryMasterPath( LocalFolder PATCHFOLDER , String repository , String ITEMPATH , String name ) throws Exception {
		if( !isValidRepositoryMasterPath( repository , ITEMPATH ) )
			return( false );
			
		if( !PATCHFOLDER.checkExists( action ) )
			action.exit( "exportRepositoryMasterPath: local directory " + PATCHFOLDER.folderPath + " does not exist" );

		String CO_PATH = SVNPATH + "/" + repository + "/" + ITEMPATH;
		if( name.isEmpty() )
			name = Common.getBaseName( ITEMPATH );
		
		session.customCheckStatus( action , PATCHFOLDER.folderPath , "svn export --non-interactive " + SVNAUTH + " " + CO_PATH + " " + name + " > " + session.getOSDevNull()  );
		return( true );
	}

	@Override public boolean exportRepositoryTagPath( LocalFolder PATCHFOLDER , String repository , String TAG , String ITEMPATH , String name ) throws Exception {
		if( !isValidRepositoryTagPath( repository , TAG , ITEMPATH ) )
			return( false );
		
		String TAGPATH = "tags/" + TAG + "/" + ITEMPATH;
		if( !PATCHFOLDER.checkExists( action ) )
			action.exit( "exportRepositoryTagPath: local directory " + PATCHFOLDER.folderPath + " does not exist" );

		String CO_PATH = SVNPATH + "/" + repository + "/" + TAGPATH;
		if( name.isEmpty() )
			name = Common.getBaseName( ITEMPATH );
		
		session.customCheckStatus( action , PATCHFOLDER.folderPath , "svn export --non-interactive " + SVNAUTH + " " + CO_PATH + " " + name + " > " + session.getOSDevNull() );
		return( true );
	}

	@Override public String getInfoMasterPath( String repository , String ITEMPATH ) throws Exception {
		String CO_PATH = SVNPATH + "/" + repository + "/" + ITEMPATH;
		return( CO_PATH );
	}

	@Override public boolean createMasterFolder( String repository , String ITEMPATH , String commitMessage ) throws Exception {
		String fullPath = SVNPATH + "/" + repository + "/" + ITEMPATH; 
		session.customCheckStatus( action , "svn mkdir " + SVNAUTH + " -m " + Common.getQuoted( commitMessage ) + " --parents " + Common.getQuoted( fullPath ) + " > " + session.getOSDevNull() );
		return( true );
	}
	
	@Override public boolean moveMasterFiles( String repository , String srcFolder , String dstFolder , String itemPath , String commitMessage ) throws Exception {
		String F_ITEMDIR = Common.getDirName( itemPath );
		String dstFullPath = SVNPATH + "/" + repository + "/" + dstFolder + "/" + F_ITEMDIR; 
		String srcFullPath = SVNPATH + "/" + repository + "/" + srcFolder + "/" + itemPath; 
		session.customCheckStatus( action , "svn mkdir " + SVNAUTH + " -m " + Common.getQuoted( commitMessage ) + " --parents " + Common.getQuoted( srcFullPath ) + " > " + session.getOSDevNull() );
		session.customCheckStatus( action , "svn rename " + SVNAUTH + " -m " + Common.getQuoted( commitMessage ) + " " + Common.getQuoted( srcFullPath ) + " " + Common.getQuoted( dstFullPath ) + " > " + session.getOSDevNull() );
		return( true );
	}

	@Override public String listMasterItems( String repository , String masterFolder ) throws Exception {
		String fullPath = SVNPATH + "/" + repository + "/" + masterFolder;
		String s = session.customGetValue( action , "svn list " + SVNAUTH + " " + fullPath );
		s = Common.replace( s , "/" , "" );
		s = Common.replace( s , "\n" , " " );
		s.trim();
		return( s );
	}

	@Override public void deleteMasterFolder( String repository , String masterFolder , String commitMessage ) throws Exception {
		String fullPath = SVNPATH + "/" + repository + "/" + masterFolder;
		session.customCheckStatus( action , "svn delete -m " + Common.getQuoted( commitMessage ) + " " + SVNAUTH + " " + fullPath );
	}
	
	@Override public void checkoutMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder ) throws Exception {
		String fullPath = SVNPATH + "/" + repository + "/" + masterFolder;
		String ospath = action.getOSPath( PATCHPATH.folderPath );
		session.customCheckStatus( action , "svn co " + SVNAUTH + " " + fullPath + " " + ospath );
	}
	
	@Override public void importMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder , String commitMessage ) throws Exception {
		String fullPath = SVNPATH + "/" + repository + "/" + masterFolder;
		String ospath = action.getOSPath( PATCHPATH.folderPath );
		session.customCheckStatus( action , "svn import -m " + Common.getQuoted( commitMessage ) + " " + SVNAUTH + " " + ospath + " " + fullPath );
	}
	
	@Override public void ensureMasterFolderExists( String repository , String masterFolder , String commitMessage ) throws Exception {
		String fullPath = SVNPATH + "/" + repository + "/" + masterFolder;
		session.customCheckStatus( action , "svn mkdir --parents -m " + Common.getQuoted( commitMessage ) + " " + SVNAUTH + " " + fullPath );
	}
	
	@Override public boolean commitMasterFolder( LocalFolder PATCHPATH , String repository , String masterFolder , String commitMessage ) throws Exception {
		int status = session.customGetStatus( action , PATCHPATH.folderPath , "svn commit -m " + Common.getQuoted( commitMessage ) + " " + SVNAUTH );
		if( status != 0 )
			return( false );
		return( true );
	}
	
	@Override public void addFileToCommit( LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		String ospath = action.getOSPath( Common.getPath( folder , file ) );
		session.customCheckStatus( action , PATCHPATH.folderPath , "svn add " + ospath );
	}
	
	@Override public void deleteFileToCommit( LocalFolder PATCHPATH , String folder , String file ) throws Exception {
		String ospath = action.getOSPath( Common.getPath( folder , file ) );
		session.customCheckStatus( action , PATCHPATH.folderPath , "svn delete " + ospath );
	}
	
	@Override public void addDirToCommit( LocalFolder PATCHPATH , String folder ) throws Exception {
		String ospath = action.getOSPath( folder );
		session.customCheckStatus( action , PATCHPATH.folderPath , "svn add " + ospath );
	}
	
	@Override public void deleteDirToCommit( LocalFolder PATCHPATH , String folder ) throws Exception {
		String ospath = action.getOSPath( folder );
		session.customCheckStatus( action , PATCHPATH.folderPath , "svn delete " + ospath );
	}

	@Override public void createMasterTag( String repository , String masterFolder , String TAG , String commitMessage ) throws Exception {
		String fullPathSrc = SVNPATH + "/" + repository + "/" + masterFolder;
		String fullPathTag = SVNPATH + "/" + repository + "/tags/" + TAG;
		
		// check source status
		if( !checkSvnPathExists( fullPathSrc ) )
			action.exit( fullPathSrc + ": svn path does not exist" );

		// check destination status
		if( checkSvnPathExists( fullPathTag ) ) {
			action.info( "drop already existing new tag ..." );
			session.customCheckStatus( action , "svn delete " + SVNAUTH + " " + fullPathTag + " -m " + Common.getQuoted( commitMessage ) );
		}

		session.customCheckStatus( action , "svn copy " + SVNAUTH + " " + fullPathSrc + " " + fullPathTag + " -m " + Common.getQuoted( commitMessage ) );
	}
	
	public List<String> getFilesNotInSvn( ActionBase action , LocalFolder pfMaster ) throws Exception {
		String[] lines = action.session.customGetLines( action , pfMaster.folderPath , "svn status" );
		List<String> values = new LinkedList<String>();
		for( String s : lines ) {
			if( s.startsWith( "?" ) ) {
				s = s.substring( 1 );
				s = s.trim();
				
				if( action.session.account.isWindows() )
					values.add( Common.getLinuxPath( s ) );
				else
					values.add( s );
			}
		}
		return( values );
	}
	
	public void addDirToSvn( ActionBase action , LocalFolder pfMaster , String dirPath ) throws Exception {
		String cmd = "svn add " + action.getOSPath( dirPath );
		action.trace( "addDirToSvn: " + cmd );
		action.session.custom( action , pfMaster.folderPath , cmd );
	}

	public void addFileToSvn( ActionBase action , LocalFolder pfMaster , String filePath ) throws Exception {
		String cmd = "svn add " + action.getOSPath( filePath );
		action.trace( "addFileToSvn: " + cmd );
		action.session.custom( action , pfMaster.folderPath , cmd );
	}
	
	public void deleteDirFromSvn( ActionBase action , LocalFolder pfMaster , String dirPath ) throws Exception {
		String cmd = "svn delete " + action.getOSPath( dirPath );
		action.trace( "deleteDirFromSvn: " + cmd );
		action.session.custom( action , pfMaster.folderPath , cmd );
	}

	public void deleteFileFromSvn( ActionBase action , LocalFolder pfMaster , String filePath ) throws Exception {
		String cmd = "svn delete " + action.getOSPath( filePath );
		action.trace( "deleteFileFromSvn: " + cmd );
		action.session.custom( action , pfMaster.folderPath , cmd );
	}
	
}
