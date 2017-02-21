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
		CHANGING ,
		RELEASED ,
		CANCELLED ,
		PROD ,
		ARCHIVED
	}
	
	// state
	public DISTSTATE state;
	DISTSTATE stateMem;
	String stateChangeID;
	String stateHash;

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
		if( state == DISTSTATE.DIRTY ) {
			if( newState == DISTSTATE.CHANGING )
				ok = true;
		}
		else
		if( state == DISTSTATE.CHANGING ) {
			if( newState == DISTSTATE.DIRTY || newState == DISTSTATE.RELEASED )
				ok = true;
		}
		else
		if( state == DISTSTATE.RELEASED ) {
			if( newState == DISTSTATE.PROD || newState == DISTSTATE.CANCELLED || newState == DISTSTATE.CHANGING )
				ok = true;
		}
		else
		if( state == DISTSTATE.PROD ) {
			if( newState == DISTSTATE.ARCHIVED )
				ok = true;
		}
		if( !ok )
			action.ifexit( _Error.UnableChangeReleaseState2 , "unable to change release state from " + state.name() + " to " + newState.name() , new String[] { state.name() , newState.name() } );
		
		String timeStamp = Common.getNameTimeStamp();
		String hash = getHashValue( action );
		String value = newState + ":" + timeStamp + ":" + hash;
		createStateFile( action , value );
		activeChangeID = timeStamp;
		stateMem = newState;
		stateHash = hash;
		
		state = stateMem;
		stateChangeID = activeChangeID;
	}

	public void checkDistChangeEnabled( ActionBase action ) throws Exception {
		if( stateMem != DISTSTATE.CHANGING )
			action.exit0( _Error.DistributiveNotOpened0 , "distributive is not opened for change" );
	}
	
	public void ctlLoadReleaseState( ActionBase action ) throws Exception {
		stateChangeID = "";
		stateHash = "";
		
		if( !distFolder.checkExists( action ) ) {
			state = DISTSTATE.MISSINGDIST;
			return;
		}
		
		if( !distFolder.checkFileExists( action, Dist.STATE_FILENAME ) ) {
			state = DISTSTATE.MISSINGSTATE;
			return;
		}
		
		// file format - state:changeID:md5, changeID is timestamp, md5 is hash of distributive
		try {
			String stateInfo = distFolder.getFileContentAsString( action , Dist.STATE_FILENAME );
			String[] parts = Common.split( stateInfo , ":" );
			if( parts.length != 3 ) {
				state = DISTSTATE.BROKEN;
				return;
			}
			
			state = DISTSTATE.valueOf( parts[ 0 ] );
			stateChangeID = parts[ 1 ];
			stateHash = parts[ 2 ];
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
	
	public void ctlOpenForChange( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != DISTSTATE.DIRTY )
			action.exit1( _Error.DistributiveNotReadyForChange1 , "distributive is not ready for change, state=" + state.name() , state.name() );
		
		ctlSetStatus( action , DISTSTATE.CHANGING );
		action.debug( "distributive has been opened for change, ID=" + activeChangeID );
	}

	public void ctlReloadCheckOpened( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != DISTSTATE.CHANGING )
			action.exit1( _Error.DistributiveNotOpenedForChange1 , "distributive is not opened for change, state=" + state.name() , state.name() );
		
		if( !activeChangeID.equals( stateChangeID ) )
			action.exit1( _Error.DistributiveOpenedForConcurrentChange1 , "distributive is opened for concurrent change ID=" + stateChangeID , stateChangeID );
	}
	
	public void ctlCloseChange( ActionBase action ) throws Exception {
		ctlReloadCheckOpened( action );
		ctlSetStatus( action , DISTSTATE.DIRTY );
		action.debug( "distributive has been closed after change, ID=" + stateChangeID );
	}

	public void ctlForceClose( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != DISTSTATE.CHANGING )
			action.exit1( _Error.DistributiveNotOpenedForChange1 ,"distributive is not opened for change, state=" + state.name() , state.name() );
		
		ctlSetStatus( action , DISTSTATE.DIRTY );
		action.info( "distributive has been closed after change, ID=" + stateChangeID );
	}

	public void ctlFinish( ActionBase action ) throws Exception {
		ctlReloadCheckOpened( action );

		ctlSetStatus( action , DISTSTATE.RELEASED );
		action.info( "distributive has been finalized, hash=" + stateHash );
	}

	public void ctlReopen( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != DISTSTATE.RELEASED )
			action.exit1( _Error.DistributiveNotReleased1 , "distributive is not released, state=" + state.name() , state.name() );
		
		ctlSetStatus( action , DISTSTATE.CHANGING );
		action.info( "distributive has been reopened" );
	}

	public void ctlOpenForUse( ActionBase action , boolean PROD ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		if( PROD == false ) {
			if( state != DISTSTATE.PROD && state != DISTSTATE.RELEASED && state != DISTSTATE.DIRTY )
				action.exit1( _Error.DistributiveNotReadyForUse1 , "distributive is not ready for use, state=" + state.name() , state.name() );
		}
		
		if( PROD == true ) {
			if( state != DISTSTATE.PROD && state != DISTSTATE.RELEASED )
				action.exit1( _Error.DistributiveNotReadyForProd1 , "distributive is not ready for use in prod environment, state=" + state.name() , state.name() );
		}

		String hash = getHashValue( action );
		if( !hash.equals( stateHash ) )
			action.exit2( _Error.DistributiveHashDiffers2 , "distributive is not ready for use - actual hash=" + hash + ", declared hash=" + stateHash , hash , stateHash );
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
		if( state == DISTSTATE.ARCHIVED || state == DISTSTATE.PROD )
			action.exit1( _Error.DistributiveProtected1 , "distributive is protected, can be deleted only manually, state=" + state.name() , state.name() );
	}

	public void createStateFile( ActionBase action , String value ) throws Exception {
		distFolder.createFileFromString( action , Dist.STATE_FILENAME , value );
	}
	
	public String getHashValue( ActionBase action ) throws Exception {
		ShellExecutor shell = distFolder.getSession( action );
		String hash;
		if( shell.account.isLinux() ) {
			String cmd = "find . -type f -printf " + Common.getQuoted( "%p %s %TD %Tr\\n" ) + " | sort | grep -v state.txt | md5sum | cut -d \" \" -f1";
			hash = shell.customGetValue( action , distFolder.folderPath , cmd );
		}
		else {
			hash = getWindowsHashValue( action , shell );
		}
		return( hash );
	}

	private String getWindowsHashValue( ActionBase action , ShellExecutor shell ) throws Exception {
		Map<String,File> fileMap = new HashMap<String,File>(); 
		scanFiles( action , fileMap , distFolder , "." );
		
		// generate hash file
		LocalFolder workFolder = action.artefactory.getWorkFolder( action );
		String content = "";
		SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yy hh:mm:ss a");
		for( String name : Common.getSortedKeys( fileMap ) ) {
			if( name.equals( "./state.txt" ) )
				continue;
			
			File f = fileMap.get( name );
			String ftext = name + " " + f.length() + " " + sdf.format( f.lastModified() );
			content += ftext + "\n";
		}
		
		String hashFile = workFolder.getFilePath( action , "hash.txt" );
		Common.createFileFromString( action.execrc , hashFile , content );
		return( action.shell.getMD5( action , hashFile ) );
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
		if( state == DISTSTATE.PROD || state == DISTSTATE.RELEASED )
			return( true );
		return( false );
	}
	
}
