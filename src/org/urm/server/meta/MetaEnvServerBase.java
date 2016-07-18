package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public class MetaEnvServerBase {

	protected Metadata meta;
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
		properties.loadRawFromAttributes( node );
		ID = properties.getSystemRequiredStringProperty( "id" );
		properties.finishRawProperties();
		properties.loadRawFromElements( node );
		properties.moveRawAsStrings();
		
		loadPrepare( action , node );
	}
		
	private void loadPrepare( ActionBase action , Node node ) throws Exception {
		prepareMap = new HashMap<String,MetaEnvServerPrepareApp>();
		
		Node[] items = ConfReader.xmlGetChildren( node , "prepare" );
		if( items == null )
			return;
		
		for( Node snnode : items ) {
			MetaEnvServerPrepareApp sn = new MetaEnvServerPrepareApp( meta , this );
			sn.load( action , snnode );
			prepareMap.put( sn.APP , sn );
		}
	}
	
}
