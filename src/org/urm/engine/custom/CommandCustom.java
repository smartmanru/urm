package org.urm.engine.custom;

import org.urm.action.ActionBase;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaSourceProject;
import org.w3c.dom.Node;

public class CommandCustom {

	Meta meta;
	
	private ICustomBuild customBuild = null;
	private ICustomDeploy customDeploy = null;
	private ICustomDatabase customDatabase = null;
	
	public CommandCustom( Meta meta ) {
		this.meta = meta;
	}

	public boolean isCustomBuild( ActionBase action ) throws Exception {
		MetaProductSettings product = meta.getProductSettings(); 
		if( product.CONFIG_CUSTOM_BUILD.isEmpty() )
			return( false );
		return( true );
	}
	
	public boolean isCustomDeploy( ActionBase action ) throws Exception {
		MetaProductSettings product = meta.getProductSettings();
		if( product.CONFIG_CUSTOM_DEPLOY.isEmpty() )
			return( false );
		return( true );
	}
	
	public boolean isCustomDatabase( ActionBase action ) throws Exception {
		MetaProductSettings product = meta.getProductSettings();
		if( product.CONFIG_CUSTOM_DATABASE.isEmpty() )
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
		
		MetaProductSettings product = meta.getProductSettings(); 
		String className = product.CONFIG_CUSTOM_BUILD;
		if( className.isEmpty() )
			action.exit0( _Error.CustomBuildNotSet0 , "custom build class is not set (CONFIG_CUSTOM_BUILD" );
		
		className = getClass().getPackage().getName() + ".build." + className;
		try {
			Class<?> cls = Class.forName( className );
			customBuild = ( ICustomBuild )cls.newInstance();
			return;
		}
		catch( Throwable e ) {
			action.handle( "error loading build class=" + className , e );
		}
		
		action.exit1( _Error.UnableLoadCustomBuild1 , "unable to load custom build class=" + className , className );
	}
	
	private void startCustomDeploy( ActionBase action ) throws Exception {
		if( customDeploy != null )
			return;
		
		MetaProductSettings product = meta.getProductSettings(); 
		String className = product.CONFIG_CUSTOM_DEPLOY;
		if( className.isEmpty() )
			action.exit0( _Error.CustomDeployNotSet0 , "custom deploy class is not set (CONFIG_CUSTOM_DEPLOY" );
		
		className = getClass().getPackage().getName() + ".deploy." + className;
		try {
			Class<?> cls = Class.forName( className );
			customDeploy = ( ICustomDeploy )cls.newInstance();
			return;
		}
		catch( Throwable e ) {
			action.handle( "error loading deploy class=" + className , e );
		}
		
		action.exit1( _Error.UnableLoadCustomDeploy1 , "unable to load custom deploy class=" + className , className );
	}

	private void startCustomDatabase( ActionBase action ) throws Exception {
		if( customDatabase != null )
			return;
		
		MetaProductSettings product = meta.getProductSettings(); 
		String className = product.CONFIG_CUSTOM_DATABASE;
		if( className.isEmpty() )
			action.exit0( _Error.CustomDatabaseNotSet0 , "custom database class is not set (CONFIG_CUSTOM_DATABASE" );
		
		className = getClass().getPackage().getName() + ".database." + className;
		try {
			Class<?> cls = Class.forName( className );
			customDatabase = ( ICustomDatabase )cls.newInstance();
			return;
		}
		catch( Throwable e ) {
			action.handle( "error loading database class=" + className , e );
		}
		
		action.exit1( _Error.UnableLoadCustomDatabase1 , "unable to load custom database class=" + className , className );
	}
	
}
