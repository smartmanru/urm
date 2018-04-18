package org.urm.action.deploy;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.loader.Types.*;

public class ServerDeployment {

	enum DeployInfoType {
		ROOT , 
		CATEGORY ,
		LOCATION ,
		FILE
	};
	
	DeployInfoType type;
	Map<String,ServerDeployment> data = new HashMap<String,ServerDeployment>();

	EnumContentType CONTENTTYPE;
	boolean rollout;
	String file;
	
	public ServerDeployment() {
	}
	
	public void getFromRedistReleaseFolder( ActionBase action , RedistStorage redist , RemoteFolder releaseFolder ) throws Exception {
		type = DeployInfoType.ROOT;
		FileSet files = releaseFolder.getFileSet( action );

		for( EnumContentType item : EnumContentType.values() ) {
			getCategory( action , redist , files , item , true );
			getCategory( action , redist , files , item , false );
		}
	}

	private String getCDKey( EnumContentType CONTENTTYPE , boolean rollout ) throws Exception {
		String mode = ( rollout )? "rollout" : "rollback";
		return( Common.getEnumLower( CONTENTTYPE ) + "-" + mode );
	}
	
	public EnumContentType getCategoryContent( ActionBase action , String category ) throws Exception {
		String name = Common.getPartBeforeFirst( category , "-" );
		return( EnumContentType.valueOf( name.toUpperCase() ) );
	}
	
	public boolean getCategoryRollout( ActionBase action , String category ) throws Exception {
		String name = Common.getPartAfterFirst( category , "-" );
		return( name.equals( "rollout" ) );
	}
	
	private void getCategory( ActionBase action , RedistStorage redist , FileSet files , EnumContentType CONTENTTYPE , boolean rollout ) throws Exception {
		String folder = redist.getRedistFolderByContent( action , CONTENTTYPE , rollout );
		FileSet categoryDir = files.getDirByPath( action , folder );
		if( categoryDir == null )
			return;
		
		ServerDeployment deployment = new ServerDeployment();
		deployment.CONTENTTYPE = CONTENTTYPE;
		deployment.rollout = rollout;
		deployment.readCategoryData( action , redist , categoryDir );
		
		data.put( getCDKey( CONTENTTYPE , rollout ) , deployment );
	}

	private void readCategoryData( ActionBase action , RedistStorage redist , FileSet files ) throws Exception {
		type = DeployInfoType.CATEGORY;
		// find locations
		String[] locations = files.getAllDirs( action );
		for( String location : locations ) {
			ServerDeployment deployment = new ServerDeployment();
			deployment.readLocationData( action , redist , files.getDirByPath( action , location ) );
			data.put( location , deployment );
		}
	}

	private void readLocationData( ActionBase action , RedistStorage redist , FileSet files ) throws Exception {
		type = DeployInfoType.LOCATION;
		for( String file : files.getAllFiles() ) {
			// ignore version files
			if( file.endsWith( ".ver" ) )
				continue;
			
			ServerDeployment deployment = new ServerDeployment();
			deployment.readLocationFile( action , redist , file );
			data.put( file , deployment );
		}
	}
	
	private void readLocationFile( ActionBase action , RedistStorage redist , String file ) throws Exception {
		type = DeployInfoType.FILE;
		this.file = file; 
	}

	public String[] getLocations( ActionBase action , EnumContentType CONTENTTYPE , boolean rollout ) throws Exception {
		ServerDeployment cd = data.get( getCDKey( CONTENTTYPE , rollout ) );
		if( cd == null )
			return( new String[0] );
		return( cd.data.keySet().toArray( new String[0] ) );
	}

	public String[] getLocationFiles( ActionBase action , EnumContentType CONTENTTYPE , boolean rollout , String LOCATION ) throws Exception {
		ServerDeployment cd = data.get( getCDKey( CONTENTTYPE , rollout ) );
		if( cd != null ) {
			ServerDeployment ld = cd.data.get( LOCATION );
			if( ld != null )
				return( ld.data.keySet().toArray( new String[0] ) );
		}
		
		return( new String[0] );
	}

	public boolean isEmpty( ActionBase action ) throws Exception {
		for( ServerDeployment cd : data.values() ) {
			for( ServerDeployment ld : cd.data.values() ) {
				for( ServerDeployment fd : ld.data.values() ) {
					if( checkDeploy( action , cd , ld , fd ) )
						return( false );
				}
			}
		}
		return( true );
	}

	public boolean checkDeploy( ActionBase action , ServerDeployment cd , ServerDeployment ld , ServerDeployment fd ) throws Exception {
		if( cd.CONTENTTYPE == EnumContentType.BINARYCOLDDEPLOY || cd.CONTENTTYPE == EnumContentType.BINARYCOPYONLY ) {
			if( action.context.CTX_DEPLOYBINARY == false && action.context.CTX_CONFDEPLOY == true )
				return( false );
			if( action.context.CTX_DEPLOYCOLD == false && action.context.CTX_DEPLOYHOT == true )
				return( false );
			return( true );
		}
		if( cd.CONTENTTYPE == EnumContentType.CONFCOLDDEPLOY || cd.CONTENTTYPE == EnumContentType.CONFCOPYONLY ) {
			if( action.context.CTX_DEPLOYBINARY == true && action.context.CTX_CONFDEPLOY == false )
				return( false );
			if( action.context.CTX_DEPLOYCOLD == false && action.context.CTX_DEPLOYHOT == true )
				return( false );
			return( true );
		}
		if( cd.CONTENTTYPE == EnumContentType.BINARYHOTDEPLOY ) {
			if( action.context.CTX_DEPLOYBINARY == false && action.context.CTX_CONFDEPLOY == true )
				return( false );
			if( action.context.CTX_DEPLOYCOLD == true && action.context.CTX_DEPLOYHOT == false )
				return( false );
			return( true );
		}
		if( cd.CONTENTTYPE == EnumContentType.CONFHOTDEPLOY ) {
			if( action.context.CTX_DEPLOYBINARY == true && action.context.CTX_CONFDEPLOY == false )
				return( false );
			if( action.context.CTX_DEPLOYCOLD == true && action.context.CTX_DEPLOYHOT == false )
				return( false );
			return( true );
		}
		action.exitUnexpectedState();
		return( false );
	}

	public String[] getCategories( ActionBase action ) throws Exception {
		return( Common.getSortedKeys( data ) );
	}

	public String[] getCategoryLocations( ActionBase action , String category ) throws Exception {
		ServerDeployment cd = data.get( category );
		return( Common.getSortedKeys( cd.data ) );
	}
	
	public String[] getLocationItems( ActionBase action , String category , String LOCATION ) throws Exception {
		ServerDeployment cd = data.get( category );
		ServerDeployment ld = cd.data.get( LOCATION );
		return( Common.getSortedKeys( ld.data ) );
	}
	
}
