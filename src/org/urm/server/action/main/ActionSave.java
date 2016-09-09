package org.urm.server.action.main;

import java.util.List;

import org.urm.common.Common;
import org.urm.common.meta.MainCommandMeta;
import org.urm.server.ServerDirectory;
import org.urm.server.ServerMirrorRepository;
import org.urm.server.action.ActionBase;
import org.urm.server.storage.FileSet;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.UrmStorage;
import org.urm.server.vcs.GenericVCS;
import org.urm.server.vcs.SubversionVCS;

public class ActionSave extends ActionBase {

	LocalFolder pfMaster = null;
	SubversionVCS vcs = null;
	
	public ActionSave( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeSimple() throws Exception {
		UrmStorage urm = artefactory.getUrmStorage();
		LocalFolder pf = urm.getInstallFolder( this );
		LocalFolder pfProducts = urm.getServerProductsFolder( this );
		if( pfProducts.checkExists( this ) ) {
			context.session.setServerLayout( context.options );
			saveServer( pf );
		}
		else {
			context.session.setStandaloneLayout( context.options );
			saveProduct( pf , true );
		}
		return( true );
	}

	private void saveServer( LocalFolder pf ) throws Exception {
		info( "save master ..." );
		saveProduct( pf , false );
		
		UrmStorage urm = artefactory.getUrmStorage();
		ServerDirectory directory = actionInit.getDirectory();
		for( String name : directory.getProducts() ) {
			info( "save product=" + name + " ..." );
			actionInit.setServerSystemProductLayout( name );
			
			LocalFolder folder = urm.getProductFolder( this );
			saveProduct( folder , false );
			actionInit.clearServerProductLayout();
		}
	}
	
	private void saveProduct( LocalFolder pf , boolean standalone ) throws Exception {
		pfMaster = pf.getSubFolder( this , "master" );
		
		// read master file and make up all files to the list
		String masterPath = pfMaster.getFilePath( this , MainCommandMeta.MASTERFILE );
		List<String> lines = readFileLines( masterPath );
		FileSet set = pfMaster.getFileSet( this );
		
		vcs = GenericVCS.getSvnDirect( this , meta.product.CONFIG_URM_VCS_RESOURCE );
		ServerMirrorRepository mirror = super.getServerMirror();
		if( vcs.checkVersioned( mirror , pfMaster.folderPath ) ) {
			List<String> filesNotInSvn = vcs.getFilesNotInSvn( mirror , pfMaster );
			executeDir( set , lines , filesNotInSvn );
		}
		else
			vcs.addDirToSvn( mirror , pf , "master" );
			
		if( !vcs.commitMasterFolder( mirror , pfMaster , "" , "svnsave" ) )
			exit( "unable to save in svn folder=" + pfMaster.folderPath );
	}
	
	private void executeDir( FileSet set , List<String> lines , List<String> filesNotInSvn ) throws Exception {
		ServerMirrorRepository mirror = super.getServerMirror();
		for( FileSet dir : set.dirs.values() ) {
			// check dir in lines
			boolean dirInLines = false;
			for( String line : lines ) {
				String filePath = Common.getPartAfterFirst( line , ":" );
				if( filePath.startsWith( dir.dirPath ) ) {
					dirInLines = true;
					trace( "executeDir: dirInLines " + filePath + " in " + dir.dirPath );
					break;
				}
			}
			
			boolean dirInSvn = checkDirInSvn( dir.dirPath , filesNotInSvn );
			if( dirInLines && dirInSvn )
				executeDir( dir , lines , filesNotInSvn );
			else {
				if( dirInLines )
					vcs.addDirToSvn( mirror , pfMaster , dir.dirPath );
				else
					vcs.deleteDirFromSvn( mirror , pfMaster , dir.dirPath );
			}
		}
		
		for( String fileActual : set.files.values() ) {
			// check file in lines
			boolean fileInLines = false;
			for( String line : lines ) {
				String filePath = Common.getPartAfterFirst( line , ":" );
				if( fileActual.equals( filePath ) ) {
					fileInLines = true;
					trace( "executeDir: fileInLines " + filePath );
					break;
				}
			}
			
			boolean fileInSvn = checkFileInSvn( fileActual , filesNotInSvn );
			if( fileInLines && fileInSvn )
				continue;
			
			if( fileInLines )
				vcs.addFileToSvn( mirror , pfMaster , fileActual );
			else
				vcs.deleteFileFromSvn( mirror , pfMaster , fileActual );
		}
	}

	private boolean checkDirInSvn( String dirPath , List<String> filesNotInSvn ) throws Exception {
		for( String xMissing : filesNotInSvn ) {
			if( dirPath.equals( xMissing ) || dirPath.startsWith( xMissing + "/" ) ) {
				trace( "checkDirInSvn: false, dirPath=" + dirPath + ", filesNotInSvn=" + xMissing );
				return( false );
			}
		}
		
		trace( "checkDirInSvn: true, dirPath=" + dirPath );
		return( true );
	}
	
	private boolean checkFileInSvn( String filePath , List<String> filesNotInSvn ) throws Exception {
		for( String xMissing : filesNotInSvn ) {
			if( filePath.equals( xMissing ) || filePath.startsWith( xMissing + "/" ) ) {
				trace( "checkFileInSvn: false, filePath=" + filePath + ", filesNotInSvn=" + xMissing );
				return( false );
			}
		}
		
		trace( "checkFileInSvn: true, filePath=" + filePath );
		return( true );
	}

}
