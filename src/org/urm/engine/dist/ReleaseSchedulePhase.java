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
	public Date startDate;
	public Date finishDate;
	public Date deadlineStart;
	public Date deadlineFinish;
	
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
		return( r );
	}

	public void load( ActionBase action , Node root , int pos ) throws Exception {
		this.pos = pos;
		
		name = ConfReader.getRequiredAttrValue( root , "name" );
		days = ConfReader.getIntegerAttrValue( root , "days" , 0 );
		normalDays = ConfReader.getIntegerAttrValue( root , "normaldays" , 0 );
		release = ConfReader.getBooleanAttrValue( root , "release" , false );
		finished = ConfReader.getBooleanAttrValue( root , "finished" , false );
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
		Common.xmlSetElementAttr( doc , root , "startdate" , Common.getDateValue( startDate ) );
		if( finished )
			Common.xmlSetElementAttr( doc , root , "finishdate" , Common.getDateValue( finishDate ) );
	}
	
	public void create( ActionBase action , ServerReleaseLifecyclePhase lcPhase , int pos ) throws Exception {
		this.pos = pos;
		this.name = lcPhase.ID;
		this.days = lcPhase.days;
		this.normalDays = lcPhase.days;
		this.release = lcPhase.isRelease();
		this.finished = false;
		this.startDate = null;
		if( pos == 0 )
			this.startDate = schedule.started;
		this.finishDate = null;
	}

	public int getDaysActually() {
		if( !finished )
			return( -1 );
			
		int startDateIndex = Common.getDayIndex( startDate.getTime() );
		int finishIndex = Common.getDayIndex( finishDate.getTime() );
		return( finishIndex - startDateIndex );
	}

	public void setDeadlineDates( Date dateStart , Date dateFinish ) {
		this.deadlineStart = dateStart;
		this.deadlineFinish = dateFinish;
	}
	
}
