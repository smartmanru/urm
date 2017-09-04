package org.urm.meta.engine;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.blotter.ServerBlotterReleaseItem;
import org.urm.engine.blotter.ServerBlotterSet;
import org.urm.engine.blotter.ServerBlotter.BlotterType;
import org.urm.engine.dist.Release;
import org.urm.engine.dist.VersionInfo;
import org.urm.meta.ServerObject;
import org.urm.meta.Types;
import org.urm.meta.Types.VarLCTYPE;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerReleaseLifecycle extends ServerObject {

	ServerReleaseLifecycles lifecycles;
	
	public VarLCTYPE lcType;
	public String ID;
	public String DESC;
	public boolean enabled;
	
	public boolean regular;
	public int daysToRelease;
	public int daysToDeploy;
	public int shiftDays;
	
	public int releasePhases;
	public int deployPhases;
	
	List<ServerReleaseLifecyclePhase> phases;
	
	public ServerReleaseLifecycle( ServerReleaseLifecycles lifecycles ) {
		super( lifecycles );
		this.lifecycles = lifecycles;
		phases = new LinkedList<ServerReleaseLifecyclePhase>();
		enabled = false;
		releasePhases = 0;
		deployPhases = 0;
	}
	
	@Override
	public String getName() {
		return( ID );
	}
	
	public ServerReleaseLifecycle copy( ServerReleaseLifecycles lifecycles ) throws Exception {
		ServerReleaseLifecycle r = new ServerReleaseLifecycle( lifecycles );
		r.lcType = lcType;
		r.ID = ID;
		r.DESC = DESC;
		r.enabled = enabled;
		r.regular = regular;
		r.daysToRelease = daysToRelease;
		r.daysToDeploy = daysToDeploy;
		r.shiftDays = shiftDays;
		r.releasePhases = releasePhases;
		r.deployPhases = deployPhases;
		
		for( ServerReleaseLifecyclePhase phase : phases ) {
			ServerReleaseLifecyclePhase rphase = phase.copy( r );
			r.addPhase( rphase );
		}
		return( r );
	}
	
	private void addPhase( ServerReleaseLifecyclePhase phase ) {
		phases.add( phase );
	}
	
	public ServerReleaseLifecyclePhase getPhase( int index ) {
		return( phases.get( index ) );
	}
	
	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		lcType = Types.getLCType( ConfReader.getAttrValue( root , "type" ) , true );
		ID = ConfReader.getAttrValue( root , "id" );
		DESC = ConfReader.getAttrValue( root , "desc" );
		enabled = ConfReader.getBooleanAttrValue( root , "enabled" , false );
		regular = ConfReader.getBooleanAttrValue( root , "regular" , false );
		daysToRelease = ConfReader.getIntegerAttrValue( root , "releasedays" , 0 );
		daysToDeploy = ConfReader.getIntegerAttrValue( root , "deploydays" , 0 );
		shiftDays = ConfReader.getIntegerAttrValue( root , "shiftdays" , 0 );
		
		Node[] list = ConfReader.xmlGetChildren( root , "phase" );
		if( list == null )
			return;
		
		releasePhases = 0;
		deployPhases = 0;
		for( Node node : list ) {
			ServerReleaseLifecyclePhase phase = new ServerReleaseLifecyclePhase( this );
			phase.load( node );
			addPhase( phase );
			
			if( phase.isRelease() )
				releasePhases++;
			else
			if( phase.isRelease() )
				deployPhases++;
		}
	}

	public void save( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "type" , Common.getEnumLower( lcType ) );
		Common.xmlSetElementAttr( doc , root , "id" , ID );
		Common.xmlSetElementAttr( doc , root , "desc" , DESC );
		Common.xmlSetElementAttr( doc , root , "enabled" , Common.getBooleanValue( enabled ) );
		Common.xmlSetElementAttr( doc , root , "regular" , Common.getBooleanValue( regular ) );
		Common.xmlSetElementAttr( doc , root , "releasedays" , "" + daysToRelease );
		Common.xmlSetElementAttr( doc , root , "deploydays" , "" + daysToDeploy );
		Common.xmlSetElementAttr( doc , root , "shiftdays" , "" + shiftDays );
		
		for( ServerReleaseLifecyclePhase phase : phases ) {
			Element element = Common.xmlCreateElement( doc , root , "phase" );
			phase.save( doc , element );
		}
	}

	public void setLifecycleName( EngineTransaction transaction , String name , String desc ) throws Exception {
		this.ID = name;
		this.DESC = desc;
	}
	
	public void setLifecycleData( EngineTransaction transaction , ServerReleaseLifecycle src ) throws Exception {
		lcType = src.lcType;
		ID = src.ID;
		DESC = src.DESC;
		enabled = src.enabled;
		regular = src.regular;
		daysToRelease = src.daysToRelease;
		daysToDeploy = src.daysToDeploy;
		shiftDays = src.shiftDays;
	}

	public void enableLifecycle( EngineTransaction transaction , boolean enabled ) throws Exception {
		this.enabled = enabled;
	}

	public synchronized ServerReleaseLifecyclePhase[] getPhases() {
		return( phases.toArray( new ServerReleaseLifecyclePhase[0] ) );
	}
	
	public synchronized void changePhases( EngineTransaction transaction , ServerReleaseLifecyclePhase[] phasesNew ) throws Exception {
		for( ServerReleaseLifecyclePhase phase : phases )
			phase.deleteObject();
		phases.clear();
		
		enabled = false;
		releasePhases = 0;
		deployPhases = 0;
		for( ServerReleaseLifecyclePhase phase : phasesNew ) {
			ServerReleaseLifecyclePhase phaseNew = phase.copy( this );
			addPhase( phaseNew );
			
			if( phase.isRelease() )
				releasePhases++;
			else
			if( phase.isRelease() )
				deployPhases++;
		}
		
		if( !isValid() )
			transaction.exit1( _Error.LifecycleWrongSettings1 , "Wrong phase settings of lifecycle=" + ID , ID );
	}

	public boolean isValid() {
		int nRelease = 0;
		int nDeploy = 0;
		int nReleaseDays = 0;
		int nDeployDays = 0;
		for( ServerReleaseLifecyclePhase phase : phases ) {
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
		
		if( daysToRelease > 0 && nReleaseDays > daysToRelease )
			return( false );
		
		if( daysToDeploy > 0 && nDeployDays > daysToDeploy )
			return( false );
		
		return( true );
	}

	public boolean isMajor() {
		return( lcType == VarLCTYPE.MAJOR );
	}

	public boolean isMinor() {
		return( lcType == VarLCTYPE.MINOR );
	}

	public boolean isUrgent() {
		return( lcType == VarLCTYPE.URGENT );
	}

	public boolean isRegular() {
		return( regular );
	}

	public static Date getReleaseDate( ActionBase action , String RELEASEVER , Meta meta ) throws Exception {
		VersionInfo info = VersionInfo.getReleaseVersion( action , RELEASEVER );
		String prevReleaseVer = info.getPreviousVersion();
		if( prevReleaseVer.isEmpty() )
			return( null );
		
		ServerBlotterSet blotter = action.getBlotter( BlotterType.BLOTTER_RELEASE );
		ServerBlotterReleaseItem item = blotter.findReleaseItem( meta.name , prevReleaseVer );
		if( item == null )
			return( null );
		
		String LIFECYCLE = item.repoItem.dist.release.schedule.LIFECYCLE;
		if( LIFECYCLE.isEmpty() )
			return( null );
		
		ServerReleaseLifecycles lifecycles = action.getServerReleaseLifecycles();
		ServerReleaseLifecycle lc = lifecycles.findLifecycle( LIFECYCLE );
		if( lc == null )
			return( null );
		
		return( lc.getNextReleaseDate( action , item.repoItem.dist.release ) );
	}
	
	public Date getNextReleaseDate( ActionBase action , Release release ) throws Exception {
		if( isRegular() )
			return( Common.addDays( release.schedule.releaseDate , shiftDays ) );
		
		return( Common.addDays( release.schedule.releaseDate , daysToRelease ) );
	}

}
