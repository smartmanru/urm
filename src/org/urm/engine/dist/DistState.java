package org.urm.engine.dist;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.engine.ServerReleaseLifecycle;

public class DistState {

	public enum DISTSTATE {
		UNKNOWN ,
		MISSINGDIST ,
		MISSINGSTATE ,
		BROKEN ,
		DIRTY ,
		CHANGING1 ,
		CHANGING2 ,
		RELEASED ,
		CANCELLED ,
		COMPLETED ,
		ARCHIVED
	}
	
	// state
	public DISTSTATE state;
	DISTSTATE stateMem;
	String stateChangeID;
	String dataHash;
	String metaHash;

	String activeChangeID;
	
	private Dist dist;
	private RemoteFolder distFolder;

	public DistState( Dist dist , RemoteFolder distFolder ) {
		this.dist = dist;
		this.distFolder = distFolder;
		this.state = DISTSTATE.UNKNOWN;
	}
	
	private void ctlSetStatus( ActionBase action , DISTSTATE newState ) throws Exception {
		// UNKNOWN = before read -> any
		// MISSING [no distributive] -> DIRTY
		// BROKEN [inconsistent]
		// DIRTY [any closed before finalyzed] -> CHANGING, RELEASED
		// CHANGING [in progress of normal change] -> DIRTY
		// RELEASED [sent to prod, not deployed to prod] -> PROD, CANCELLED
		// CANCELLED
		// PROD [deployed to prod] -> ARCHIVED
		// ARCHIVED

		boolean ok = false;
		if( state == DISTSTATE.MISSINGDIST ) {
			if( newState == DISTSTATE.DIRTY )
				ok = true;
		}
		else
		if( state == DISTSTATE.MISSINGSTATE ) {
			if( newState == DISTSTATE.DIRTY )
				ok = true;
		}
		else
		if( state == DISTSTATE.BROKEN ) {
			if( newState == DISTSTATE.CHANGING1 )
				ok = true;
		}
		else
		if( state == DISTSTATE.DIRTY ) {
			if( newState == DISTSTATE.CHANGING1 )
				ok = true;
		}
		else
		if( state == DISTSTATE.CHANGING1 ) {
			if( newState == DISTSTATE.CANCELLED || newState == DISTSTATE.DIRTY || newState == DISTSTATE.RELEASED )
				ok = true;
		}
		else
		if( state == DISTSTATE.RELEASED ) {
			if( newState == DISTSTATE.CHANGING1 || newState == DISTSTATE.CHANGING2 )
				ok = true;
		}
		else
		if( state == DISTSTATE.CHANGING2 ) {
			if( newState == DISTSTATE.CANCELLED || newState == DISTSTATE.RELEASED || newState == DISTSTATE.COMPLETED )
				ok = true;
		}
		else
		if( state == DISTSTATE.COMPLETED ) {
			if( newState == DISTSTATE.ARCHIVED )
				ok = true;
		}
		if( !ok )
			action.ifexit( _Error.UnableChangeReleaseState2 , "unable to change release state from " + state.name() + " to " + newState.name() , new String[] { state.name() , newState.name() } );
		
		String timeStamp = Common.getNameTimeStamp();
		String value = newState + ":" + timeStamp;
		String dataHashNew = "";
		String metaHashNew = "";
		if( isFinalized() ) {
			dataHashNew = dataHash;
			if( isCompleted() )
				metaHashNew = metaHash;
			else
				metaHashNew = getMetaHashValue( action );
		}
		else {
			if( isFinalized( newState ) ) {
				dataHashNew = getDataHashValue( action );
				metaHashNew = getMetaHashValue( action );
			}
		}
		
		value += ":" + dataHashNew + ":" + metaHashNew;
		createStateFile( action , value );
		activeChangeID = timeStamp;
		stateMem = newState;
		dataHash = dataHashNew;
		metaHash = metaHashNew;
		
		state = stateMem;
		stateChangeID = activeChangeID;
	}

	public void checkDistDataChangeEnabled( ActionBase action ) throws Exception {
		if( stateMem != DISTSTATE.CHANGING1 )
			action.exit0( _Error.DistributiveNotOpened0 , "distributive is not opened for change" );
	}
	
