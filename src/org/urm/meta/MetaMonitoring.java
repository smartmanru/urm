package org.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.ConfReader;
import org.urm.action.ActionBase;
import org.urm.storage.MetadataStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MetaMonitoring {
	
	Metadata meta;
	boolean loaded = false;

	Map<String,MetaMonitoringTarget> mapEnvs;

	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_RES;
	public String RESOURCE_CONTEXT;
	
	public int MAJORINTERVAL;
	public int MINORINTERVAL;
	public int MINSILENT;
	
	public MetaMonitoring( Metadata meta ) {
		this.meta = meta;
	}
	
	public Map<String,MetaMonitoringTarget> getTargets( ActionBase action ) throws Exception { 
		return( mapEnvs );
	}
	
	public void load( ActionBase action , MetadataStorage storage ) throws Exception {
		if( loaded )
			return;

		loaded = true;
		
		// read xml
		String file = storage.getMonitoringFile( action );
		
		action.debug( "read monitoring definition file " + file + "..." );
		Document doc = ConfReader.readXmlFile( action , file );

		loadProperties( action , doc.getDocumentElement() );
		loadEnvironments( action , ConfReader.xmlGetPathNode( action , doc.getDocumentElement() , "scope" ) );
	}

	private void loadProperties( ActionBase action , Node node ) throws Exception {
		DIR_DATA = ConfReader.getRequiredPropertyValue( action , node , "dataPath" );
		DIR_REPORTS = ConfReader.getRequiredPropertyValue( action , node , "reportPath" );
		DIR_RES = ConfReader.getRequiredPropertyValue( action , node , "resourcePath" );
		RESOURCE_CONTEXT = ConfReader.getRequiredPropertyValue( action , node , "resourceContext" );
		
		MAJORINTERVAL = ConfReader.getIntegerPropertyValue( action , node , "majorInterval" , 300 );
		MINORINTERVAL = ConfReader.getIntegerPropertyValue( action , node , "minorInterval" , 60 );
		MINSILENT = ConfReader.getIntegerPropertyValue( action , node , "minSilent" , 30 );
	}
	
	private void loadEnvironments( ActionBase action , Node node ) throws Exception {
		if( node == null )
			action.exit( "no environments defined for monitoring" );

		mapEnvs = new HashMap<String,MetaMonitoringTarget>();
		Node[] items = ConfReader.xmlGetChildren( action , node , "environment" );
		if( items == null )
			return;
		
		for( Node deliveryNode : items ) {
			MetaMonitoringTarget item = new MetaMonitoringTarget( meta , this );
			item.loadEnv( action , deliveryNode );
			mapEnvs.put( item.NAME , item );
		}
	}
	
}
