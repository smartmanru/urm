package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.run.ActionBase;

public class HiddenFiles {

	Artefactory artefactory;
	Metadata meta;
	
	public HiddenFiles( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}

	public boolean copyHiddenConf( ActionBase action , MetaEnvServer server , MetaDistrConfItem confItem , LocalFolder folder ) throws Exception {
		if( meta.env.CONF_SECRETFILESPATH.isEmpty() )
			return( false );
		
		if( !action.options.OPT_HIDDEN ) {
			action.debug( "ignore hidden files without option" );
			return( false );
		}
		
		LocalFolder srcServerFolder = artefactory.getAnyFolder( action , 
			Common.getPath( meta.env.CONF_SECRETFILESPATH , confItem.KEY + "-" + server.NAME ) );
		if( srcServerFolder.checkExists( action ) ) {
			action.trace( "found server-specific hidden configuration" );
			folder.copyDirContent( action , srcServerFolder );
			folder.removeVcsFiles( action );
		}
		else {
			LocalFolder srcFolder = artefactory.getAnyFolder( action , 
				Common.getPath( meta.env.CONF_SECRETFILESPATH , confItem.KEY ) );
			if( srcFolder.checkExists( action ) ) {
				action.trace( "found environment-specific hidden configuration" );
				folder.copyDirContent( action , srcFolder );
				folder.removeVcsFiles( action );
			}
		}
		
		return( true );
	}
	
}
