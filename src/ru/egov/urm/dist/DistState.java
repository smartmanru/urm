package ru.egov.urm.dist;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.shell.ShellExecutor;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.RemoteFolder;

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
	
	private RemoteFolder distFolder;

	public DistState( RemoteFolder distFolder ) {
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
		if( !ok ) {
			if( !action.context.CTX_FORCE )
				action.exit( "unable to change release state from " + state.name() + " to " + newState.name() );
		}
		
		String timeStamp = Common.getNameTimeStamp();
		String hash = getHashValue( action );
		String value = newState + ":" + timeStamp + ":" + hash;
		distFolder.createFileFromString( action , Dist.STATE_FILENAME , value );
		activeChangeID = timeStamp;
		stateMem = newState;
		stateHash = hash;
	}

	private String getHashValue( ActionBase action ) throws Exception {
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
		Common.createFileFromString( hashFile , content );
		return( action.session.getMD5( action , hashFile ) );
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
	
	public void checkDistChangeEnabled( ActionBase action ) throws Exception {
		if( stateMem != DISTSTATE.CHANGING )
			action.exit( "distributive is not open for changes" );
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

	public void ctlCreate( ActionBase action ) throws Exception {
		// create release.xml, create status file, set closed dirty state
		// check current status
		ctlLoadReleaseState( action );
		if( state != DISTSTATE.MISSINGDIST ) {
			if( !action.context.CTX_FORCE )
				action.exit( "unable to create existing distributive" );
		}
			
		// create directory
		distFolder.ensureExists( action );
		
		// create empty release.xml
		String filePath = action.artefactory.workFolder.getFilePath( action , Dist.META_FILENAME );
		String RELEASEDIR = distFolder.folderName;
		String RELEASEVER = Common.getPartBeforeFirst( RELEASEDIR , "-" );
		
		Release info = new Release( action.meta );
		info.create( action , RELEASEVER , filePath );
		distFolder.copyFileFromLocal( action , filePath );
		
		// set status
		ctlSetStatus( action , DISTSTATE.DIRTY );
		action.info( "release has been created: " + RELEASEDIR );
	}
	
	public void ctlCreateProd( ActionBase action , String RELEASEVER ) throws Exception {
		// create release.xml, create status file, set closed dirty state
		if( !distFolder.checkExists( action ) )
			action.exit( "prod distributive directory should exist" );
		
		// check current status
		ctlLoadReleaseState( action );
		if( state != DISTSTATE.MISSINGSTATE )
			action.exit( "state file should not exist" );
		
		// create empty release.xml
		String filePath = action.artefactory.workFolder.getFilePath( action , Dist.META_FILENAME );
		
		Release info = new Release( action.meta );
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
			action.exit( "distributive is not ready for change, state=" + state.name() );
		
		ctlSetStatus( action , DISTSTATE.CHANGING );
		action.debug( "distributive has been opened for change, ID=" + activeChangeID );
	}

	public void ctlReloadCheckOpened( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		// dirty state expected
		if( state != DISTSTATE.CHANGING )
			action.exit( "distributive is not opened for change, state=" + state.name() );
		
		if( !activeChangeID.equals( stateChangeID ) )
			action.exit( "distributive is opened for concurrent change ID=" + stateChangeID );
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
			action.exit( "distributive is not opened for change, state=" + state.name() );
		
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
			action.exit( "distributive is not released, state=" + state.name() );
		
		ctlSetStatus( action , DISTSTATE.CHANGING );
		action.info( "distributive has been reopened" );
	}

	public void ctlOpenForUse( ActionBase action , boolean PROD ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		
		if( PROD == false ) {
			if( state != DISTSTATE.PROD && state != DISTSTATE.RELEASED && state != DISTSTATE.DIRTY )
				action.exit( "distributive is not ready for use, state=" + state.name() );
		}
		
		if( PROD == true ) {
			if( state != DISTSTATE.PROD && state != DISTSTATE.RELEASED )
				action.exit( "distributive is not ready for use in prod environment, state=" + state.name() );
		}

		String hash = getHashValue( action );
		if( !hash.equals( stateHash ) )
			action.exit( "distributive is not ready for use - actual hash=" + hash + ", declared hash=" + stateHash );
	}
	
	public void ctlCancel( ActionBase action ) throws Exception {
	}

	public void ctlCheckCanDropRelease( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		if( state != DISTSTATE.DIRTY )
			action.exit( "distributive is not closed, state=" + state.name() );
	}

	public void ctlClearRelease( ActionBase action ) throws Exception {
		stateMem = DISTSTATE.MISSINGDIST;
		action.info( "distributive has been deleted: " + distFolder.folderName );
	}

	public void ctlCheckCanForceDropRelease( ActionBase action ) throws Exception {
		// check current status
		ctlLoadReleaseState( action );
		if( state == DISTSTATE.DIRTY || state == DISTSTATE.BROKEN || state == DISTSTATE.CHANGING || state == DISTSTATE.CANCELLED )
			return;
		
		action.exit( "distributive is protected, can be deleted only manually, state=" + state.name() );
	}

}
