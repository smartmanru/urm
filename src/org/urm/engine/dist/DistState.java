package org.urm.engine.dist;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.shell.Shell;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.release.ReleaseDist;

public class DistState {

	public enum DISTSTATE {
		UNKNOWN ,
		MISSINGDIST ,
		MISSINGSTATE ,
		BROKEN ,
		DIRTY ,
		CHANGING ,
		RELEASED ,
		CANCELLED ,
		COMPLETED ,
		ARCHIVED
	}
	
	// state
	private RemoteFolder distFolder;

	public DISTSTATE state;
	DISTSTATE stateMem;
	String stateChangeID;
	public Date stateDate;
	public String dataHash;
	public String metaHash;
	String activeChangeID;
	
	public DistState( RemoteFolder distFolder ) {
		this.distFolder = distFolder;
		this.state = DISTSTATE.UNKNOWN;
	}
	
	public DistState copy( ActionBase action , Dist rdist ) throws Exception {
		DistState rstate = new DistState( distFolder );
		rstate.state = state;
		rstate.stateMem = stateMem;
		rstate.stateChangeID = stateChangeID;
		rstate.dataHash = dataHash;
		rstate.metaHash = metaHash;
		rstate.activeChangeID = activeChangeID;
		return( rstate );
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
			if( newState == DISTSTATE.CHANGING )
				ok = true;
		}
		else
		if( state == DISTSTATE.DIRTY ) {
			if( newState == DISTSTATE.CHANGING )
				ok = true;
		}
		else
		if( state == DISTSTATE.CHANGING ) {
			if( newState == DISTSTATE.CANCELLED || newState == DISTSTATE.DIRTY || newState == DISTSTATE.RELEASED )
				ok = true;
		}
		else
		if( state == DISTSTATE.RELEASED ) {
			if( newState == DISTSTATE.CHANGING || newState == DISTSTATE.CANCELLED || newState == DISTSTATE.RELEASED || newState == DISTSTATE.COMPLETED )
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
		
		value += ":" + dataHash + ":" + metaHash;
		createStateFile( action , value );
		activeChangeID = timeStamp;
		stateMem = newState;
		
		state = stateMem;
		stateChangeID = activeChangeID;
	}

	public void checkDistDataChangeEnabled( ActionBase action ) throws Exception {
		if( stateMem != DISTSTATE.CHANGING )
			action.exit0( _Error.DistributiveNotOpened0 , "distributive is not opened for change" );
	}
	
