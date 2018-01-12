package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.meta.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaMonitoring {
	
	public Meta meta;

	Map<String,MetaMonitoringTarget> mapTargets;

	public MetaMonitoring( ProductMeta storage , MetaProductSettings settings , Meta meta ) {
		this.meta = meta;
		mapTargets = new HashMap<String,MetaMonitoringTarget>();
	}
	
	public MetaMonitoring copy( ActionBase action , Meta meta ) throws Exception {
		MetaProductSettings settings = meta.getProductSettings();
		MetaMonitoring r = new MetaMonitoring( meta.getStorage() , settings , meta );
		
		for( MetaMonitoringTarget target : mapTargets.values() ) {
			MetaMonitoringTarget rtarget = target.copy( action , meta , r );
			r.mapTargets.put( target.NAME , rtarget );
		}
		
		return( r );
	}
	
	public void createMonitoring( TransactionBase transaction ) throws Exception {
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
		mapTargets.put( target.NAME , target );
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
			if( target.ENV.equals( sg.env.NAME ) && target.SG.equals( sg.NAME ) )
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

}
