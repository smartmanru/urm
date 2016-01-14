package ru.egov.urm.storage;

import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.Account;

public class DistRepository {

	Artefactory artefactory;
	private RemoteFolder repoFolder;
	Metadata meta;

	private DistRepository( Artefactory artefactory ) {
		this.artefactory = artefactory; 
		this.meta = artefactory.meta;
	}
	
	public static DistRepository getDistRepository( ActionBase action , Artefactory artefactory ) throws Exception {
		DistRepository repo = new DistRepository( artefactory ); 
		
		if( action.meta.env != null )
			repo.repoFolder = new RemoteFolder( artefactory , Account.getAccount( action , action.meta.env.DISTR_HOSTLOGIN ) , action.meta.env.DISTR_PATH );
		else
			repo.repoFolder = new RemoteFolder( artefactory , Account.getAccount( action , action.meta.product.CONFIG_DISTR_HOSTLOGIN ) , action.meta.product.CONFIG_DISTR_PATH );
		
		return( repo );
	}

	public RemoteFolder getDataFolder( ActionBase action , String dataSet ) throws Exception {
		return( repoFolder.getSubFolder( action , "data/" + dataSet ) );
	}
	
	public DistStorage getDistByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		
		String RELEASEDIR = getReleaseDirByLabel( action , RELEASELABEL );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , RELEASEDIR );
		DistStorage storage = new DistStorage( artefactory , distFolder );
		
		// check release directory exists
		if( !distFolder.checkExists( action ) )
			action.exit( "unknown release=" + RELEASEDIR );
		
		storage.load( action );
		return( storage );
	}

	public DistStorage createDist( ActionBase action , String RELEASELABEL , VarBUILDMODE BUILDMODE ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		
		String RELEASEDIR = getReleaseDirByLabel( action , RELEASELABEL );
		
		RemoteFolder distFolder = repoFolder.getSubFolder( action , RELEASEDIR );
		DistStorage storage = new DistStorage( artefactory , distFolder );
		
		// check release directory exists
		if( distFolder.checkExists( action ) )
			action.exit( "release " + RELEASEDIR + " already exists" );

		storage.create( action , BUILDMODE );
		storage.load( action );
		return( storage );
	}

	public String getReleaseProdDir( ActionBase action ) throws Exception {
		return( getReleaseDirByLabel( action , "prod" ) );
	}
	
	public String getReleaseDirByLabel( ActionBase action , String RELEASELABEL ) throws Exception {
		action.checkRequired( RELEASELABEL , "RELEASELABEL" );
		
		String RELEASEDIR = "";
		if( RELEASELABEL.equals( "last" ) ) {
			RELEASEDIR = meta.product.CONFIG_VERSION_LAST_FULL;
			if( RELEASEDIR.isEmpty() )
				action.exit( "CONFIG_VERSION_LAST_FULL is not set in product.conf" );
		}
		else if( RELEASELABEL.equals( "next" ) ) {
			RELEASEDIR = meta.product.CONFIG_VERSION_NEXT_FULL;
			if( RELEASEDIR.isEmpty() )
				action.exit( "CONFIG_VERSION_NEXT_FULL is not set in product.conf" );
		}
		else if( RELEASELABEL.equals( "prod" ) ) {
			RELEASEDIR = repoFolder.findOneTop( action , "*-prod" );

			// check content
			RELEASEDIR.trim();
			if( RELEASEDIR.isEmpty() )
				action.exit( "getReleaseVerByLabel: unable to find prod distributive. Exiting" );

			if( RELEASEDIR.indexOf( ' ' ) > 0 )
				action.exit( "getReleaseVerByLabel: ambiguus distributives - " + RELEASEDIR );
		}
		else
			RELEASEDIR = RELEASELABEL;
		
		if( !RELEASELABEL.equals( RELEASEDIR ))
			action.debug( "found release directory=" + RELEASEDIR + " by label=" + RELEASELABEL );
		
		return( RELEASEDIR );
	}
}
