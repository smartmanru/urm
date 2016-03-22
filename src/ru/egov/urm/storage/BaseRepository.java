package ru.egov.urm.storage;

import org.w3c.dom.Document;

import ru.egov.urm.ConfReader;
import ru.egov.urm.PropertySet;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaFapBase;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.shell.Account;

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
		repo.repoFolder = new RemoteFolder( artefactory , Account.getAccount( action , action.context.env.DISTR_HOSTLOGIN , VarOSTYPE.UNIX ) , repo.meta.product.CONFIG_BASE_PATH );
		return( repo );
	}

	public String getBasePath( ActionBase action , String ID ) throws Exception {
		return( ID + "/metadata.xml" );
	}

	public MetaFapBase getBaseInfo( ActionBase action , String ID , PropertySet parentProperties ) throws Exception {
		String basePath = getBasePath( action , ID );
		String text = repoFolder.readFile( action , basePath );
		Document xml = ConfReader.readXmlString( action , text );
		
		MetaFapBase base = new MetaFapBase();
		base.load( action , xml.getDocumentElement() , parentProperties );
		return( base );
	}
	
}
