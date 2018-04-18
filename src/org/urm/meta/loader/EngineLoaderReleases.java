package org.urm.meta.loader;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.release.DBProductReleases;
import org.urm.db.release.DBRelease;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductReleases;
import org.urm.engine.storage.ProductStorage;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineLoaderReleases {

	public static String XML_ROOT_RELEASE = "release";
	
	public EngineLoader loader;
	public ProductMeta set;
	public Meta meta;
	
	public EngineLoaderReleases( EngineLoader loader , ProductMeta set ) {
		this.loader = loader;
		this.set = set;
		this.meta = set.meta;
	}

	public void createAll( boolean forceClearMeta , boolean forceClearDist ) throws Exception {
		ReleaseRepository repo = DBProductReleases.createdb( loader , meta , forceClearMeta , forceClearDist );
		set.setReleaseRepository( repo );

		// distributives
		EngineProduct ep = set.ep;
		EngineProductReleases releases = ep.getReleases();
		ActionBase action = loader.getAction();
		DistRepository distrepo = DistRepository.createInitialRepository( action , releases , forceClearDist );
		releases.setDistRepository( distrepo );
	}
	
	public void loadReleases( ProductMeta set , boolean importxml ) throws Exception {
		ReleaseRepository repo = DBProductReleases.loaddb( loader , meta , importxml );
		set.setReleaseRepository( repo );
		
		// distributives
		EngineProduct ep = set.ep;
		EngineProductReleases releases = ep.getReleases();
		ActionBase action = loader.getAction();
		try {
			DistRepository distrepo = DistRepository.loadDistRepository( action , releases , importxml );
			releases.setDistRepository( distrepo );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductReleases1 , e , "unable to load release repository, product=" + set.NAME , set.NAME );
		}
	}
	
	public void exportxmlReleaseDist( Release release , ReleaseDist releaseDist , String filePath ) throws Exception {
		ActionBase action = loader.getAction();
		action.debug( "export release distributive file ..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_RELEASE );
		Element root = doc.getDocumentElement();

		DBRelease.exportxml( loader , release , releaseDist , doc , root );
		ProductStorage.saveDoc( doc , filePath );
	}

	public void importxmlReleaseDist( Release release , ReleaseDist releaseDist , Dist dist , String file ) throws Exception {
		ActionBase action = loader.getAction();
		action.debug( "import release distributive data ..." );
		Document doc = action.readXmlFile( file );
		Node root = doc.getDocumentElement();
		DBRelease.importxml( loader , release , releaseDist , dist , root );
	}
	
}
