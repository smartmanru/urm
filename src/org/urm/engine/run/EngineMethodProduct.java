package org.urm.engine.run;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.EventService;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductReleases;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class EngineMethodProduct {

	public EngineMethod method;
	public EngineProduct ep;

	private Map<Integer,ReleaseRepository> updateReleaseRepositories;
	private Map<String,EngineMethodProductRelease> mapReleases;
	private Map<String,EngineMethodProductDist> mapDist;
	private DistRepository updateDistRepository;
	
	public EngineMethodProduct( EngineMethod method , EngineProduct ep ) {
		this.method = method;
		this.ep = ep;
		
		updateReleaseRepositories = new HashMap<Integer,ReleaseRepository>(); 
		mapReleases = new HashMap<String,EngineMethodProductRelease>();
		mapDist = new HashMap<String,EngineMethodProductDist>(); 
	}

	public ReleaseRepository changeReleaseRepository( Meta meta ) throws Exception {
		return( changeReleaseRepository( meta.getStorage() ) );
	}
	
	public synchronized ReleaseRepository changeReleaseRepository( ProductMeta storage ) throws Exception {
		ReleaseRepository updateReleaseRepository = updateReleaseRepositories.get( storage.ID );
		if( updateReleaseRepository == null ) {
			ReleaseRepository repo = storage.getReleaseRepository();
			repo.modify( false );
			updateReleaseRepository = repo.copy( storage.meta );
			updateReleaseRepositories.put( storage.ID , updateReleaseRepository );
		}
		return( updateReleaseRepository );
	}

	public synchronized ReleaseRepository getReleaseRepository( ProductMeta storage ) {
		ReleaseRepository updateReleaseRepository = updateReleaseRepositories.get( storage.ID );
		if( updateReleaseRepository != null )
			return( updateReleaseRepository );
		return( storage.getReleaseRepository() );
	}
	
	public synchronized DistRepository getDistRepository() {
		if( updateDistRepository != null )
			return( updateDistRepository );
		
		EngineProductReleases releases = ep.getReleases();
		return( releases.getDistRepository() );
	}
	
	public synchronized void checkUpdateReleaseRepository( ReleaseRepository repo ) throws Exception {
		ReleaseRepository updateReleaseRepository = updateReleaseRepositories.get( repo.meta.getId() );
		if( updateReleaseRepository == null || updateReleaseRepository != repo )
			Common.exitUnexpected();
	}
	
	public synchronized DistRepository changeDistRepository() throws Exception {
		if( updateDistRepository == null ) {
			EngineProductReleases releases = ep.getReleases();
			DistRepository repo = releases.getDistRepository();
			repo.modify( false );
			updateDistRepository = repo.copy( releases );
		}
		return( updateDistRepository );
	}
	
	public synchronized void checkUpdateDistRepository( DistRepository repo ) throws Exception {
		if( updateDistRepository == null || updateDistRepository != repo )
			Common.exitUnexpected();
	}

	public synchronized void createRelease( Release release ) throws Exception {
		checkUpdateReleaseRepository( release.repo );
		
		EngineMethodProductRelease emmr = mapReleases.get( release.RELEASEVER );
		if( emmr != null )
			Common.exitUnexpected();
		
		emmr = new EngineMethodProductRelease( this , release );
		emmr.setCreated();
		mapReleases.put( release.RELEASEVER , emmr );
	}

	public synchronized Release updateRelease( Release release ) throws Exception {
		ReleaseRepository updateReleaseRepository = changeReleaseRepository( release.getMeta() );
		release = updateReleaseRepository.getRelease( release.ID );
		
		EngineMethodProductRelease emmr = mapReleases.get( release.RELEASEVER );
		if( emmr == null ) {
			emmr = new EngineMethodProductRelease( this , release );
			emmr.setUpdated();
			mapReleases.put( release.RELEASEVER , emmr );
		}
		
		if( emmr.releaseNew == null )
			Common.exitUnexpected();
		return( emmr.releaseNew );
	}

	public synchronized void deleteRelease( Release release ) throws Exception {
		checkUpdateReleaseRepository( release.repo );
		
		EngineMethodProductRelease emmr = mapReleases.get( release.RELEASEVER );
		if( emmr != null )
			Common.exitUnexpected();
		
		emmr = new EngineMethodProductRelease( this , release );
		emmr.setDeleted();
		mapReleases.put( release.RELEASEVER , emmr );
	}

	public synchronized void checkUpdateDistItem( DistRepositoryItem item ) throws Exception {
		EngineMethodProductDist emmd = mapDist.get( item.RELEASEDIR );
		if( emmd == null )
			Common.exitUnexpected();
	}
	
	public synchronized void checkUpdateRelease( Release release ) throws Exception {
		EngineMethodProductRelease emmr = mapReleases.get( release.RELEASEVER );
		if( emmr == null )
			Common.exitUnexpected();
	}
	
	public synchronized void createDistItem( DistRepositoryItem item ) throws Exception {
		if( updateDistRepository == null || updateDistRepository != item.repo )
			Common.exitUnexpected();
		
		EngineMethodProductDist emmd = mapDist.get( item.RELEASEDIR );
		if( emmd != null )
			Common.exitUnexpected();
		
		emmd = new EngineMethodProductDist( this , item );
		emmd.setCreated();
		mapDist.put( item.RELEASEDIR , emmd );
	}
	
	public synchronized DistRepositoryItem updateDistItem( DistRepositoryItem item ) throws Exception {
		changeDistRepository();
		item = updateDistRepository.findNormalItem( item.RELEASEDIR );
		
		EngineMethodProductDist emmd = mapDist.get( item.RELEASEDIR );
		if( emmd == null ) {
			emmd = new EngineMethodProductDist( this , item );
			emmd.setUpdated();
			mapDist.put( item.RELEASEDIR , emmd );
		}
		
		if( emmd.itemNew == null )
			Common.exitUnexpected();
		return( emmd.itemNew );
	}

	public synchronized void deleteDistItem( DistRepositoryItem item ) throws Exception {
		EngineMethodProductDist emmd = mapDist.get( item.RELEASEDIR );
		if( emmd != null )
			Common.exitUnexpected();
		
		emmd = new EngineMethodProductDist( this , item );
		emmd.setDeleted();
		mapDist.put( item.RELEASEDIR , emmd );
	}
	
	public synchronized void commit() throws Exception {
		for( ReleaseRepository updateReleaseRepository : updateReleaseRepositories.values() ) {
			for( EngineMethodProductRelease emmr : mapReleases.values() )
				emmr.commit();
				
			Meta meta = updateReleaseRepository.meta;
			ProductMeta storage = meta.getStorage(); 
			storage.setReleaseRepository( updateReleaseRepository );
			ActionBase action = method.getAction();
			method.addCommitEvent( action.eventSource , EventService.OWNER_ENGINE , EventService.EVENT_RELEASEREPOCHANGED , updateReleaseRepository );
		}
		
		if( updateDistRepository != null ) {
			for( EngineMethodProductDist emmd : mapDist.values() )
				emmd.commit();
			
			EngineProductReleases releases = updateDistRepository.releases;
			releases.setDistRepository( updateDistRepository );
			ActionBase action = method.getAction();
			method.addCommitEvent( action.eventSource , EventService.OWNER_ENGINE , EventService.EVENT_DISTREPOCHANGED , updateDistRepository );
		}
	}
	
	public synchronized void abort() throws Exception {
		for( ReleaseRepository updateReleaseRepository : updateReleaseRepositories.values() ) {
			Meta meta = updateReleaseRepository.meta;
			ReleaseRepository repo = meta.getReleases();
			repo.modify( true );
		}
			
		if( updateDistRepository != null ) {
			EngineProductReleases releases = updateDistRepository.releases;
			DistRepository repo = releases.getDistRepository();
			repo.modify( true );
		}
			
		for( EngineMethodProductRelease emmr : mapReleases.values() )
			emmr.abort();
		for( EngineMethodProductDist emmd : mapDist.values() )
			emmd.abort();
	}
	
}
