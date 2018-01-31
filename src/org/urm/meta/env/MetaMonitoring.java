package org.urm.meta.env;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaMonitoring {

	public Meta meta;
	
	private Map<Integer,MetaMonitoringTarget> mapTargets;

	public MetaMonitoring( ProductMeta storage , Meta meta ) {
		this.meta = meta;
		
		mapTargets = new HashMap<Integer,MetaMonitoringTarget>();
	}
	
	public MetaMonitoring copy( Meta rmeta ) throws Exception {
		MetaMonitoring r = new MetaMonitoring( rmeta.getStorage() , rmeta );
		
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			MetaMonitoringTarget rtarget = target.copy( meta , r );
			r.mapTargets.put( target.ENVSG.FKID , rtarget );
		}
		
		return( r );
	}

	public void load( ActionBase action , Node root ) throws Exception {
		loadTargets( action , ConfReader.xmlGetPathNode( root , "scope" ) );
	}

	public MetaMonitoringTarget[] getTargets() { 
		return( mapTargets.values().toArray( new MetaMonitoringTarget[0] ) );
	}
	
	private void loadTargets( ActionBase action , Node node ) throws Exception {
		mapTargets.clear();
		
		Node[] items = null;
		if( node != null )
			items = ConfReader.xmlGetChildren( node , "target" );
		
		if( items == null ) {
			action.info( "no targets defined for monitoring" );
			return;
		}

		for( Node deliveryNode : items ) {
			MetaMonitoringTarget item = new MetaMonitoringTarget( meta , this );
			item.loadTarget( action , deliveryNode );
			addTarget( item );
		}
	}
	
	private void addTarget( MetaMonitoringTarget target ) {
		mapTargets.put( target.ID , target );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Element scope = Common.xmlCreateElement( doc , root , "scope" );
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			Element element = Common.xmlCreateElement( doc , scope , "target" );
			target.save( action , doc , element );
		}
	}

	public MetaMonitoringTarget findMonitoringTarget( MetaEnvSegment sg ) {
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			MetaEnvSegment sgTarget = target.findSegment();
			if( sgTarget == sg )
				return( target );
		}
		return( null );
	}

	public MetaMonitoringTarget modifyTarget( EngineTransaction transaction , MetaEnvSegment sg , boolean major , boolean enabled , int maxTime , ScheduleProperties schedule ) throws Exception {
		MetaMonitoringTarget target = findMonitoringTarget( sg );
		if( target == null ) {
			target = new MetaMonitoringTarget( meta , this );
			target.createTarget( transaction , sg );
			addTarget( target );
		}
		
		target.modifyTarget( transaction , major , enabled , schedule , maxTime );
		return( target );
	}

	public MetaEnvSegment getTargetSegment( MetaMonitoringTarget target ) {
		MetaEnvs envs = meta.getEnviroments();
		MetaEnv env = envs.findEnv( target.getMatchEnvName() );
		return( env.findSegment( target.getMatchSgName() ) );
	}
	
}
