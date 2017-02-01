package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.dist.DistRepository.DistOperation;
import org.urm.engine.storage.RemoteFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DistRepositoryItem {

	public DistRepository repo;
	public Dist dist;
	
	public String RELEASEDIR; 
	public long created;
	
	public DistRepositoryItem( DistRepository repo ) {
		this.repo = repo;
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		RELEASEDIR = ConfReader.getAttrValue( root , "releasedir" );
		created = Long.parseLong( ConfReader.getAttrValue( root , "created" ) );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "releasedir" , dist.RELEASEDIR );
		Common.xmlSetElementAttr( doc , root , "created" , Long.toString( created ) );
	}

	public void read( ActionBase action , RemoteFolder distFolder ) throws Exception {
		dist = read( action , repo , distFolder );
	}
	
	public static Dist read( ActionBase action , DistRepository repo , RemoteFolder distFolder ) throws Exception {
		if( !distFolder.checkExists( action ) ) {
			String path = distFolder.getLocalPath( action );
			action.exit1( _Error.MissingRelease1 , "release does not exist at " + path , path );
		}
		
		boolean prod = distFolder.folderName.equals( "prod" );
		Dist dist = new Dist( repo.meta , repo );
		dist.setFolder( distFolder , prod );
		dist.load( action );
		return( dist );
	}

	public void createItem( ActionBase action , Dist dist ) throws Exception {
		this.dist = dist;
		created = System.currentTimeMillis();
	}
	
	public static Dist createDist( ActionBase action , DistRepository repo , RemoteFolder distFolder ) throws Exception {
		if( distFolder.checkExists( action ) ) {
			String path = distFolder.folderPath;
			action.ifexit( _Error.ReleaseAlreadyExists1 , "distributive already exists at " + path , new String[] { path } );
		}

		Dist dist = new Dist( repo.meta , repo );
		dist.setFolder( distFolder , false );
		dist.create( action , distFolder.folderName );
		return( dist );
	}
	
	public static Dist createProdDist( ActionBase action , DistRepository repo , RemoteFolder distFolder , String RELEASEVER ) throws Exception {
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

	public void addAction( ActionBase action , boolean success , DistOperation op , String msg ) throws Exception {
	}
	
	public void archiveItem( ActionBase action ) throws Exception {
	}
	
}
