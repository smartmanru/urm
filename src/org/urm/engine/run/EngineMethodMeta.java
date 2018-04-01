package org.urm.engine.run;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class EngineMethodMeta {

	public EngineMethod method;
	public Meta meta;

	private ReleaseRepository updateReleaseRepository;
	private DistRepository updateDistRepository;
	private Map<String,EngineMethodMetaRelease> mapReleases;
	private Map<String,EngineMethodMetaDistItem> mapDistItems;
	
	public EngineMethodMeta( EngineMethod method , Meta meta ) {
		this.method = method;
		this.meta = meta;
		
		mapReleases = new HashMap<String,EngineMethodMetaRelease>();
		mapDistItems = new HashMap<String,EngineMethodMetaDistItem>(); 
	}
	
	public synchronized ReleaseRepository changeReleaseRepository() throws Exception {
		if( updateReleaseRepository == null ) {
			ReleaseRepository repo = meta.getReleaseRepository();
			repo.modify( false );
			updateReleaseRepository = repo.copy( meta , meta.getReleases() );
		}
		return( updateReleaseRepository );
	}

	public synchronized ReleaseRepository getReleaseRepository() {
		if( updateReleaseRepository != null )
			return( updateReleaseRepository );
		return( meta.getReleaseRepository() );
	}
	
	public synchronized DistRepository getDistRepository() {
		if( updateDistRepository != null )
			return( updateDistRepository );
		return( meta.getDistRepository() );
	}
	
	public synchronized void checkUpdateReleaseRepository( ReleaseRepository repo ) throws Exception {
		if( updateReleaseRepository == null || updateReleaseRepository != repo )
			Common.exitUnexpected();
	}
	
	public synchronized DistRepository changeDistRepository() throws Exception {
		if( updateDistRepository == null ) {
			DistRepository repo = meta.getDistRepository();
			repo.modify( false );
			updateDistRepository = repo.copy( meta , getReleaseRepository() );
		}
		return( updateDistRepository );
	}
	
	public synchronized void checkUpdateDistRepository( DistRepository repo ) throws Exception {
		if( updateDistRepository == null || updateDistRepository != repo )
			Common.exitUnexpected();
	}

	public synchronized void createRelease( Release release ) throws Exception {
		if( updateReleaseRepository == null || updateReleaseRepository != release.repo )
			Common.exitUnexpected();
		
		EngineMethodMetaRelease emmr = mapReleases.get( release.RELEASEVER );
		if( emmr != null )
			Common.exitUnexpected();
		
		emmr = new EngineMethodMetaRelease( this , release );
		emmr.setCreated();
		mapReleases.put( release.RELEASEVER , emmr );
	}

	public synchronized Release updateRelease( Release release ) throws Exception {
		changeReleaseRepository();
		
		EngineMethodMetaRelease emmr = mapReleases.get( release.RELEASEVER );
		if( emmr == null ) {
			emmr = new EngineMethodMetaRelease( this , release );
			emmr.setUpdated();
			mapReleases.put( release.RELEASEVER , emmr );
		}
		
		if( emmr.releaseNew == null )
			Common.exitUnexpected();
		return( emmr.releaseNew );
	}

	public synchronized void deleteRelease( Release release ) throws Exception {
		if( updateReleaseRepository == null )
			Common.exitUnexpected();
		
		EngineMethodMetaRelease emmr = mapReleases.get( release.RELEASEVER );
		if( emmr != null )
			Common.exitUnexpected();
		
		emmr = new EngineMethodMetaRelease( this , release );
		emmr.setDeleted();
		mapReleases.put( release.RELEASEVER , emmr );
	}

	public synchronized void checkUpdateDistItem( DistRepositoryItem item ) throws Exception {
		EngineMethodMetaDistItem emmd = mapDistItems.get( item.RELEASEDIR );
		if( emmd == null )
			Common.exitUnexpected();
	}
	
	public synchronized void checkUpdateRelease( Release release ) throws Exception {
		EngineMethodMetaRelease emmr = mapReleases.get( release.RELEASEVER );
		if( emmr == null )
			Common.exitUnexpected();
	}
	
	public synchronized void createDistItem( DistRepositoryItem item ) throws Exception {
		if( updateDistRepository == null || updateDistRepository != item.repo )
			Common.exitUnexpected();
		
		EngineMethodMetaDistItem emmd = mapDistItems.get( item.RELEASEDIR );
		if( emmd != null )
			Common.exitUnexpected();
		
		emmd = new EngineMethodMetaDistItem( this , item );
		emmd.setCreated();
		mapDistItems.put( item.RELEASEDIR , emmd );
	}
	
	public synchronized DistRepositoryItem updateDistItem( DistRepositoryItem item ) throws Exception {
		changeDistRepository();
		
		EngineMethodMetaDistItem emmd = mapDistItems.get( item.RELEASEDIR );
		if( emmd == null ) {
			emmd = new EngineMethodMetaDistItem( this , item );
			emmd.setUpdated( getReleaseRepository() );
			mapDistItems.put( item.RELEASEDIR , emmd );
		}
		
		if( emmd.itemNew == null )
			Common.exitUnexpected();
		return( emmd.itemNew );
	}

	public synchronized void deleteDistItem( DistRepositoryItem item ) throws Exception {
		EngineMethodMetaDistItem emmd = mapDistItems.get( item.RELEASEDIR );
		if( emmd != null )
			Common.exitUnexpected();
		
		emmd = new EngineMethodMetaDistItem( this , item );
		emmd.setDeleted();
		mapDistItems.put( item.RELEASEDIR , emmd );
	}
	
	public synchronized void commit() throws Exception {
		if( updateReleaseRepository != null ) {
			for( EngineMethodMetaRelease emmr : mapReleases.values() )
				emmr.commit();
				
			meta.setReleaseRepository( updateReleaseRepository );
		}
		
		if( updateDistRepository != null ) {
			for( EngineMethodMetaDistItem emmd : mapDistItems.values() )
				emmd.commit();
			
			meta.setDistRepository( updateDistRepository );
			return;
		}
	}
	
	public synchronized void abort() throws Exception {
		if( updateReleaseRepository != null ) {
			ReleaseRepository repo = meta.getReleaseRepository();
			repo.modify( true );
		}
			
		if( updateDistRepository != null ) {
			DistRepository repo = meta.getDistRepository();
			repo.modify( true );
		}
			
		for( EngineMethodMetaRelease emmr : mapReleases.values() )
			emmr.abort();
		for( EngineMethodMetaDistItem emmd : mapDistItems.values() )
			emmd.abort();
	}
	
}