	public void checkDistMetaChangeEnabled( ActionBase action ) throws Exception {
		if( stateMem != DISTSTATE.CHANGING2 )
			action.exit0( _Error.DistributiveNotOpened0 , "distributive is not opened for change" );
	}
	
	public void ctlLoadReleaseState( ActionBase action ) throws Exception {
		stateChangeID = "";
		dataHash = "";
		metaHash = "";
		
		if( !distFolder.checkExists( action ) ) {
			state = DISTSTATE.MISSINGDIST;
			return;
		}
		
		if( !distFolder.checkFileExists( action, Dist.STATE_FILENAME ) ) {
			state = DISTSTATE.MISSINGSTATE;
			return;
		}
		
		// file format - state:changeID:data-md5:release-md5, changeID is timestamp, md5 is hash of distributive
		// hash fields are defined only on deploy stage  
		try {
			String stateInfo = distFolder.getFileContentAsString( action , Dist.STATE_FILENAME );
			String[] parts = Common.split( stateInfo , ":" );
			
			if( parts.length < 2 ) {
				state = DISTSTATE.BROKEN;
				return;
			}
			
			state = DISTSTATE.valueOf( parts[ 0 ] );
			if( isFinalized() ) {
				if( parts.length != 4 ) {
					state = DISTSTATE.BROKEN;
					return;
				}
				
				dataHash = parts[ 2 ];
				metaHash = parts[ 3 ];
			}
			
			stateChangeID = parts[ 1 ];
		}
		catch( IllegalArgumentException e ) {
			state = DISTSTATE.BROKEN;
		}
	}

	public void ctlCreate( ActionBase action , Date releaseDate , ServerReleaseLifecycle lc ) throws Exception {
		// create release.xml, create status file, set closed dirty state
		// check current status
		ctlLoadReleaseState( action );
		if( state != DISTSTATE.MISSINGDIST ) {
			if( !action.isForced() )
				action.exit0( _Error.CannotCreateExistingDistributive0 , "cannot create existing distributive" );
		}
			
		// create directory
		createMetaFile( action , releaseDate , lc );
		
		// set status
		ctlSetStatus( action , DISTSTATE.DIRTY );
		action.info( "release has been created: " + dist.RELEASEDIR );
	}
	
	public void ctlCreateProd( ActionBase action , String RELEASEVER ) throws Exception {
		// create release.xml, create status file, set closed dirty state
		if( !distFolder.checkExists( action ) )
			action.exit0( _Error.MissingProdDistributiveDirectory0 , "prod distributive directory should exist" );
		
		// check current status
		ctlLoadReleaseState( action );
		if( state != DISTSTATE.MISSINGSTATE )
			action.exit0( _Error.StateFileExists0 , "state file should not exist" );
		
		// create empty release.xml
		String filePath = action.artefactory.workFolder.getFilePath( action , Dist.META_FILENAME );
		
		Release info = new Release( dist.meta , dist );
		info.createProd( action , RELEASEVER , filePath );
		distFolder.copyFileFromLocal( action , filePath );
		
		// set status
		ctlSetStatus( action , DISTSTATE.DIRTY );
		action.info( "prod has been created at " + distFolder.folderPath );
	}
	
	public void ctlOpenForDataChange( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != DISTSTATE.DIRTY )
			action.exit1( _Error.DistributiveNotReadyForChange1 , "distributive is not ready for change, state=" + state.name() , state.name() );
		
