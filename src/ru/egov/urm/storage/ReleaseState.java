package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaRelease;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.run.ActionBase;

public class ReleaseState {

	enum RELEASESTATE {
		UNKNOWN ,
		MISSING ,
		BROKEN ,
		DIRTY ,
		CHANGING ,
		RELEASED ,
		CANCELLED ,
		PROD ,
		ARCHIVED
	}
	
	// state
	public RELEASESTATE state;
	RELEASESTATE stateMem;
	String stateChangeID;
	String stateHash;

	String activeChangeID;
	
	private RemoteFolder distFolder;

	public ReleaseState( RemoteFolder distFolder ) {
		this.distFolder = distFolder;
		this.state = RELEASESTATE.UNKNOWN;
	}
	
	private void ctlSetStatus( ActionBase action , RELEASESTATE newState , String hash ) throws Exception {
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
		if( state == RELEASESTATE.MISSING ) {
			if( newState == RELEASESTATE.DIRTY )
				ok = true;
		}
		else
		if( state == RELEASESTATE.DIRTY ) {
			if( newState == RELEASESTATE.CHANGING )
				ok = true;
		}
		else
		if( state == RELEASESTATE.CHANGING ) {
			if( newState == RELEASESTATE.DIRTY || newState == RELEASESTATE.RELEASED )
				ok = true;
		}
		else
		if( state == RELEASESTATE.RELEASED ) {
			if( newState == RELEASESTATE.PROD || newState == RELEASESTATE.CANCELLED )
				ok = true;
		}
		else
		if( state == RELEASESTATE.PROD ) {
			if( newState == RELEASESTATE.ARCHIVED )
				ok = true;
		}
		if( !ok )
			action.exit( "unable to change release state from " + state.name() + " to " + newState.name() );
		
		String timeStamp = Common.getNameTimeStamp();
		String value = newState + ":" + timeStamp + ":" + hash;
		distFolder.createFileFromString( action , DistStorage.stateFileName , value );
		activeChangeID = timeStamp;
		stateMem = newState;
	}
	
	public void checkDistChangeEnabled( ActionBase action ) throws Exception {
		if( stateMem != RELEASESTATE.CHANGING )
			action.exit( "distributive is not open for changes" );
	}
	
	public void ctlLoadReleaseState( ActionBase action ) throws Exception {
		stateChangeID = "";
		stateHash = "";
		
		if( !distFolder.checkExists( action ) ) {
			state = RELEASESTATE.MISSING;
			return;
		}
		
		if( !distFolder.checkFileExists( action, DistStorage.stateFileName ) ) {
			state = RELEASESTATE.BROKEN;
			return;
		}
		
		// file format - state:changeID:md5, changeID is timestamp, md5 is hash of distributive
		try {
			String stateInfo = distFolder.getFileContentAsString( action , DistStorage.stateFileName );
			String[] parts = Common.split( stateInfo , ":" );
			if( parts.length != 3 ) {
				state = RELEASESTATE.BROKEN;
				return;
			}
			
			state = RELEASESTATE.valueOf( parts[ 0 ] );
			stateChangeID = parts[ 1 ];
			stateHash = parts[ 2 ];
		}
		catch( IllegalArgumentException e ) {
			state = RELEASESTATE.BROKEN;
		}
	}

	private void ctlSetStatus( ActionBase action , RELEASESTATE newState ) throws Exception {
		ctlSetStatus( action , newState , "ignore" );
	}
	
	public void ctlCreate( ActionBase action , VarBUILDMODE BUILDMODE , String RELEASEDIR ) throws Exception {
		// create release.xml, create status file, set closed dirty state
		// check current status
		ctlLoadReleaseState( action );
		
		if( state != RELEASESTATE.MISSING )
			action.exit( "unable to create existing distributive" );
			
		// create directory
		distFolder.ensureExists( action );
		
		// create empty release.xml
		String filePath = action.artefactory.workFolder.getFilePath( action , DistStorage.metaFileName );
		String RELEASEVER = Common.getPartBeforeFirst( RELEASEDIR , "-" );
		
		MetaRelease info = new MetaRelease( action.meta );
		info.create( action , RELEASEVER , BUILDMODE , filePath , false );
		distFolder.copyFileFromLocal( action , filePath );
		
		// set status
		ctlSetStatus( action , RELEASESTATE.DIRTY );
		action.log( "release has been created: " + distFolder.folderName );
	}
	
	public void ctlOpenForChange( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != RELEASESTATE.DIRTY )
			action.exit( "distributive is not ready for change, state=" + state.name() );
		
		ctlSetStatus( action , RELEASESTATE.CHANGING );
		action.debug( "distributive has been opened for change, ID=" + activeChangeID );
	}

	public void ctlReloadCheckOpened( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != RELEASESTATE.CHANGING )
			action.exit( "distributive is not opened for change, state=" + state.name() );
		
		if( !activeChangeID.equals( stateChangeID ) )
			action.exit( "distributive is opened for concurrent change ID=" + stateChangeID );
	}
	
	public void ctlCloseChange( ActionBase action ) throws Exception {
		ctlReloadCheckOpened( action );
		ctlSetStatus( action , RELEASESTATE.DIRTY );
		action.debug( "distributive has been closed after change, ID=" + stateChangeID );
	}

	public void ctlForceClose( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != RELEASESTATE.CHANGING )
			action.exit( "distributive is not opened for change, state=" + state.name() );
		
		ctlSetStatus( action , RELEASESTATE.DIRTY );
		action.log( "distributive has been closed after change, ID=" + stateChangeID );
	}

	public void ctlFinish( ActionBase action , String hash ) throws Exception {
		ctlReloadCheckOpened( action );

		ctlSetStatus( action , RELEASESTATE.RELEASED , hash );
		action.log( "distributive has been finalized, hash=" + hash );
	}

	public void ctlReopen( ActionBase action ) throws Exception {
	}

	public void ctlOpenForUse( ActionBase action ) throws Exception {
	}
	
	public void ctlCancel( ActionBase action ) throws Exception {
	}

	public void ctlCheckCanDropRelease( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		if( state != RELEASESTATE.DIRTY )
			action.exit( "distributive is not closed, state=" + state.name() );
	}

	public void ctlClearRelease( ActionBase action ) throws Exception {
		stateMem = RELEASESTATE.MISSING;
		action.log( "distributive has been deleted: " + distFolder.folderName );
	}

	public void ctlCheckCanForceDropRelease( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		if( state == RELEASESTATE.DIRTY || state == RELEASESTATE.BROKEN || state == RELEASESTATE.CHANGING || state == RELEASESTATE.CANCELLED )
			return;
		
		action.exit( "distributive is protected, can be deleted only manually, state=" + state.name() );
	}

}
