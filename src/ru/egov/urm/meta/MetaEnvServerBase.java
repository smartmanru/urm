package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.PropertySet;
import ru.egov.urm.action.ActionBase;

public class MetaEnvServerBase {

	Metadata meta;
	public MetaEnvServer server;

	public String ID;
	public Map<String,MetaEnvServerPrepareApp> prepareMap;
	public PropertySet properties;
	
	public MetaEnvServerBase( Metadata meta , MetaEnvServer server ) {
		this.meta = meta;
		this.server = server;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		properties = new PropertySet( "base" , server.properties );
		properties.loadRawFromAttributes( action , node );
		ID = properties.getSystemRequiredStringProperty( action , "id" );
		properties.finishRawProperties( action );
		properties.loadRawFromElements( action , node );
		properties.moveRawAsStrings( action );
		
		loadPrepare( action , node );
	}
		
	private void loadPrepare( ActionBase action , Node node ) throws Exception {
		prepareMap = new HashMap<String,MetaEnvServerPrepareApp>();
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "prepare" );
		if( items == null )
			return;
		
		for( Node snnode : items ) {
			MetaEnvServerPrepareApp sn = new MetaEnvServerPrepareApp( meta , this );
			sn.load( action , snnode );
			prepareMap.put( sn.APP , sn );
		}
	}
	
}