		ctlSetStatus( action , DISTSTATE.CHANGING1 );
		action.debug( "distributive has been opened for change, ID=" + activeChangeID );
	}

	public void ctlReloadCheckOpenedForChange( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != DISTSTATE.CHANGING1 && state != DISTSTATE.CHANGING2 )
			action.exit1( _Error.DistributiveNotOpenedForChange1 , "distributive is not opened for change, state=" + state.name() , state.name() );
		
		if( !activeChangeID.equals( stateChangeID ) )
			action.exit1( _Error.DistributiveOpenedForConcurrentChange1 , "distributive is opened for concurrent change ID=" + stateChangeID , stateChangeID );
	}

	public void ctlReloadCheckOpenedForDataChange( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != DISTSTATE.CHANGING1 )
			action.exit1( _Error.DistributiveNotOpenedForChange1 , "distributive is not opened for change, state=" + state.name() , state.name() );
		
		if( !activeChangeID.equals( stateChangeID ) )
			action.exit1( _Error.DistributiveOpenedForConcurrentChange1 , "distributive is opened for concurrent change ID=" + stateChangeID , stateChangeID );
	}

	public void ctlReloadCheckOpenedForStatusChange( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != DISTSTATE.CHANGING2 )
			action.exit1( _Error.DistributiveNotOpenedForChange1 , "distributive is not opened for change, state=" + state.name() , state.name() );
		
		if( !activeChangeID.equals( stateChangeID ) )
			action.exit1( _Error.DistributiveOpenedForConcurrentChange1 , "distributive is opened for concurrent change ID=" + stateChangeID , stateChangeID );
	}
	
	public void ctlCloseDataChange( ActionBase action ) throws Exception {
		ctlReloadCheckOpenedForDataChange( action );
		ctlSetStatus( action , DISTSTATE.DIRTY );
		action.debug( "distributive has been closed after change, ID=" + stateChangeID );
	}

	public void ctlCloseControl( ActionBase action , DISTSTATE state ) throws Exception {
		ctlSetStatus( action , state );
		action.debug( "distributive has been closed after control, state=" + Common.getEnumLower( state ) );
	}

	public void ctlForceClose( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state == DISTSTATE.CHANGING1 ) {
			ctlSetStatus( action , DISTSTATE.DIRTY );
			action.info( "distributive has been closed after change, ID=" + stateChangeID );
			return;
		}
		
		if( state == DISTSTATE.CHANGING2 ) {
			ctlSetStatus( action , DISTSTATE.RELEASED );
			action.info( "distributive has been closed after change, ID=" + stateChangeID );
			return;
		}
		
		action.exit1( _Error.DistributiveNotOpenedForChange1 ,"distributive is not opened for change, state=" + state.name() , state.name() );
	}

	public void ctlFinish( ActionBase action ) throws Exception {
		ctlReloadCheckOpenedForDataChange( action );

		ctlSetStatus( action , DISTSTATE.RELEASED );
		action.info( "distributive has been finalized, state=" + Common.getEnumLower( DISTSTATE.RELEASED ) );
	}

	public void ctlReopen( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( isCompleted() )
			action.exit1( _Error.DistributiveProtected1 , "distributive is protected from changes, state=" + state.name() , state.name() );
		
		ctlSetStatus( action , DISTSTATE.CHANGING1 );
		action.info( "distributive has been reopened" );
	}

	public void ctlOpenForUse( ActionBase action , boolean openForUse ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		if( openForUse == false ) {
			if( state != DISTSTATE.COMPLETED && state != DISTSTATE.RELEASED && state != DISTSTATE.DIRTY )
				action.exit1( _Error.DistributiveNotReadyForUse1 , "distributive is not ready for use, state=" + state.name() , state.name() );
		}
		
		if( openForUse == true ) {
			if( state != DISTSTATE.COMPLETED && state != DISTSTATE.RELEASED )
				action.exit1( _Error.DistributiveNotReadyForProd1 , "distributive is not ready for use in prod environment, state=" + state.name() , state.name() );
		}

		String dataHashCurrent = getDataHashValue( action );
		String metaHashCurrent = getMetaHashValue( action );
		if( dataHashCurrent.equals( dataHash ) == false || metaHashCurrent.equals( metaHash ) == false )
			action.exit0( _Error.DistributiveHashDiffers0 , "distributive is not ready for use, hash value differs from declared" );
	}

	public void ctlOpenForControl( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		if( state != DISTSTATE.RELEASED )
			action.exit1( _Error.DistributiveNotReleased1 , "distributive is not released, state=" + state.name() , state.name() );
	}
	
	public void ctlCancel( ActionBase action ) throws Exception {
	}

	public void ctlCheckCanDropRelease( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		if( state != DISTSTATE.DIRTY )
			action.exit1( _Error.DistributiveNotClosed1 , "distributive is not in preparation state, use force option, state=" + state.name() , state.name() );
	}

	public void ctlClearRelease( ActionBase action ) throws Exception {
		stateMem = DISTSTATE.MISSINGDIST;
		action.info( "distributive has been deleted: " + distFolder.folderName );
	}

	public void ctlCheckCanForceDropRelease( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		if( state == DISTSTATE.ARCHIVED || state == DISTSTATE.COMPLETED )
			action.exit1( _Error.DistributiveProtected1 , "distributive is protected, can be deleted only manually, state=" + state.name() , state.name() );
	}

	public void createStateFile( ActionBase action , String value ) throws Exception {
		distFolder.createFileFromString( action , Dist.STATE_FILENAME , value );
	}

	public String getMetaHashValue( ActionBase action ) throws Exception {
		String data = distFolder.readFile( action , Dist.META_FILENAME );
		String hash = Common.getMD5( data );
		return( hash );
	}
	
	public String getDataHashValue( ActionBase action ) throws Exception {
		ShellExecutor shell = distFolder.getSession( action );
		String hash;
		if( shell.account.isLinux() ) {
			String cmd = "find . -type f -printf " + Common.getQuoted( "%p %s %TD %Tr\\n" ) + " | sort | grep -v state.txt | grep -v release.xml | md5sum | cut -d \" \" -f1";
			hash = shell.customGetValue( action , distFolder.folderPath , cmd );
		}
		else {
			Map<String,File> fileMap = new HashMap<String,File>(); 
			scanFiles( action , fileMap , distFolder , "." );
			
			// generate hash file
			LocalFolder workFolder = action.artefactory.getWorkFolder( action );
			String content = "";
			SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yy hh:mm:ss a");
			for( String name : Common.getSortedKeys( fileMap ) ) {
				if( name.equals( "./state.txt" ) || name.equals( "./release.xml" ) )
					continue;
				
				File f = fileMap.get( name );
				String ftext = name + " " + f.length() + " " + sdf.format( f.lastModified() );
				content += ftext + "\n";
			}
			
			String hashFile = workFolder.getFilePath( action , "hash.txt" );
			Common.createFileFromString( action.execrc , hashFile , content );
			hash = action.shell.getMD5( action , hashFile );
		}
		
		return( hash );
	}

	private void scanFiles( ActionBase action , Map<String,File> fileMap , RemoteFolder folder , String path ) throws Exception {
		File folderFile = new File( folder.folderPath );
		File[] list = folderFile.listFiles();
		if( list == null )
			return;
		
		for ( File f : list ) {
			String name = f.getName();
			String namePath = Common.getPath( path ,  name );
            if ( f.isDirectory() )
                scanFiles( action , fileMap , folder.getSubFolder( action , name ) , namePath );
            else
                fileMap.put( namePath , f );
        }		
	}

	public void createMetaFile( ActionBase action , Date releaseDate , ServerReleaseLifecycle lc ) throws Exception {
		// create empty release.xml
		String filePath = action.getWorkFilePath( Dist.META_FILENAME );
		String RELEASEDIR = distFolder.folderName;
		
		Release info = new Release( dist.meta , dist );
		String RELEASEVER = DistLabelInfo.getReleaseVerByDir( action , RELEASEDIR ); 
		info.create( action , RELEASEVER , releaseDate , lc , filePath );
		
		distFolder.ensureExists( action );
		distFolder.copyFileFromLocal( action , filePath );
	}

	public boolean isFinalized() {
		return( isFinalized( state ) );
	}
	
	public boolean isFinalized( DISTSTATE state ) {
		if( state == DISTSTATE.RELEASED || state == DISTSTATE.COMPLETED || state == DISTSTATE.ARCHIVED || state == DISTSTATE.CHANGING2 )
			return( true );
		return( false );
	}
	
	public boolean isCompleted() {
		return( isCompleted( state ) );
	}
	
	public boolean isCompleted( DISTSTATE state ) {
		if( state == DISTSTATE.COMPLETED || state == DISTSTATE.ARCHIVED || state == DISTSTATE.CHANGING2 )
			return( true );
		return( false );
	}
	
}
