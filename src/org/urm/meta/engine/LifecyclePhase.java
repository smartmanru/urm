package org.urm.meta.engine;

import org.urm.db.core.DBEnums.*;
import org.urm.meta.EngineObject;

public class LifecyclePhase extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_STAGE = "stage";
	public static String PROPERTY_STAGE_POS = "stage_pos";
	public static String PROPERTY_UNLIMITED = "unlimited";
	public static String PROPERTY_START_DAY = "start_day";
	public static String PROPERTY_DAYS = "days";
	
	public ReleaseLifecycle lc;
	
	public int ID;
	public String NAME;
	public String DESC;
	public DBEnumLifecycleStageType LIFECYCLESTAGE_TYPE;
	public int STAGE_POS;
	public boolean UNLIMITED;
	public boolean START_DAY;
	public int DAYS;
	public int CV;
	
	public LifecyclePhase( ReleaseLifecycle lc ) {
		super( lc );
		this.lc = lc;
	}
	
	@Override
	public String getName() {
		return( NAME );
	}
	
	public LifecyclePhase copy( ReleaseLifecycle rlc ) throws Exception {
		LifecyclePhase r = new LifecyclePhase( rlc );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.LIFECYCLESTAGE_TYPE = LIFECYCLESTAGE_TYPE;
		r.STAGE_POS = STAGE_POS;
		r.UNLIMITED = UNLIMITED;
		r.START_DAY = START_DAY;
		r.DAYS = DAYS;
		r.CV = CV;
		return( r );
	}

	public void createPhase( String name , String desc , DBEnumLifecycleStageType stage , int pos , boolean unlimited , boolean startDay , int days ) throws Exception {
		modifyPhase( name , desc , stage , pos , unlimited , startDay , days );
	}
	
	public void modifyPhase( String name , String desc , DBEnumLifecycleStageType stage , int pos , boolean unlimited , boolean startDay , int days ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.LIFECYCLESTAGE_TYPE = stage;
		this.STAGE_POS = pos;
		this.UNLIMITED = unlimited;
		this.START_DAY = startDay;
		this.DAYS = days;
	}
	
	public boolean isRelease() {
		if( LIFECYCLESTAGE_TYPE == DBEnumLifecycleStageType.RELEASE )
			return( true );
		return( false );
	}

	public boolean isDeploy() {
		if( LIFECYCLESTAGE_TYPE == DBEnumLifecycleStageType.DEPLOYMENT )
			return( true );
		return( false );
	}

	public int getDuration() {
		if( DAYS < 0 )
			return( 0 );
		return( DAYS );
	}

	public boolean isUnlimited() {
		if( DAYS < 0 )
			return( true );
		return( false );
	}
	
}
