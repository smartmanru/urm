package org.urm.engine.meta;

import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.engine.action.ActionBase;
import org.w3c.dom.Node;

public class MetaEnvServerPrepareApp {

	protected Meta meta;
	public MetaEnvServerBase base;

	public String APP;
	public PropertySet properties;
	
	public MetaEnvServerPrepareApp( Meta meta , MetaEnvServerBase base ) {
		this.meta = meta;
		this.base = base;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		APP = ConfReader.getRequiredAttrValue( node , "app" );
		properties = new PropertySet( "prepare" , base.properties );
		properties.loadRawFromNodeElements( node );
		properties.resolveRawProperties();
	}
	
}
