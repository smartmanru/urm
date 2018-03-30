package org.urm.meta.release;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.dist._Error;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;

public class Release {

	public static String ELEMENT_RELEASE = "release";
	public static String ELEMENT_SET = "set";
	public static String ELEMENT_PROJECT = "project";
	public static String ELEMENT_CONFITEM = "confitem";
	public static String ELEMENT_DISTITEM = "distitem";
	public static String ELEMENT_DELIVERY = "delivery";
	public static String ELEMENT_SCHEMA = "schema";
	public static String ELEMENT_DOC = "doc";
	public static String ELEMENT_HISTORY = "history";
	public static String ELEMENT_FILES = "files";
	public static String ELEMENT_PHASE = "phase";
	public static String ELEMENT_SCHEDULE = "schedule";
	public static String ELEMENT_CHANGES = "changes";
	public static String ELEMENT_TICKETSET = "ticketset";
	public static String ELEMENT_TICKET = "ticket";
	public static String ELEMENT_TICKETSETTARGET = "target";

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_MASTER = "master";
	public static String PROPERTY_VERSION = "version";
	public static String PROPERTY_LIFECYCLETYPE = "type";
	public static String PROPERTY_BUILDMODE = "mode"; 
	public static String PROPERTY_COMPATIBILITY = "over";
	public static String PROPERTY_CUMULATIVE = "cumulative";
	public static String PROPERTY_ARCHIVED = "archived";
	public static String PROPERTY_CANCELLED = "cancelled";
	
	public enum RELEASESTATE {
		UNKNOWN ,
		DIRTY ,
		RELEASED ,
		CANCELLED ,
		COMPLETED ,
		ARCHIVED
	}
	
	public ReleaseRepository repo;

	public int ID;
	public String NAME;
	public String DESC;
	public boolean MASTER;
	public DBEnumLifecycleType TYPE;
	public String RELEASEVER;
	public DBEnumBuildModeType BUILDMODE;
	public String COMPATIBILITY;
	public boolean CUMULATIVE;
	public boolean ARCHIVED;
	public boolean CANCELLED;
	public int RV;
	
	private ReleaseSchedule schedule;
	private ReleaseChanges changes;
	private ReleaseScope scope;
	private ReleaseDist defaultDist;

	private Map<String,ReleaseDist> distMap;
	
	private boolean modifyState;
	
	public Release( ReleaseRepository repo ) {
		this.repo = repo;
		
		schedule = new ReleaseSchedule( this );
		changes = new ReleaseChanges( this );
		scope = new ReleaseScope( this );
		
		distMap = new HashMap<String,ReleaseDist>();
		modifyState = false;
	}

	public Release copy( ReleaseRepository rrepo ) throws Exception {
		Release r = new Release( rrepo );

		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.MASTER = MASTER;
		r.TYPE = TYPE;
		r.RELEASEVER = RELEASEVER;
		r.BUILDMODE = BUILDMODE;
		r.COMPATIBILITY = COMPATIBILITY;
		r.CUMULATIVE = CUMULATIVE;
		r.ARCHIVED = ARCHIVED;
		r.CANCELLED = CANCELLED;
		r.RV = RV;
		
		r.schedule = schedule.copy( r );
		r.changes = changes.copy( r );
		r.scope = scope.copy( r );
		
		if( defaultDist != null )
			r.defaultDist = defaultDist.copy( r );
		
		for( ReleaseDist releaseDist : distMap.values() ) {
			ReleaseDist rreleaseDist = releaseDist.copy( r );
			r.addReleaseDist( rreleaseDist );
		}
		
		return( r );
	}
	
	public synchronized void modify( boolean done ) throws Exception {
		if( !done ) {
			if( modifyState )
				Common.exitUnexpected();
			modifyState = true;
		}
		else {
			if( !modifyState )
				Common.exitUnexpected();
			modifyState = false;
		}
	}
	
	public void setRepository( ReleaseRepository repo ) throws Exception {
		this.repo = repo;
	}
	
	public void setDefaultDist( ReleaseDist releaseDist ) throws Exception {
		if( defaultDist != null )
			Common.exitUnexpected();
		
		this.defaultDist = releaseDist;
	}
	
	public void addDist( ReleaseDist releaseDist ) throws Exception {
		if( releaseDist.DIST_VARIANT.isEmpty() )
			Common.exitUnexpected();
		if( distMap.get( releaseDist.DIST_VARIANT ) != null )
			Common.exitUnexpected();
		distMap.put( releaseDist.DIST_VARIANT , releaseDist );
	}
	
	private void addReleaseDist( ReleaseDist releaseDist ) {
		if( releaseDist.DIST_VARIANT.isEmpty() )
			return;
		
		distMap.put( releaseDist.DIST_VARIANT , releaseDist );
	}
	
	public void create( String NAME , String DESC , boolean MASTER , DBEnumLifecycleType TYPE , String RELEASEVER , 
			DBEnumBuildModeType BUILDMODE , String COMPATIBILITY , boolean CUMULATIVE , boolean ARCHIVED , boolean CANCELLED ) {
		this.NAME = NAME;
		this.DESC = DESC;
		this.MASTER = MASTER;
		this.TYPE = TYPE;
		this.RELEASEVER = RELEASEVER;
		this.BUILDMODE = BUILDMODE;
		this.COMPATIBILITY = COMPATIBILITY;
		this.CUMULATIVE = CUMULATIVE;
		this.ARCHIVED = ARCHIVED;
		this.CANCELLED = CANCELLED;
	}
	