	public void checkDistMetaChangeEnabled( ActionBase action ) throws Exception {
		if( stateMem != DISTSTATE.RELEASED && stateMem != DISTSTATE.COMPLETED )
			action.exit0( _Error.DistributiveNotOpened0 , "distributive is not ready for control" );
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
			if( parts.length != 4 ) {
				state = DISTSTATE.BROKEN;
				return;
			}
			
			dataHash = parts[ 2 ];
			metaHash = parts[ 3 ];
			stateDate = new Date();
			
			stateChangeID = parts[ 1 ];
		}
		catch( IllegalArgumentException e ) {
			state = DISTSTATE.BROKEN;
		}
	}

	public void ctlCreateNormal( ActionBase action , ReleaseDist releaseDist ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		if( state != DISTSTATE.MISSINGSTATE ) {
			if( state == DISTSTATE.MISSINGDIST )
				Common.exitUnexpected();
			
			if( !action.isForced() )
				action.exit0( _Error.CannotCreateExistingDistributive0 , "cannot create existing distributive" );
		}
			
		// set status
		updateHashValues( action );
		ctlSetStatus( action , DISTSTATE.DIRTY );
		action.info( "release has been created at " + distFolder.getLocalPath( action ) );
	}

	public void updateHashValues( ActionBase action ) throws Exception {
		metaHash = getMetaHashValue( action );
		dataHash = getDataHashValue( action );
	}
	
	public void ctlCreateMaster( ActionBase action , ReleaseDist releaseDist ) throws Exception {
		// create release.xml, create status file, set closed dirty state
		if( !distFolder.checkExists( action ) )
			action.exit0( _Error.MissingProdDistributiveDirectory0 , "prod distributive directory should exist" );
		
		// check current status
		ctlLoadReleaseState( action );
		if( state != DISTSTATE.MISSINGSTATE )
			action.exit0( _Error.StateFileExists0 , "state file should not exist" );
		
		// set status
		updateHashValues( action );
		ctlSetStatus( action , DISTSTATE.DIRTY );
		action.info( "prod has been created at " + distFolder.getLocalPath( action ) );
	}
	
	public void ctlOpenForDataChange( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != DISTSTATE.DIRTY )
			action.exit1( _Error.DistributiveNotReadyForChange1 , "distributive is not ready for change, state=" + state.name() , state.name() );
		
		ctlSetStatus( action , DISTSTATE.CHANGING );
		action.debug( "distributive has been opened for change, ID=" + activeChangeID );
	}

	public void ctlReloadCheckOpenedForMetaChange( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		if( isFinalized() || state == DISTSTATE.CHANGING )
			return;
		
		action.exit1( _Error.DistributiveNotReadyForChange1 , "release metadata cannot be changed, state=" + state.name() , state.name() );
	}
	
	public void ctlReloadCheckOpenedForDataChange( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != DISTSTATE.CHANGING )
			action.exit1( _Error.DistributiveNotOpenedForChange1 , "distributive is not opened for change, state=" + state.name() , state.name() );
		
		if( !activeChangeID.equals( stateChangeID ) )
			action.exit1( _Error.DistributiveOpenedForConcurrentChange1 , "distributive is opened for concurrent change ID=" + stateChangeID , stateChangeID );
	}

	public void ctlCloseDataChange( ActionBase action ) throws Exception {
		ctlReloadCheckOpenedForDataChange( action );
		updateHashValues( action );
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
		if( state == DISTSTATE.CHANGING ) {
			ctlSetStatus( action , DISTSTATE.DIRTY );
			action.info( "distributive has been closed after change, ID=" + stateChangeID );
			return;
		}
		
		action.exit1( _Error.DistributiveNotOpenedForChange1 ,"distributive is not opened for change, state=" + state.name() , state.name() );
	}

	public void ctlFinish( ActionBase action ) throws Exception {
		ctlReloadCheckOpenedForDataChange( action );

		updateHashValues( action );
		ctlSetStatus( action , DISTSTATE.RELEASED );
		action.info( "distributive has been finalized, state=" + Common.getEnumLower( DISTSTATE.RELEASED ) );
	}

	public void ctlReopen( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( isCompleted() )
			action.exit1( _Error.DistributiveProtected1 , "distributive is protected from changes, state=" + state.name() , state.name() );
		
		ctlSetStatus( action , DISTSTATE.CHANGING );
		action.info( "distributive has been reopened" );
	}

	public void ctlOpenForUse( ActionBase action , boolean requireReleased ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		if( requireReleased == false ) {
			if( isFinalized() == false && state != DISTSTATE.DIRTY )
				action.exit1( _Error.DistributiveNotReadyForUse1 , "distributive is not ready for use, state=" + state.name() , state.name() );
		}
		
		if( requireReleased == true ) {
			if( state != DISTSTATE.COMPLETED && state != DISTSTATE.RELEASED )
				action.exit1( _Error.DistributiveNotReadyForProd1 , "distributive is not ready for use in prod environment, state=" + state.name() , state.name() );
		}

		if( state != DISTSTATE.DIRTY ) {
			String dataHashCurrent = getDataHashValue( action );
			if( dataHashCurrent.equals( dataHash ) == false )
				action.exit0( _Error.DistributiveHashDiffers0 , "distributive is not ready for use, data hash value differs from declared (" + dataHash + " / " + dataHashCurrent + ")" );
			
			String metaHashCurrent = getMetaHashValue( action );
			if( metaHashCurrent.equals( metaHash ) == false )
				action.exit0( _Error.DistributiveHashDiffers0 , "distributive is not ready for use, meta hash value differs from declared (" + metaHash + " / " + metaHashCurrent + ")" );
		}
	}

	public void ctlOpenForControl( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		if( !isFinalized() )
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

	public void updateMetaHashValue( ActionBase action ) throws Exception {
		if( !isFinalized() )
			return;
		
		metaHash = getMetaHashValue( action );
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
			hash = shell.customGetValue( action , distFolder.folderPath , cmd , Shell.WAIT_DEFAULT );
		}
		else {
			Map<String,File> fileMap = new HashMap<String,File>(); 
			scanFiles( action , fileMap , distFolder , "." );
			
			// generate hash file
			LocalFolder workFolder = action.artefactory.getWorkFolder( action );
			String content = "HASH:\n";
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

	public boolean isFinalized() {
		return( isFinalized( state ) );
	}
	
	public boolean isBroken() {
		if( state == DISTSTATE.UNKNOWN || state == DISTSTATE.MISSINGDIST || state == DISTSTATE.MISSINGSTATE || state == DISTSTATE.BROKEN )
			return( true );
		return( false );
	}
	
	public boolean isFinalized( DISTSTATE state ) {
		if( state == DISTSTATE.RELEASED || state == DISTSTATE.COMPLETED || state == DISTSTATE.ARCHIVED )
			return( true );
		return( false );
	}
	
	public boolean isCompleted() {
		return( isCompleted( state ) );
	}
	
	public boolean isCompleted( DISTSTATE state ) {
		if( state == DISTSTATE.COMPLETED || state == DISTSTATE.ARCHIVED )
			return( true );
		return( false );
	}
	
}
