package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.server.ServerRegistry;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaMonitoring {
	
	private boolean loaded;
	public boolean loadFailed;

	protected Meta meta;

	Map<String,MetaMonitoringTarget> mapEnvs;

	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_RES;
	public String RESOURCE_CONTEXT;
	
	public int MAJORINTERVAL;
	public int MINORINTERVAL;
	public int MINSILENT;
	
	public MetaMonitoring( Meta meta ) {
		this.meta = meta;
		loaded = false;
		loadFailed = false;
	}
	
	public MetaMonitoring copy( ActionBase action , Meta meta ) throws Exception {
		MetaMonitoring r = new MetaMonitoring( meta );
		return( r );
	}
	
	public void setLoadFailed() {
		loadFailed = true;
	}
	
	public void createInitial( ActionBase action , ServerRegistry registry ) throws Exception {
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		
		loadProperties( action , root );
		loadEnvironments( action , ConfReader.xmlGetPathNode( root , "scope" ) );
	}

	public Map<String,MetaMonitoringTarget> getTargets( ActionBase action ) throws Exception { 
		return( mapEnvs );
	}
	
	private void loadProperties( ActionBase action , Node node ) throws Exception {
		DIR_DATA = ConfReader.getRequiredPropertyValue( node , "dataPath" );
		DIR_REPORTS = ConfReader.getRequiredPropertyValue( node , "reportPath" );
		DIR_RES = ConfReader.getRequiredPropertyValue( node , "resourcePath" );
		RESOURCE_CONTEXT = ConfReader.getRequiredPropertyValue( node , "resourceContext" );
		
		MAJORINTERVAL = ConfReader.getIntegerPropertyValue( node , "majorInterval" , 300 );
		MINORINTERVAL = ConfReader.getIntegerPropertyValue( node , "minorInterval" , 60 );
		MINSILENT = ConfReader.getIntegerPropertyValue( node , "minSilent" , 30 );
	}
	
	private void loadEnvironments( ActionBase action , Node node ) throws Exception {
		if( node == null )
			action.exit( "no environments defined for monitoring" );

		mapEnvs = new HashMap<String,MetaMonitoringTarget>();
		Node[] items = ConfReader.xmlGetChildren( node , "environment" );
		if( items == null )
			return;
		
		for( Node deliveryNode : items ) {
			MetaMonitoringTarget item = new MetaMonitoringTarget( meta , this );
			item.loadEnv( action , deliveryNode );
			mapEnvs.put( item.NAME , item );
		}
	}
	
}
