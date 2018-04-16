package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.db.release.DBReleaseDist;
import org.urm.db.release.DBReleaseRepository;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineLoaderReleases;
import org.urm.meta.engine.ProductReleases;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;

public class ActionImportRelease extends ActionBase {

	public Meta meta;
	public Release release;
	public String RELEASELABEL;
	
	public ActionImportRelease( ActionBase action , String stream , Meta meta , String RELEASELABEL ) {
		super( action , stream , "Import release, product=" + meta.name + ", label=" + RELEASELABEL );
		this.meta = meta;
		this.RELEASELABEL = RELEASELABEL;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		ProductReleases releases = meta.getReleases();
		synchronized( releases ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( releases );
			DistRepository distrepoUpdated = method.changeDistRepository( releases );
			
			// create release
			ReleaseLabelInfo info = ReleaseLabelInfo.getLabelInfo( this , meta , RELEASELABEL );
			release = DBReleaseRepository.createReleaseNormal( method , this , repoUpdated , info );
			ReleaseDist releaseDist = DBReleaseDist.createReleaseDist( method , this , release , info.VARIANT );
			
			// create distributive
			DistRepositoryItem item = distrepoUpdated.attachRepositoryItem( method , this , info , releaseDist );
			
			// import meta
			LocalFolder workFolder = super.getWorkFolder();
			String fileName = Dist.META_FILENAME;
			Dist dist = item.dist;
			dist.openForUse( this );
			dist.copyDistToFolder( this , workFolder , fileName );
			
			EngineLoader loader = engine.createLoader( method , this );
			EngineLoaderReleases loaderReleases = new EngineLoaderReleases( loader , meta.getStorage() );
			String filePath = workFolder.getFilePath( this ,  fileName );
			DBReleaseDist.updateHash( method , this , release , releaseDist , dist );
			loaderReleases.importxmlReleaseDist( release , releaseDist , dist , filePath );
		}
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
