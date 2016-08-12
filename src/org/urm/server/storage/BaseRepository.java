package org.urm.server.storage;

import org.urm.common.ConfReader;
import org.urm.common.RunContext.VarOSTYPE;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.meta.MetaBase;
import org.urm.server.meta.Meta;
import org.urm.server.shell.Account;
import org.w3c.dom.Document;

public class BaseRepository {

	Artefactory artefactory;
	private RemoteFolder repoFolder;
	Meta meta;

	static String RELEASEHISTORYFILE = "history.txt";
	
	private BaseRepository( Artefactory artefactory ) {
		this.artefactory = artefactory; 
		this.meta = artefactory.meta;
	}
	
	public static BaseRepository getBaseRepository( ActionBase action , Artefactory artefactory ) throws Exception {
		BaseRepository repo = new BaseRepository( artefactory ); 
		repo.repoFolder = new RemoteFolder( Account.getAccount( action , action.context.env.DISTR_HOSTLOGIN , VarOSTYPE.LINUX ) , repo.meta.product.CONFIG_BASE_PATH );
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
		MetaBase base = new MetaBase( meta , this , primary );
		base.load( action , xml.getDocumentElement() , node );
		return( base );
	}
	
}
