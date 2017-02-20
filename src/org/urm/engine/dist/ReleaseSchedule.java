package org.urm.engine.dist;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.engine.ServerReleaseLifecycle;
import org.urm.meta.engine.ServerReleaseLifecyclePhase;
import org.urm.meta.engine.ServerReleaseLifecycles;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseSchedule {

	public Meta meta;
	public Release release;
	
	public String LIFECYCLE;
	public Date started;
	public Date releaseDate;
	public int currentPhase;
	public int releasePhases;
	public int deployPhases;
	
	public List<ReleaseSchedulePhase> phases;
	
	public ReleaseSchedule( Meta meta , Release release ) {
		this.meta = meta;
		this.release = release;
		phases = new LinkedList<ReleaseSchedulePhase>();
		currentPhase = 0;
	}
	
	public ReleaseSchedule copy( ActionBase action , Meta meta , Release release ) throws Exception {
		ReleaseSchedule r = new ReleaseSchedule( meta , release );
		r.LIFECYCLE = LIFECYCLE;
		r.started = started;
		r.releaseDate = releaseDate;
		r.currentPhase = currentPhase;
		r.releasePhases = releasePhases; 
		r.deployPhases = deployPhases; 
		
		for( ReleaseSchedulePhase phase : phases ) {
			ReleaseSchedulePhase rphase = phase.copy( action , meta , r );
			r.phases.add( rphase );
		}
		return( r );
	}

	public void load( ActionBase action , Node root ) throws Exception {
		phases.clear();
		
		Node node = ConfReader.xmlGetFirstChild( root , "schedule" );
		if( node == null ) {
			LIFECYCLE = "";
			releaseDate = null;
			currentPhase = 0;
			releasePhases = 0; 
			deployPhases = 0; 
			return;
		}
		
		LIFECYCLE = ConfReader.getAttrValue( node , "lifecycle" , "" );
		started = Common.getDateValue( ConfReader.getAttrValue( node , "started" ) );
		releaseDate = Common.getDateValue( ConfReader.getAttrValue( node , "releasedate" ) );
		currentPhase = ConfReader.getIntegerAttrValue( node , "phase" , 0 );
		
		Node[] items = ConfReader.xmlGetChildren( root , "phase" );
		if( items == null )
			return;
		
		int pos = 0;
		for( Node phaseNode : items ) {
			ReleaseSchedulePhase phase = new ReleaseSchedulePhase( meta , this );
			phase.load( action , phaseNode , pos );
			phases.add( phase );
			pos++;
			if( phase.release )
				releasePhases++;
			else
				deployPhases++;
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Element node = Common.xmlCreateElement( doc , root , "schedule" );
		Common.xmlSetElementAttr( doc , node , "lifecycle" , LIFECYCLE );
		Common.xmlSetElementAttr( doc , node , "started" , Common.getDateValue( started ) );
		Common.xmlSetElementAttr( doc , node , "releasedate" , Common.getDateValue( releaseDate ) );
		Common.xmlSetElementAttr( doc , node , "phase" , "" + currentPhase );
		
		for( ReleaseSchedulePhase phase : phases ) {
			Element phaseElement = Common.xmlCreateElement( doc , node , "phase" );
			phase.save( action , doc , phaseElement );
		}
	}
	
	public void createReleaseSchedule( ActionBase action , Date releaseDate , ServerReleaseLifecycle lc ) throws Exception {
		this.LIFECYCLE = ( lc == null )? "" : lc.ID;
		currentPhase = 0;
		started = Common.getDateDay( System.currentTimeMillis() );
		phases.clear();
		
		if( lc != null ) {
			int pos = 0;
			releasePhases = 0; 
			deployPhases = 0;
			
			for( ServerReleaseLifecyclePhase lcPhase : lc.getPhases() ) {
				ReleaseSchedulePhase phase = new ReleaseSchedulePhase( meta , this );
				phase.create( action , lcPhase , pos );
				phases.add( phase );
				pos++;
				
				if( phase.release )
					releasePhases++;
				else
					deployPhases++;
			}
		}
		
		changeReleaseSchedule( action , releaseDate );
	}
	
	public void changeReleaseSchedule( ActionBase action , Date releaseDate ) throws Exception {
		this.releaseDate = releaseDate;
	}

	public ServerReleaseLifecycle getLifecycle( ActionBase action ) throws Exception {
		if( LIFECYCLE.isEmpty() )
			return( null );
		
		ServerReleaseLifecycles lifecycles = action.getServerReleaseLifecycles();
		return( lifecycles.findLifecycle( LIFECYCLE ) );
	}

	public ReleaseSchedulePhase[] getPhases() {
		return( phases.toArray( new ReleaseSchedulePhase[0] ) );
	}

	public int getPhaseCount() {
		return( phases.size() );
	}
	
	public ReleaseSchedulePhase getPhase( int index ) {
		return( phases.get( index ) );
	}
	
}
