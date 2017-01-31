package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.storage.RemoteFolder;
import org.w3c.dom.Node;

public class DistRepositoryItem {

	DistRepository repo;
	Dist dist;
	
	public DistRepositoryItem( DistRepository repo ) {
		this.repo = repo;
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		String dir = ConfReader.getAttrValue( root , "releasedir" );
		RemoteFolder folder = repo.repoFolder.getSubFolder( action , dir );
		dist = read( action , repo , folder );
	}

	public static Dist read( ActionBase action , DistRepository repo , RemoteFolder distFolder ) throws Exception {
		if( !distFolder.checkExists( action ) )
			action.exit1( _Error.MissingRelease1 , "release does not exist at " + distFolder.folderPath , distFolder.folderPath );
		
		boolean prod = distFolder.folderName.equals( "prod" );
		Dist dist = new Dist( repo.meta , repo );
		dist.setFolder( distFolder , prod );
		dist.load( action );
		return( dist );
	}

	public static Dist create( ActionBase action , DistRepository repo , RemoteFolder distFolder ) throws Exception {
		if( distFolder.checkExists( action ) ) {
			String path = distFolder.folderPath;
			action.ifexit( _Error.ReleaseAlreadyExists1 , "distributive already exists at " + path , new String[] { path } );
		}

		Dist dist = new Dist( repo.meta , repo );
		dist.setFolder( distFolder , false );
		dist.create( action , distFolder.folderName );
		return( dist );
	}
	
	public static Dist createProd( ActionBase action , DistRepository repo , RemoteFolder distFolder , String RELEASEVER ) throws Exception {
		if( distFolder.checkExists( action ) ) {
			String path = distFolder.folderPath;
			action.ifexit( _Error.ReleaseAlreadyExists1 , "distributive already exists at " + path , new String[] { path } );
		}

		if( action.context.CTX_FORCE ) {
			distFolder.removeFiles( action , "history.txt state.txt" );
		}
		else {
			if( distFolder.checkFileExists( action , DistRepository.RELEASEHISTORYFILE ) )
				action.exit1( _Error.ProdFolderAlreadyInitialized1 , "prod folder is probably already initialized, delete history.txt manually to recreate" , distFolder.folderPath );
		}
		
		Dist dist = new Dist( repo.meta , repo );
		dist.setFolder( distFolder , true );
		dist.createProd( action , distFolder.folderName );
		distFolder.createFileFromString( action , DistRepository.RELEASEHISTORYFILE , getHistoryRecord( action , RELEASEVER , "add" ) );
		dist.finish( action );
		return( dist );
	}
	
	private static String getHistoryRecord( ActionBase action , String RELEASEVER , String operation ) throws Exception {
		String s = Common.getNameTimeStamp() + ":" + operation + ":" + RELEASEVER;
		return( s );
	}

}
