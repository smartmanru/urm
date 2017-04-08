package org.urm.engine.dist;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.engine.ServerReleaseLifecyclePhase;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseSchedulePhase {

	Meta meta;
	ReleaseSchedule schedule;

	public int pos;
	public String name;
	
	private int days;
	private int normalDays;
	private boolean release;
	private boolean finished;
	private boolean unlimited;
	private Date startDate;
	private Date finishDate;
	private Date deadlineStart;
	private Date deadlineFinish;
	private Date bestStart;
	private Date bestFinish;
	
	public ReleaseSchedulePhase( Meta meta , ReleaseSchedule schedule ) {
		this.meta = meta;
		this.schedule = schedule;
		
		pos = 0;
		days = 0;
		normalDays = 0;
		release = false;
		finished = false;
	}
	
	public ReleaseSchedulePhase copy( ActionBase action , Meta meta , ReleaseSchedule schedule ) throws Exception {
		ReleaseSchedulePhase r = new ReleaseSchedulePhase( meta , schedule );
		r.pos = pos;
		r.name = name;
		r.days = days;
		r.normalDays = normalDays;
		r.release = release;
		r.finished = finished;
		r.startDate = startDate;
		r.finishDate = finishDate;
		r.deadlineStart = deadlineStart;
		r.deadlineFinish = deadlineFinish;
		r.bestStart = bestStart;
		r.bestFinish = bestFinish;
		return( r );
	}

	public void load( ActionBase action , Node root , int pos , int current ) throws Exception {
		this.pos = pos;
		
		name = ConfReader.getRequiredAttrValue( root , "name" );
		days = ConfReader.getIntegerAttrValue( root , "days" , 0 );
		normalDays = ConfReader.getIntegerAttrValue( root , "normaldays" , 0 );
		release = ConfReader.getBooleanAttrValue( root , "release" , false );
		unlimited = ConfReader.getBooleanAttrValue( root , "unlimited" , false );

		if( current >= 0 && current < pos )
			startDate = null;
		else
			startDate = Common.getDateValue( ConfReader.getAttrValue( root , "startdate" ) );
		
		if( current >= 0 && current <= pos ) {
			finished = false;
			finishDate = null;
		}
		else {
			finished = ConfReader.getBooleanAttrValue( root , "finished" , false );
			if( finished )
				finishDate = Common.getDateValue( ConfReader.getAttrValue( root , "finishdate" ) );
		}
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "name" , name );
		Common.xmlSetElementAttr( doc , root , "days" , "" + days );
		Common.xmlSetElementAttr( doc , root , "normaldays" , "" + normalDays );
		Common.xmlSetElementAttr( doc , root , "release" , Common.getBooleanValue( release ) );
		Common.xmlSetElementAttr( doc , root , "finished" , Common.getBooleanValue( finished ) );
		Common.xmlSetElementAttr( doc , root , "unlimited" , Common.getBooleanValue( unlimited ) );
		Common.xmlSetElementAttr( doc , root , "startdate" , Common.getDateValue( startDate ) );
		if( finished )
			Common.xmlSetElementAttr( doc , root , "finishdate" , Common.getDateValue( finishDate ) );
	}
	
	public void create( ActionBase action , ServerReleaseLifecyclePhase lcPhase , int pos ) throws Exception {
		this.pos = pos;
		this.name = lcPhase.ID;
		
		this.unlimited = lcPhase.isUnlimited();
		this.days = lcPhase.getDuration();
		this.normalDays = this.days;
		this.release = lcPhase.isRelease();
		this.finished = false;
		this.startDate = null;
		this.finishDate = null;
	}

	public boolean isRelease() {
		return( release );
	}
	
	public boolean isDeploy() {
		if( release )
			return( false );
		return( true );
	}
	
	public boolean isStarted() {
		if( startDate != null )
			return( true );
		return( false );
	}
	
	public boolean isFinished() {
		return( finished );
	}
	
	public int getDaysPassed() {
		if( startDate == null )
			return( -1 );
		Date currentDate = Common.getDateCurrentDay();
		int ndays = Common.getDateDiffDays( startDate , currentDate );
		if( requireStartDay() )
			ndays++;
		return( ndays );
	}
	
	public int getDaysExpected() {
		return( days );
	}
	
	public int getDaysBest() {
		return( normalDays );
	}
	
	public Date getDeadlineStart() {
		return( deadlineStart );
	}

	public Date getBestStart() {
		return( bestStart );
	}

	public Date getDeadlineFinish() {
		return( deadlineFinish );
	}

	public Date getBestFinish() {
		return( bestFinish );
	}

	public Date getStartDate() {
		return( startDate );
	}
	
	public Date getFinishDate() {
		return( finishDate );
	}
	
	public Date getDateBeforePhaseExpected() {
		if( !requireStartDay() )
			return( deadlineStart );
		
		return( Common.addDays( deadlineStart , -1 ) );
	}
	
	public Date getDateBeforePhaseBest() {
		if( !requireStartDay() )
			return( bestStart );
		
		return( Common.addDays( bestStart , -1 ) );
	}
	
	public boolean requireStartDay() {
		return( ( unlimited || normalDays > 0 )? true : false );
	}
	
	public int getDaysActually() {
		if( startDate == null || finishDate == null )
			return( -1 );
			
		int diff = Common.getDateDiffDays( startDate.getTime() , finishDate.getTime() );
		if( requireStartDay() )
			diff++;
			
		return( diff );
	}

	public void setDeadlineDateExpected( Date deadlineFinish ) {
		this.deadlineFinish = deadlineFinish;
		
		if( days > 0 )
			this.deadlineStart = Common.addDays( deadlineFinish , -(days-1) );
		else
			this.deadlineStart = deadlineFinish;
	}

	public void setDeadlineDateBest( Date bestFinish ) {
		this.bestFinish = bestFinish;
		
		if( normalDays > 0 )
			this.bestStart = Common.addDays( bestFinish , -(normalDays-1) );
		else
			this.bestStart = bestFinish;
	}

	public void setStartDateExpected( Date deadlineStart ) {
		if( requireStartDay() )
			this.deadlineStart = Common.addDays( deadlineStart , 1 );
		else
			this.deadlineStart = deadlineStart;
		
		if( days > 0 )
			this.deadlineFinish = Common.addDays( this.deadlineStart , (days-1) );
		else
			this.deadlineFinish = this.deadlineStart;
	}

	public void setStartDateBest( Date bestStart ) {
		if( requireStartDay() )
			this.bestStart = Common.addDays( bestStart , 1 );
		else
			this.bestStart = bestStart;
		
		if( normalDays > 0 )
			this.bestFinish = Common.addDays( this.bestStart , (normalDays-1) );
		else
			this.bestFinish = this.bestStart;
	}

	public void startPhase( ActionBase action , Date date ) throws Exception {
		startDate = date;
		finished = false;
		finishDate = null;
	}
	
	public void finishPhase( ActionBase action , Date date ) throws Exception {
		finished = true;
		finishDate = date;
	}
	
	public void reopenPhase( ActionBase action ) throws Exception {
		finished = false;
		finishDate = null;
	}
	
	public void clearPhase( ActionBase action ) throws Exception {
		startDate = null;
		finished = false;
		finishDate = null;
	}

	public void setDuration( ActionBase action , int duration ) throws Exception {
		this.days = duration;
	}

	private void changeDays() {
		days = Common.getDateDiffDays( deadlineStart , deadlineFinish );
		if( requireStartDay() )
			days++;
	}
	
	public void setFinishDeadline( ActionBase action , Date deadlineDate , boolean shiftStart ) throws Exception {
		deadlineFinish = deadlineDate;
		if( shiftStart ) {
			if( days > 0 )
				deadlineStart = Common.addDays( deadlineFinish , -(days-1) );
			else
				deadlineStart = deadlineFinish;
		}
		else
			changeDays();
	}
	
	public void setStartDeadline( ActionBase action , Date deadlineDate , boolean shiftFinish ) throws Exception {
		deadlineStart = deadlineDate;
		if( shiftFinish ) {
			if( days > 0 )
				deadlineFinish = Common.addDays( deadlineStart , days - 1 );
			else
				deadlineFinish = deadlineStart;
		}
		else
			changeDays();
	}

	public void setDeadlines( ActionBase action , Date deadlineStart , Date deadlineFinish ) throws Exception {
		this.deadlineStart = deadlineStart;
		this.deadlineFinish = deadlineFinish;
		changeDays();
	}
	
}
