package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.release.DBProductReleases;
import org.urm.db.release.DBRelease;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.storage.ProductStorage;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
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
		ProductReleases releases = new ProductReleases( set , meta );
		set.setReleases( releases );
		
		DBProductReleases.createdb( loader , releases , forceClearMeta , forceClearDist );

		// old
		ActionBase action = loader.getAction();
		DistRepository repo = DistRepository.createInitialRepository( action , set.meta , forceClearDist );
		releases.setDistRepository( repo );
	}
	
	public void loadReleases( ProductMeta set , boolean importxml ) throws Exception {
		ProductReleases releases = new ProductReleases( set , meta );
		set.setReleases( releases );

		DBProductReleases.loaddb( loader , releases , importxml );
		
		// old
		ActionBase action = loader.getAction();
		try {
			DistRepository repo = DistRepository.loadDistRepository( action , set.meta , importxml );
			releases.setDistRepository( repo );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductReleases1 , e , "unable to load release repository, product=" + set.name , set.name );
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
