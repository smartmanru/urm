package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.meta.Metadata.VarOSTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.Account;

public class DistRepository {

	Artefactory artefactory;
	private RemoteFolder repoFolder;
	Metadata meta;

	static String RELEASEHISTORYFILE = "history.txt";
	
	private DistRepository( Artefactory artefactory ) {
		this.artefactory = artefactory; 
		this.meta = artefactory.meta;
	}
	
	public static DistRepository getDistRepository( ActionBase action , Artefactory artefactory ) throws Exception {
		DistRepository repo = new DistRepository( artefactory ); 
		
		if( action.meta.env != null )
			repo.repoFolder = new RemoteFolder( artefactory , Account.getAccount( action , action.meta.env.DISTR_HOSTLOGIN , VarOSTYPE.UNIX ) , action.meta.env.DISTR_PATH );
		else
			repo.repoFolder = new RemoteFolder( artefactory , Account.getAccount( action , action.meta.product.CONFIG_DISTR_HOSTLOGIN , VarOSTYPE.UNIX ) , action.meta.product.CONFIG_DISTR_PATH );
		
		return( repo );
	}

	public RemoteFolder getDataFolder( ActionBase action , String dataSet ) throws Exception {
		return( repoFolder.getSubFolder( action , "data/" + dataSet ) );
	}
	
	public DistStorage getDistByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		
		String RELEASEPATH = getReleasePathByLabel( action , RELEASELABEL );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , RELEASEPATH );
		DistStorage storage = new DistStorage( artefactory , distFolder );
		
		// check release directory exists
		if( !distFolder.checkExists( action ) )
			action.exit( "release does not exist at " + RELEASEPATH );
		
		storage.load( action );
		return( storage );
	}

	public DistStorage createDist( ActionBase action , String RELEASELABEL , VarBUILDMODE BUILDMODE ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		
		String RELEASEPATH = getReleasePathByLabel( action , RELEASELABEL );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , RELEASEPATH );
		DistStorage storage = new DistStorage( artefactory , distFolder );
		
		// check release directory exists
		if( distFolder.checkExists( action ) )
			action.exit( "release already exists at " + RELEASEPATH );

		storage.create( action , BUILDMODE );
		return( storage );
	}

	public String getReleaseProdDir( ActionBase action ) throws Exception {
		return( getReleasePathByLabel( action , "prod" ) );
	}
	
	private String getReleasePathByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		
		String RELEASEPATH = "";
		if( RELEASELABEL.equals( "last" ) ) {
			RELEASEPATH = meta.product.CONFIG_VERSION_LAST_FULL;
			if( RELEASEPATH.isEmpty() )
				action.exit( "CONFIG_VERSION_LAST_FULL is not set in product.conf" );
			
			RELEASEPATH = "releases/" + RELEASEPATH;
		}
		else if( RELEASELABEL.equals( "next" ) ) {
			RELEASEPATH = meta.product.CONFIG_VERSION_NEXT_FULL;
			if( RELEASEPATH.isEmpty() )
				action.exit( "CONFIG_VERSION_NEXT_FULL is not set in product.conf" );

			RELEASEPATH = "releases/" + RELEASEPATH;
		}
		else if( RELEASELABEL.equals( "prod" ) ) {
			RELEASEPATH = "prod";

			// check content
			if( !repoFolder.checkFolderExists( action , RELEASEPATH ) )
				action.exit( "getReleaseVerByLabel: unable to find prod distributive" );
		}
		else
			RELEASEPATH = "releases/" + RELEASELABEL;
		
		if( !RELEASELABEL.equals( RELEASEPATH ))
			action.debug( "found release directory=" + RELEASEPATH + " by label=" + RELEASELABEL );
		
		return( RELEASEPATH );
	}
	
	public void createProd( ActionBase action , String RELEASEVER ) throws Exception {
		String PRODPATH = getReleasePathByLabel( action , "prod" );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , PRODPATH );
		if( !distFolder.checkExists( action ) )
			action.exit( "prod folder does not exists at " + distFolder.folderPath );
		
		if( distFolder.checkFileExists( action , RELEASEHISTORYFILE ) )
			action.exit( "prod folder is probably already initialized, delete history.txt manually to recreate" );
		
		distFolder.createFileFromString( action , RELEASEHISTORYFILE , getHistoryRecord( action , RELEASEVER , "add" ) );
		
		DistStorage storage = new DistStorage( artefactory , distFolder );
		storage.createProd( action , RELEASEVER );
		storage.finish( action );
	}

	private String getHistoryRecord( ActionBase action , String RELEASEVER , String operation ) throws Exception {
		String s = Common.getNameTimeStamp() + ":" + operation + ":" + RELEASEVER;
		return( s );
	}
	
}
