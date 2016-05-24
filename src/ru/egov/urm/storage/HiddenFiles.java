package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.Metadata;

public class HiddenFiles {

	public static String SECRETPROPERTYFILE = "secret.properties";
	
	Artefactory artefactory;
	Metadata meta;
	
	public HiddenFiles( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}

	public String getSecretPropertyFile( ActionBase action ) throws Exception {
		if( action.context.CTX_HIDDENPATH.isEmpty() )
			return( "" );
		return( Common.getPath( action.context.CTX_HIDDENPATH , SECRETPROPERTYFILE ) );
	}
	
	public boolean copyHiddenConf( ActionBase action , MetaEnvServer server , MetaDistrConfItem confItem , LocalFolder folder ) throws Exception {
		if( !action.context.CTX_HIDDEN ) {
			action.debug( "ignore hidden files without option" );
			return( false );
		}
		
		if( action.context.env.CONF_SECRETFILESPATH.isEmpty() )
			return( false );
		
		LocalFolder srcServerFolder = artefactory.getAnyFolder( action , 
			Common.getPath( action.context.env.CONF_SECRETFILESPATH , confItem.KEY + "-" + server.NAME ) );
		if( srcServerFolder.checkExists( action ) ) {
			action.trace( "found server-specific hidden configuration" );
			folder.copyDirContent( action , srcServerFolder );
			folder.removeVcsFiles( action );
		}
		else {
			LocalFolder srcFolder = artefactory.getAnyFolder( action , 
				Common.getPath( action.context.env.CONF_SECRETFILESPATH , confItem.KEY ) );
			if( srcFolder.checkExists( action ) ) {
				action.trace( "found environment-specific hidden configuration" );
				folder.copyDirContent( action , srcFolder );
				folder.removeVcsFiles( action );
			}
		}
		
		return( true );
	}
	
}
