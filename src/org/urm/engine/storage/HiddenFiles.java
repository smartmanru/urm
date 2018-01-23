package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaEnvServer;

public class HiddenFiles {

	public static String SECRETPROPERTYFILE = "secret.properties";
	
	Artefactory artefactory;
	Meta meta;
	
	public HiddenFiles( Artefactory artefactory , Meta meta ) {
		this.artefactory = artefactory;
		this.meta = meta;
	}

	public String getSecretPropertyFile( ActionBase action , String envPath ) throws Exception {
		String secretPath = action.context.CTX_HIDDENPATH;
		if( secretPath.isEmpty() )
			secretPath = envPath;
			
		if( secretPath == null || secretPath.isEmpty() )
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
			Common.getPath( action.context.env.CONF_SECRETFILESPATH , confItem.NAME + "-" + server.NAME ) );
		if( srcServerFolder.checkExists( action ) ) {
			action.trace( "found server-specific hidden configuration" );
			folder.copyDirContent( action , srcServerFolder );
			folder.removeVcsFiles( action );
		}
		else {
			LocalFolder srcFolder = artefactory.getAnyFolder( action , 
				Common.getPath( action.context.env.CONF_SECRETFILESPATH , confItem.NAME ) );
			if( srcFolder.checkExists( action ) ) {
				action.trace( "found environment-specific hidden configuration" );
				folder.copyDirContent( action , srcFolder );
				folder.removeVcsFiles( action );
			}
		}
		
		return( true );
	}
	
}
