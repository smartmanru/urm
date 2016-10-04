package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.engine.ServerContext;
import org.urm.engine.ServerSettings;
import org.urm.meta.product.MetaBase;
import org.urm.meta.product.MetaEnvServerNode;
import org.w3c.dom.Document;

public class BaseRepository {

	Artefactory artefactory;
	private RemoteFolder repoFolder;

	static String RELEASEHISTORYFILE = "history.txt";
	
	private BaseRepository( Artefactory artefactory ) {
		this.artefactory = artefactory; 
	}
	
	public static BaseRepository getBaseRepository( ActionBase action , Artefactory artefactory ) throws Exception {
		BaseRepository repo = new BaseRepository( artefactory );
		ServerSettings settings = action.getServerSettings();
		ServerContext context = settings.getServerContext();
		repo.repoFolder = new RemoteFolder( action.getLocalAccount() , context.DIST_BASEPATH );
		return( repo );
	}

	public String getBasePath( ActionBase action , String ID ) throws Exception {
		return( ID + "/metadata.xml" );
	}

	public String getBaseItemPath( ActionBase action , String BASEID , String itemPath ) throws Exception {
		return( repoFolder.getFilePath( action , BASEID + "/" + itemPath ) );
	}

	public RemoteFolder getBaseFolder( ActionBase action , String BASEID ) throws Exception {
		return( repoFolder.getSubFolder( action , BASEID ) );
	}

	public MetaBase getBaseInfo( ActionBase action , String ID , MetaEnvServerNode node , boolean primary ) throws Exception {
		String basePath = getBasePath( action , ID );
		String text = repoFolder.readFile( action , basePath );
		Document xml = ConfReader.readXmlString( text );
		
		action.debug( "load base info id=" + ID + " ..." );
		MetaBase base = new MetaBase( this , primary );
		base.load( action , xml.getDocumentElement() , node );
		return( base );
	}
	
}
