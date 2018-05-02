package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.engine.data.EngineContext;
import org.urm.engine.data.EngineSettings;

public class BaseRepository {

	Artefactory artefactory;
	private RemoteFolder repoFolder;

	static String RELEASEHISTORYFILE = "history.txt";
	
	private BaseRepository( Artefactory artefactory ) {
		this.artefactory = artefactory; 
	}
	
	public static BaseRepository getBaseRepository( ActionBase action , Artefactory artefactory ) throws Exception {
		BaseRepository repo = new BaseRepository( artefactory );
		EngineSettings settings = action.getEngineSettings();
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

}
