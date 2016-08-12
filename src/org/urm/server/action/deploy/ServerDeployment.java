package org.urm.server.action.deploy;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta.VarCONTENTTYPE;
import org.urm.server.storage.FileSet;
import org.urm.server.storage.RedistStorage;
import org.urm.server.storage.RemoteFolder;

public class ServerDeployment {

	enum DeployInfoType {
		ROOT , 
		CATEGORY ,
		LOCATION ,
		FILE
	};
	
	DeployInfoType type;
	Map<String,ServerDeployment> data = new HashMap<String,ServerDeployment>();

	VarCONTENTTYPE CONTENTTYPE;
	boolean rollout;
	String file;
	
	public ServerDeployment() {
	}
	
	public void getFromRedistReleaseFolder( ActionBase action , RedistStorage redist , RemoteFolder releaseFolder ) throws Exception {
		type = DeployInfoType.ROOT;
		FileSet files = releaseFolder.getFileSet( action );

		for( VarCONTENTTYPE item : VarCONTENTTYPE.values() ) {
			getCategory( action , redist , files , item , true );
			getCategory( action , redist , files , item , false );
		}
	}

	private String getCDKey( VarCONTENTTYPE CONTENTTYPE , boolean rollout ) throws Exception {
		String mode = ( rollout )? "rollout" : "rollback";
		return( Common.getEnumLower( CONTENTTYPE ) + "-" + mode );
	}
	
	public VarCONTENTTYPE getCategoryContent( ActionBase action , String category ) throws Exception {
		String name = Common.getPartBeforeFirst( category , "-" );
		return( VarCONTENTTYPE.valueOf( name.toUpperCase() ) );
	}
	
	public boolean getCategoryRollout( ActionBase action , String category ) throws Exception {
		String name = Common.getPartAfterFirst( category , "-" );
		return( name.equals( "rollout" ) );
	}
	
	private void getCategory( ActionBase action , RedistStorage redist , FileSet files , VarCONTENTTYPE CONTENTTYPE , boolean rollout ) throws Exception {
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
		for( String file : files.files.keySet() ) {
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

	public String[] getLocations( ActionBase action , VarCONTENTTYPE CONTENTTYPE , boolean rollout ) throws Exception {
		ServerDeployment cd = data.get( getCDKey( CONTENTTYPE , rollout ) );
		if( cd == null )
			return( new String[0] );
		return( cd.data.keySet().toArray( new String[0] ) );
	}

	public String[] getLocationFiles( ActionBase action , VarCONTENTTYPE CONTENTTYPE , boolean rollout , String LOCATION ) throws Exception {
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
		if( cd.CONTENTTYPE == VarCONTENTTYPE.BINARYCOLDDEPLOY || cd.CONTENTTYPE == VarCONTENTTYPE.BINARYCOPYONLY ) {
			if( action.context.CTX_DEPLOYBINARY == false && action.context.CTX_CONFDEPLOY == true )
				return( false );
			if( action.context.CTX_DEPLOYCOLD == false && action.context.CTX_DEPLOYHOT == true )
				return( false );
			return( true );
		}
		if( cd.CONTENTTYPE == VarCONTENTTYPE.CONFCOLDDEPLOY || cd.CONTENTTYPE == VarCONTENTTYPE.CONFCOPYONLY ) {
			if( action.context.CTX_DEPLOYBINARY == true && action.context.CTX_CONFDEPLOY == false )
				return( false );
			if( action.context.CTX_DEPLOYCOLD == false && action.context.CTX_DEPLOYHOT == true )
				return( false );
			return( true );
		}
		if( cd.CONTENTTYPE == VarCONTENTTYPE.BINARYHOTDEPLOY ) {
			if( action.context.CTX_DEPLOYBINARY == false && action.context.CTX_CONFDEPLOY == true )
				return( false );
			if( action.context.CTX_DEPLOYCOLD == true && action.context.CTX_DEPLOYHOT == false )
				return( false );
			return( true );
		}
		if( cd.CONTENTTYPE == VarCONTENTTYPE.CONFHOTDEPLOY ) {
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
