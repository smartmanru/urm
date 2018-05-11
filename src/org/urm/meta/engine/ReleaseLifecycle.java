package org.urm.meta.engine;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.blotter.EngineBlotterReleaseItem;
import org.urm.engine.blotter.EngineBlotterSet;
import org.urm.engine.blotter.EngineBlotter.BlotterType;
import org.urm.engine.dist.Release;
import org.urm.engine.dist.VersionInfo;
import org.urm.meta.EngineObject;
import org.urm.meta.product.Meta;

public class ReleaseLifecycle extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_TYPE = "type";
	public static String PROPERTY_ENABLED = "enabled";
	public static String PROPERTY_REGULAR = "regular";
	public static String PROPERTY_DAYS_TO_RELEASE = "releasedays";
	public static String PROPERTY_DAYS_TO_DEPLOY = "deploydays";
	public static String PROPERTY_SHIFT_DAYS = "shiftdays";
	
	EngineLifecycles lifecycles;
	
	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumLifecycleType LIFECYCLE_TYPE;
	public boolean ENABLED;
	public boolean REGULAR;
	public int DAYS_TO_RELEASE;
	public int DAYS_TO_DEPLOY;
	public int SHIFT_DAYS;
	public int CV;
	
	public int releasePhases;
	public int deployPhases;
	
	List<LifecyclePhase> phases;
	
	public ReleaseLifecycle( EngineLifecycles lifecycles ) {
		super( lifecycles );
		this.lifecycles = lifecycles;
		ENABLED = false;
		ID = -1;
		CV = 0;
		
		releasePhases = 0;
		deployPhases = 0;
		phases = new LinkedList<LifecyclePhase>();
	}
	
	@Override
	public String getName() {
		return( NAME );
	}
	
	public ReleaseLifecycle copy( EngineLifecycles lifecycles ) throws Exception {
		ReleaseLifecycle r = new ReleaseLifecycle( lifecycles );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.LIFECYCLE_TYPE = LIFECYCLE_TYPE;
		r.ENABLED = ENABLED;
		r.REGULAR = REGULAR;
		r.DAYS_TO_RELEASE = DAYS_TO_RELEASE;
		r.DAYS_TO_DEPLOY = DAYS_TO_DEPLOY;
		r.SHIFT_DAYS = SHIFT_DAYS;
		r.CV = CV;
		
		r.releasePhases = releasePhases;
		r.deployPhases = deployPhases;
		
		for( LifecyclePhase phase : phases ) {
			LifecyclePhase rphase = phase.copy( r );
			r.addPhase( rphase );
		}
		return( r );
	}
	
	public void addPhase( LifecyclePhase phase ) {
		phases.add( phase );
	}
	
	public LifecyclePhase getPhase( int index ) {
		return( phases.get( index ) );
	}
	
	public void rebuild() {
		// reorder by pos and type
		Map<String,LifecyclePhase> map = new HashMap<String,LifecyclePhase>();
		
		releasePhases = 0;
		deployPhases = 0;
		for( LifecyclePhase phase : phases ) {
			if( phase.isRelease() ) {
				releasePhases++;
				map.put( "0:" + Common.getZeroPadded( phase.STAGE_POS , 10 ) , phase );
			}
			else
			if( phase.isDeploy() ) {
				deployPhases++;
				map.put( "1:" + Common.getZeroPadded( phase.STAGE_POS , 10 ) , phase );
			}
		}
		
		phases.clear();
		DBEnumLifecycleStageType type = DBEnumLifecycleStageType.UNKNOWN;
		int pos = 0;
		for( String key : Common.getSortedKeys( map ) ) {
			LifecyclePhase phase = map.get( key );
			if( type != phase.LIFECYCLESTAGE_TYPE ) {
				pos = 0;
				type = phase.LIFECYCLESTAGE_TYPE;
			}
			else
				pos++;
			
			phase.STAGE_POS = pos;
			phases.add( phase );
		}
	}

	public void createLifecycle( String name , String desc , DBEnumLifecycleType type ) throws Exception {
		modifyLifecycle( name , desc , type );
	}
	
	public void modifyLifecycle( String name , String desc , DBEnumLifecycleType type ) throws Exception {
		this.NAME = name;
		this.DESC = Common.nonull( desc );
		this.LIFECYCLE_TYPE = type;
	}
	
	public void setLifecycleData( boolean regular , int daysRelease , int daysDeploy , int shiftDays ) throws Exception {
		REGULAR = regular;
		DAYS_TO_RELEASE = daysRelease;
		DAYS_TO_DEPLOY = daysDeploy;
		SHIFT_DAYS = shiftDays;
	}

	public void setEnabled( boolean enabled ) throws Exception {
		this.ENABLED = enabled;
	}

	public synchronized LifecyclePhase[] getPhases() {
		return( phases.toArray( new LifecyclePhase[0] ) );
	}
	
	public synchronized void setPhases( LifecyclePhase[] phasesNew ) throws Exception {
		for( LifecyclePhase phase : phases )
			phase.deleteObject();
		phases.clear();
		
		for( LifecyclePhase phase : phasesNew )
			addPhase( phase );
			
		rebuild();
		
		if( !isValid() )
			Common.exit1( _Error.LifecycleWrongSettings1 , "Wrong phase settings of lifecycle=" + NAME , NAME );
	}

	public boolean isValid() {
		int nRelease = 0;
		int nDeploy = 0;
		int nReleaseDays = 0;
		int nDeployDays = 0;
		for( LifecyclePhase phase : phases ) {
			if( phase.isRelease() ) {
				nRelease++;
				nReleaseDays += phase.getDuration();
			}
			else
			if( phase.isDeploy() ) {
				nDeploy++;
				nDeployDays += phase.getDuration();
			}
		}
		
		if( nRelease == 0 )
			return( false );
		
		if( nDeploy == 0 )
			return( false );
		
		if( DAYS_TO_RELEASE > 0 && nReleaseDays > DAYS_TO_RELEASE )
			return( false );
		
		if( DAYS_TO_DEPLOY > 0 && nDeployDays > DAYS_TO_DEPLOY )
			return( false );
		
		return( true );
	}

	public boolean isMajor() {
		return( LIFECYCLE_TYPE == DBEnumLifecycleType.MAJOR );
	}

	public boolean isMinor() {
		return( LIFECYCLE_TYPE == DBEnumLifecycleType.MINOR );
	}

	public boolean isUrgent() {
		return( LIFECYCLE_TYPE == DBEnumLifecycleType.URGENT );
	}

	public boolean isRegular() {
		return( REGULAR );
	}

	public static Date getReleaseDate( ActionBase action , String RELEASEVER , Meta meta ) throws Exception {
		VersionInfo info = VersionInfo.getReleaseDirInfo( RELEASEVER );
		String prevReleaseVer = info.getPreviousVersion();
		if( prevReleaseVer.isEmpty() )
			return( null );
		
		EngineBlotterSet blotter = action.getBlotter( BlotterType.BLOTTER_RELEASE );
		EngineBlotterReleaseItem item = blotter.findReleaseItem( meta.name , prevReleaseVer );
		if( item == null )
			return( null );
		
		String LIFECYCLE = item.repoItem.dist.release.schedule.LIFECYCLE;
		if( LIFECYCLE.isEmpty() )
			return( null );
		
		EngineLifecycles lifecycles = action.getServerReleaseLifecycles();
		ReleaseLifecycle lc = lifecycles.findLifecycle( LIFECYCLE );
		if( lc == null )
			return( null );
		
		return( lc.getNextReleaseDate( action , item.repoItem.dist.release ) );
	}
	
	public Date getNextReleaseDate( ActionBase action , Release release ) throws Exception {
		if( isRegular() )
			return( Common.addDays( release.schedule.releaseDate , SHIFT_DAYS ) );
		
		return( Common.addDays( release.schedule.releaseDate , DAYS_TO_RELEASE ) );
	}

}
