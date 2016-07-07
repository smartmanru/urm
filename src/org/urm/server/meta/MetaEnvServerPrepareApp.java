package org.urm.server.meta;

import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.urm.server.action.PropertySet;
import org.w3c.dom.Node;

public class MetaEnvServerPrepareApp {

	protected Metadata meta;
	public MetaEnvServerBase base;

	public String APP;
	public PropertySet properties;
	
	public MetaEnvServerPrepareApp( Metadata meta , MetaEnvServerBase base ) {
		this.meta = meta;
		this.base = base;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		APP = ConfReader.getRequiredAttrValue( node , "app" );
		properties = new PropertySet( "prepare" , base.properties );
		properties.loadRawFromElements( action , node );
		properties.moveRawAsStrings( action );
	}
	
}
