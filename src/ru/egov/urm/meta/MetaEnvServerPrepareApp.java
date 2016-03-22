package ru.egov.urm.meta;

import org.w3c.dom.Node;

import ru.egov.urm.ConfReader;
import ru.egov.urm.PropertySet;
import ru.egov.urm.action.ActionBase;

public class MetaEnvServerPrepareApp {

	public MetaEnvServerBase base;

	public String APP;
	public PropertySet properties;
	
	public MetaEnvServerPrepareApp( MetaEnvServerBase base ) {
		this.base = base;
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		APP = ConfReader.getRequiredAttrValue( action , node , "app" );
		properties = new PropertySet( "prepare" , base.properties );
		properties.loadFromElements( action , node );
	}
	
}
