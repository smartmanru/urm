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
	public int days;
	public int normalDays;
	public boolean release;
	public boolean finished;
	public boolean unlimited;
	public Date startDate;
	public Date finishDate;
	public Date deadlineStart;
	public Date deadlineFinish;
	public Date bestStart;
	public Date bestFinish;
	
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

	public void load( ActionBase action , Node root , int pos ) throws Exception {
		this.pos = pos;
		
		name = ConfReader.getRequiredAttrValue( root , "name" );
		days = ConfReader.getIntegerAttrValue( root , "days" , 0 );
		normalDays = ConfReader.getIntegerAttrValue( root , "normaldays" , 0 );
		release = ConfReader.getBooleanAttrValue( root , "release" , false );
		finished = ConfReader.getBooleanAttrValue( root , "finished" , false );
		unlimited = ConfReader.getBooleanAttrValue( root , "unlimited" , false );
		startDate = Common.getDateValue( ConfReader.getAttrValue( root , "startdate" ) );
		if( finished )
			finishDate = Common.getDateValue( ConfReader.getAttrValue( root , "finishdate" ) );
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

	public int getDaysActually() {
		if( startDate == null || finishDate == null )
			return( -1 );
			
		int diff = Common.getDateDiffDays( startDate.getTime() , finishDate.getTime() );
		return( diff );
	}

	public void setDeadlineDates( Date deadlineStart , Date deadlineFinish , Date bestStart , Date bestFinish ) {
		this.deadlineStart = deadlineStart;
		this.deadlineFinish = deadlineFinish;
		this.bestStart = bestStart;
		this.bestFinish = bestFinish;
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
	
}