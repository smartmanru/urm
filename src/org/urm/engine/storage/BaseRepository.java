package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.ConfReader;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.engine.BaseItemData;
import org.urm.meta.engine.EngineContext;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.env.MetaEnvServerNode;
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

	public BaseItemData getBaseInfo( ActionBase action , BaseItem item ) throws Exception {
		String basePath = getBasePath( action , item.NAME );
		
		BaseItemData data = new BaseItemData( item , this );
		if( repoFolder.checkFileExists( action , basePath ) ) {
			String text = repoFolder.readFile( action , basePath );
			Document xml = ConfReader.readXmlString( text );
		
			action.trace( "load base info id=" + item.NAME + " ..." );
			data.load( action , xml.getDocumentElement() );
		}
		else {
			action.trace( "new base info id=" + item.NAME + " ..." );
			data.create( action );
		}
		
		return( data );
	}
	
	public BaseItemData getBaseInfo( ActionBase action , String ID , MetaEnvServerNode node , boolean primary ) throws Exception {
		String basePath = getBasePath( action , ID );
		String text = repoFolder.readFile( action , basePath );
		Document xml = ConfReader.readXmlString( text );
		
		action.debug( "load base info id=" + ID + " ..." );
		EngineBase base = action.getServerBase();
		BaseItem item = base.findItem( ID );
		if( item == null )
			action.exit1( _Error.UnknownBaseId1 , "Unknown base ID=" + ID , ID );
		
		BaseItemData data = new BaseItemData( item , this , node );
		data.load( action , xml.getDocumentElement() );
		return( data );
	}
	
}
