package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.PropertySet;
import ru.egov.urm.action.ActionBase;

public class MetaEnvServerBase {

	public MetaEnvServer server;

	public String ID;
	public Map<String,MetaEnvServerPrepareApp> prepareMap;
	public PropertySet properties;
	
	public MetaEnvServerBase( MetaEnvServer server ) {
		this.server = server;
	}

	public void load( ActionBase action , Node node ) throws Exception {
		properties = new PropertySet( "base" , server.properties );
		List<String> systemProps = new LinkedList<String>();
		properties.loadFromAttributes( action , node );
		ID = properties.getSystemRequiredProperty( action , "id" , systemProps );
		properties.checkUnexpected( action , systemProps );
		
		loadPrepare( action , node );
	}
		
	private void loadPrepare( ActionBase action , Node node ) throws Exception {
		prepareMap = new HashMap<String,MetaEnvServerPrepareApp>();
		
		Node[] items = ConfReader.xmlGetChildren( action , node , "prepare" );
		if( items == null )
			return;
		
		for( Node snnode : items ) {
			MetaEnvServerPrepareApp sn = new MetaEnvServerPrepareApp( this );
			sn.load( action , snnode );
			prepareMap.put( sn.APP , sn );
		}
	}
	
}
