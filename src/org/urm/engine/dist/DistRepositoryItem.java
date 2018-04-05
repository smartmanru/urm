package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumScopeCategoryType;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.storage.RemoteFolder;
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
	
	public DistRepositoryItem copy( DistRepository rrepo , ReleaseDist rreleaseDist ) {
		DistRepositoryItem r = new DistRepositoryItem( rrepo );
		r.RELEASEDIR = RELEASEDIR;
		r.DISTPATH = DISTPATH;
		r.dist = dist.copy( rrepo.meta , r , rreleaseDist );
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

	public void createItem( ActionBase action , ReleaseLabelInfo info ) throws Exception {
		this.dist = null;
		this.RELEASEDIR = info.RELEASEDIR;
		this.DISTPATH = info.DISTPATH;
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
		
		Dist dist = new Dist( repo.meta , this , releaseDist , distFolder );
		dist.setFolder( distFolder );
		dist.createMaster( action );
		return( dist );
	}
	
	public void copyFiles( EngineMethod method , ActionBase action , Dist src ) throws Exception {
		if( dist == null )
			Common.exitUnexpected();
		
		ReleaseDistScope srcScope = ReleaseDistScope.createScope( src.release );
		ReleaseDistScope dstScope = ReleaseDistScope.createScope( dist.release );
		for( ReleaseDistScopeSet set : dstScope.getSets() ) {
			ReleaseDistScopeSet srcSet = srcScope.findCategorySet( set.CATEGORY );
			if( srcSet == null )
				continue;
			
			for( ReleaseDistScopeDelivery delivery : set.getDeliveries() ) {
				ReleaseDistScopeDelivery srcDelivery = srcSet.findDelivery( delivery.distDelivery.NAME );
				if( srcDelivery == null )
					continue;

				if( delivery.CATEGORY == DBEnumScopeCategoryType.DB ) {
					copyFilesDeliveryDatabase( method , action , src , delivery , srcDelivery );
					continue;
				}
				
				for( ReleaseDistScopeDeliveryItem item : delivery.getItems() ) {
					if( item.isBinary() ) {
						ReleaseDistScopeDeliveryItem srcItem = srcDelivery.findBinaryItem( item.binary );
						if( srcItem != null )
							copyFilesBinaryItem( method , action , src , item , srcItem );
					}
					else
					if( item.isConf() ) {
						ReleaseDistScopeDeliveryItem srcItem = srcDelivery.findConfItem( item.conf );
						if( srcItem != null )
							copyFilesConfItem( method , action , src , item , srcItem );
					}
					else
					if( item.isDoc() ) {
						ReleaseDistScopeDeliveryItem srcItem = srcDelivery.findDoc( item.doc );
						if( srcItem != null )
							copyFilesDoc( method , action , src , item , srcItem );
					}
				}
			}
		}
	}

	private void copyFilesDeliveryDatabase( EngineMethod method , ActionBase action , Dist src , ReleaseDistScopeDelivery delivery , ReleaseDistScopeDelivery srcDelivery ) throws Exception {
		dist.copyDatabaseDistrToDistr( action , delivery.distDelivery , src );
	}

	private void copyFilesBinaryItem( EngineMethod method , ActionBase action , Dist src , ReleaseDistScopeDeliveryItem item , ReleaseDistScopeDeliveryItem srcItem ) throws Exception {
		DistItemInfo info = src.getDistItemInfo( action , item.binary , false , false );
		if( info.isFound() )
			dist.copyFileDistrToDistr( action , item.distDelivery , src , info.getDeliveryItemPath() );
	}
	
	private void copyFilesConfItem( EngineMethod method , ActionBase action , Dist src , ReleaseDistScopeDeliveryItem item , ReleaseDistScopeDeliveryItem srcItem ) throws Exception {
		DistItemInfo info = src.getDistItemInfo( action , item.conf );
		if( info.isFound() )
			dist.copyDirDistrToDistr( action , item.distDelivery , src , info.getDeliveryItemPath() );
	}
	
	private void copyFilesDoc( EngineMethod method , ActionBase action , Dist src , ReleaseDistScopeDeliveryItem item , ReleaseDistScopeDeliveryItem srcItem ) throws Exception {
		DistItemInfo info = src.getDistItemInfo( action , item.distDelivery , item.doc , false , false );
		if( info.isFound() )
			dist.copyFileDistrToDistr( action , item.distDelivery , src , info.getDeliveryItemPath() );
	}
	
}
