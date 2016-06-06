package org.urm.server.storage;

import org.urm.common.Common;
import org.urm.meta.MetaDistrConfItem;
import org.urm.meta.MetaEnvServer;
import org.urm.meta.Metadata;
import org.urm.server.action.ActionBase;

public class HiddenFiles {

	public static String SECRETPROPERTYFILE = "secret.properties";
	
	Artefactory artefactory;
	Metadata meta;
	
	public HiddenFiles( Artefactory artefactory ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
	}

	public String getSecretPropertyFile( ActionBase action , String envPath ) throws Exception {
		String secretPath = action.context.CTX_HIDDENPATH;
		if( secretPath.isEmpty() )
			secretPath = envPath;
			
		if( secretPath.isEmpty() )
			return( "" );
		
		return( Common.getPath( secretPath , SECRETPROPERTYFILE ) );
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
