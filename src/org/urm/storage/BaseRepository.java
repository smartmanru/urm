package org.urm.storage;

import org.urm.ConfReader;
import org.urm.action.ActionBase;
import org.urm.meta.MetaEnvServerNode;
import org.urm.meta.MetaFapBase;
import org.urm.meta.Metadata;
import org.urm.meta.Metadata.VarOSTYPE;
import org.urm.shell.Account;
import org.w3c.dom.Document;

public class BaseRepository {

	Artefactory artefactory;
	private RemoteFolder repoFolder;
	Metadata meta;

	static String RELEASEHISTORYFILE = "history.txt";
	
	private BaseRepository( Artefactory artefactory ) {
		this.artefactory = artefactory; 
		this.meta = artefactory.meta;
	}
	
	public static BaseRepository getBaseRepository( ActionBase action , Artefactory artefactory ) throws Exception {
		BaseRepository repo = new BaseRepository( artefactory ); 
		repo.repoFolder = new RemoteFolder( artefactory , Account.getAccount( action , action.context.env.DISTR_HOSTLOGIN , VarOSTYPE.LINUX ) , repo.meta.product.CONFIG_BASE_PATH );
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

	public MetaFapBase getBaseInfo( ActionBase action , String ID , MetaEnvServerNode node , boolean primary ) throws Exception {
		String basePath = getBasePath( action , ID );
		String text = repoFolder.readFile( action , basePath );
		Document xml = ConfReader.readXmlString( action , text );
		
		action.debug( "load base info id=" + ID + " ..." );
		MetaFapBase base = new MetaFapBase( meta , this , primary );
		base.load( action , xml.getDocumentElement() , node );
		return( base );
	}
	
}
