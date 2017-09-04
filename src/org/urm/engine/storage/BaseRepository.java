package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineBaseItem;
import org.urm.meta.engine.EngineBaseItemData;
import org.urm.meta.engine.EngineContext;
import org.urm.meta.engine.EngineSettings;
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
		EngineSettings settings = action.getServerSettings();
		EngineContext context = settings.getServerContext();
		repo.repoFolder = new RemoteFolder( action.getLocalAccount() , context.DIST_PLATFORMPATH );
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

	public EngineBaseItemData getBaseInfo( ActionBase action , EngineBaseItem item ) throws Exception {
		String basePath = getBasePath( action , item.ID );
		
		EngineBaseItemData data = new EngineBaseItemData( item , this );
		if( repoFolder.checkFileExists( action , basePath ) ) {
			String text = repoFolder.readFile( action , basePath );
			Document xml = ConfReader.readXmlString( text );
		
			action.trace( "load base info id=" + item.ID + " ..." );
			data.load( action , xml.getDocumentElement() );
		}
		else {
			action.trace( "new base info id=" + item.ID + " ..." );
			data.create( action );
		}
		
		return( data );
	}
	
	public EngineBaseItemData getBaseInfo( ActionBase action , String ID , MetaEnvServerNode node , boolean primary ) throws Exception {
		String basePath = getBasePath( action , ID );
		String text = repoFolder.readFile( action , basePath );
		Document xml = ConfReader.readXmlString( text );
		
		action.debug( "load base info id=" + ID + " ..." );
		EngineBase base = action.getServerBase();
		EngineBaseItem item = base.findBase( ID );
		if( item == null )
			action.exit1( _Error.UnknownBaseId1 , "Unknown base ID=" + ID , ID );
		
		EngineBaseItemData data = new EngineBaseItemData( item , this , node );
		data.load( action , xml.getDocumentElement() );
		return( data );
	}
	
}
