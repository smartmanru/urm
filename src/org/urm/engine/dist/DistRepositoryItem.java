package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;

public class DistRepositoryItem {

	public DistRepository repo;
	
	public String RELEASEDIR;
	public String DISTPATH;

	public Dist dist;
	
	private boolean modifyState;
	
	public DistRepositoryItem( DistRepository repo ) {
		this.repo = repo;
		modifyState = false;
	}
	
	public DistRepositoryItem copy( DistRepository rrepo ) {
		DistRepositoryItem r = new DistRepositoryItem( rrepo );
		r.RELEASEDIR = RELEASEDIR;
		r.DISTPATH = DISTPATH;
		r.dist = dist;
		return( r );
	}
	
	public synchronized void modify( boolean done ) throws Exception {
		if( !done ) {
			if( modifyState )
				Common.exitUnexpected();
			modifyState = true;
		}
		else {
			if( !modifyState )
				Common.exitUnexpected();
			modifyState = false;
		}
	}
	
	public void setRepository( DistRepository repo ) {
		this.repo = repo;
	}
	
	public Dist read( ActionBase action , RemoteFolder distFolder , ReleaseDist releaseDist ) throws Exception {
		RELEASEDIR = distFolder.folderName;
		if( !distFolder.checkExists( action ) ) {
			String path = distFolder.getLocalPath( action );
			action.exit1( _Error.MissingRelease1 , "release does not exist at " + path , path );
		}
		
		dist = new Dist( repo.meta , this , releaseDist , distFolder );
		dist.setFolder( distFolder );
		dist.loadState( action );
		return( dist );
	}

	public void createItem( ActionBase action , String releaseDir , String distPath ) throws Exception {
		this.dist = null;
		this.RELEASEDIR = releaseDir;
		this.DISTPATH = distPath;
	}
	
	public void createItemFolder( ActionBase action ) throws Exception {
		RemoteFolder distFolder = repo.getDistFolder( action , this );
		if( distFolder.checkExists( action ) ) {
			String path = distFolder.getLocalPath( action );
			action.ifexit( _Error.ReleaseAlreadyExists1 , "distributive already exists at " + path , new String[] { path } );
		}
		
		distFolder.ensureExists( action );
	}
	
	public void setDist( Dist dist ) throws Exception {
		if( !RELEASEDIR.equals( dist.RELEASEDIR ) )
			Common.exitUnexpected();
		
		this.dist = dist;
	}
	
	public Dist createDistMaster( ActionBase action , RemoteFolder distFolder , ReleaseDist releaseDist ) throws Exception {
		if( distFolder.checkExists( action ) ) {
			String path = distFolder.getLocalPath( action );
			action.ifexit( _Error.ReleaseAlreadyExists1 , "distributive already exists at " + path , new String[] { path } );
			
			if( action.isForced() )
				distFolder.removeFiles( action , "history.txt state.txt" );
		}
		else
			distFolder.ensureExists( action );
		
		Release release = releaseDist.release;
		Dist dist = new Dist( repo.meta , this , releaseDist , distFolder );
		dist.setFolder( distFolder );
		dist.createMaster( action );
		distFolder.createFileFromString( action , DistRepository.RELEASEHISTORYFILE , getHistoryRecord( action , release.RELEASEVER , "add" ) );
		return( dist );
	}
	
	private static String getHistoryRecord( ActionBase action , String RELEASEVER , String operation ) throws Exception {
		String s = Common.getNameTimeStamp() + ":" + operation + ":" + RELEASEVER;
		return( s );
	}

}
