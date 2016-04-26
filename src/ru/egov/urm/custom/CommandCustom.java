package ru.egov.urm.custom;

import org.w3c.dom.Node;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.LocalFolder;

public class CommandCustom {

	Metadata meta;
	
	private ICustomBuild customBuild = null;
	private ICustomDeploy customDeploy = null;
	private ICustomDatabase customDatabase = null;
	
	public CommandCustom( Metadata meta ) {
		this.meta = meta;
	}

	public boolean isCustomBuild() {
		if( meta.product.CONFIG_CUSTOM_BUILD.isEmpty() )
			return( false );
		return( true );
	}
	
	public boolean isCustomDeploy() {
		if( meta.product.CONFIG_CUSTOM_DEPLOY.isEmpty() )
			return( false );
		return( true );
	}
	
	public boolean isCustomDatabase() {
		if( meta.product.CONFIG_CUSTOM_DATABASE.isEmpty() )
			return( false );
		return( true );
	}
	
	public void parseDistItem( ActionBase action , MetaDistrBinaryItem item , Node node ) throws Exception {
		startCustomDeploy( action );
		customDeploy.parseDistItem( action , this , item , node );
	}

	public void parseProject( ActionBase action , MetaSourceProject project , Node node ) throws Exception {
		startCustomBuild( action );
		customBuild.parseProject( action , this , project , node );
	}
	
	public String getGroupName( ActionBase action , String groupFolder ) throws Exception {
		startCustomDatabase( action );
		return( customDatabase.getGroupName( action , this , groupFolder ) );
	}

	public void copyCustom( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , LocalFolder P_TARGETDIR ) throws Exception {
		startCustomDatabase( action );
		customDatabase.copyCustom( action , this , P_ALIGNEDNAME , P_ALIGNEDID , P_TARGETDIR );
	}

	public boolean checkDatabaseDir( ActionBase action , FileSet P_ALIGNEDNAME , String P_ALIGNEDID , String P_DIR , String P_SCHEMALIST ) throws Exception {
		startCustomDatabase( action );
		return( customDatabase.checkDatabaseDir( action , this , P_ALIGNEDNAME , P_ALIGNEDID , P_DIR , P_SCHEMALIST ) );
	}
	
	private void startCustomBuild( ActionBase action ) throws Exception {
		if( customBuild != null )
			return;
		
		String className = meta.product.CONFIG_CUSTOM_BUILD;
		if( className.isEmpty() )
			action.exit( "custom build class is not set (CONFIG_CUSTOM_BUILD" );
		
		className = "ru.egov.urm.custom.build." + className;
		try {
			Class<?> cls = Class.forName( "ru.egov.urm.custom.build." + meta.product.CONFIG_CUSTOM_BUILD );
			customBuild = ( ICustomBuild )cls.newInstance();
			return;
		}
		catch( Throwable e ) {
			action.log( "error loading build class=" + className , e );
		}
		
		action.exit( "unable to load custom build class=" + className );
	}
	
	private void startCustomDeploy( ActionBase action ) throws Exception {
		if( customDeploy != null )
			return;
		
		String className = meta.product.CONFIG_CUSTOM_DEPLOY;
		if( className.isEmpty() )
			action.exit( "custom deploy class is not set (CONFIG_CUSTOM_DEPLOY" );
		
		className = "ru.egov.urm.custom.deploy." + className;
		try {
			Class<?> cls = Class.forName( className );
			customDeploy = ( ICustomDeploy )cls.newInstance();
			return;
		}
		catch( Throwable e ) {
			action.log( "error loading deploy class=" + className , e );
		}
		
		action.exit( "unable to load custom deploy class=" + className );
	}

	private void startCustomDatabase( ActionBase action ) throws Exception {
		if( customDatabase != null )
			return;
		
		String className = meta.product.CONFIG_CUSTOM_DATABASE;
		if( className.isEmpty() )
			action.exit( "custom deploy class is not set (CONFIG_CUSTOM_DATABASE" );
		
		className = "ru.egov.urm.custom.database." + className;
		try {
			Class<?> cls = Class.forName( className );
			customDatabase = ( ICustomDatabase )cls.newInstance();
			return;
		}
		catch( Throwable e ) {
			action.log( "error loading build class=" + className , e );
		}
		
		action.exit( "unable to load custom deploy class=" + className );
	}
	
}