	public void createNormal( ActionBase action , String RELEASEVER , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		this.NAME = RELEASEVER;
		this.DESC = "";
		this.MASTER = false;
		VersionInfo info = VersionInfo.getReleaseDirInfo( RELEASEVER );
		this.RELEASEVER = info.getFullVersion();
		this.TYPE = info.getLifecycleType();
		this.BUILDMODE = action.context.CTX_BUILDMODE;
		this.COMPATIBILITY = "";
		this.CUMULATIVE = action.context.CTX_CUMULATIVE;
		this.ARCHIVED = false;
		this.CANCELLED = false;
		this.RV = 0;

		schedule.createNormal( releaseDate );
		schedule.createReleaseSchedule( action , releaseDate , lc );
		setProperties( action );
	}

	public void createMaster( ActionBase action , String RELEASEVER , ReleaseDist releaseDist , boolean copy ) throws Exception {
		this.RELEASEVER = RELEASEVER;

		schedule.createMaster(); 
		
		this.MASTER = true;
		this.BUILDMODE = DBEnumBuildModeType.UNKNOWN;
		this.COMPATIBILITY = "";
		this.CUMULATIVE = true;
		this.ARCHIVED = false;
		this.CANCELLED = false;
	}
	
	public ReleaseSchedule getSchedule() {
		return( schedule );
	}
	
	public boolean isMaster() {
		if( MASTER )
			return( true );
		return( false );
	}
	
	public boolean isCompleted() {
		return( schedule.COMPLETED );
	}
	
	public boolean isFinalized() {
		return( schedule.RELEASED );
	}
	
	public ReleaseDist getDefaultReleaseDist() {
		return( defaultDist );
	}
	
	public boolean isCumulative() {
		return( CUMULATIVE );
	}
	
	public boolean isCancelled() {
		return( CANCELLED );
	}
	
	public String[] getApplyVersions() {
		if( isCumulative() )
			return( getCumulativeVersions() );
		return( new String[] { RELEASEVER } );
	}
	
	public String[] getCumulativeVersions() {
		String versions = Common.getSortedUniqueSpacedList( COMPATIBILITY + " " + RELEASEVER );
		String[] list = Common.splitSpaced( versions );
		return( VersionInfo.orderVersions( list ) );	
	}
	
	public void setReleaseVer( ActionBase action , String RELEASEVER ) throws Exception {
		this.RELEASEVER = RELEASEVER;
	}
	
	public void setReleaseDate( ActionBase action , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		if( lc == null )
			schedule.changeReleaseSchedule( action , releaseDate );
		else
			schedule.createReleaseSchedule( action , releaseDate , lc );
	}
	
	public void setProperties( ActionBase action ) throws Exception {
		BUILDMODE = action.context.CTX_BUILDMODE;
		
		if( action.context.CTX_ALL )
			COMPATIBILITY = "";
		for( String OLDRELEASE : Common.splitSpaced( action.context.CTX_OLDRELEASE ) ) {
			OLDRELEASE = VersionInfo.normalizeReleaseVer( OLDRELEASE );
			if( OLDRELEASE.compareTo( RELEASEVER ) >= 0 )
				action.exit1( _Error.CompatibilityExpectedForEarlierRelease1 , "compatibility is expected for earlier release (version=" + OLDRELEASE + ")" , OLDRELEASE );
			
			COMPATIBILITY = Common.addItemToUniqueSpacedList( COMPATIBILITY , OLDRELEASE );
		}
	}
	
	public String getReleaseCandidateTag( ActionBase action ) {
		return( "prod-" + RELEASEVER + "-candidate" );
	}
	
	public boolean isEmpty() {
		return( scope.isEmpty() );
	}

	public DBEnumLifecycleType getLifecycleType() {
		return( TYPE );
	}

	public void finish( ActionBase action ) throws Exception {
		schedule.finish( action );
	}
	
	public void complete( ActionBase action ) throws Exception {
		schedule.complete( action );
	}
	
	public void reopen( ActionBase action ) throws Exception {
		schedule.reopen( action );
	}

	public RELEASESTATE getState() {
		if( ARCHIVED )
			return( RELEASESTATE.ARCHIVED );
		if( CANCELLED )
			return( RELEASESTATE.CANCELLED );
		if( !schedule.RELEASED )
			return( RELEASESTATE.DIRTY );
		if( !schedule.COMPLETED )
			return( RELEASESTATE.RELEASED );
		return( RELEASESTATE.COMPLETED );
	}

	public Meta getMeta() {
		return( repo.meta );
	}

	public boolean isCompatible( String version ) {
		if( COMPATIBILITY.isEmpty() )
			return( true );
			
		if( Common.checkPartOfSpacedList( RELEASEVER , COMPATIBILITY ) )
			return( true );
		return( false );	
	}

	public ReleaseChanges getChanges() {
		return( changes );
	}

	public ReleaseDist findDistVariant( String variant ) {
		if( variant ==  null || variant.isEmpty() )
			return( defaultDist );
		return( distMap.get( variant ) );
	}

	public ReleaseScope getScope() {
		return( scope );
	}
	
}
