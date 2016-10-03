package org.urm.engine.meta;

import org.urm.action.ActionBase;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServerPrepareApp extends PropertyController {

	protected Meta meta;
	public MetaEnvServerBase base;

	public String APP;
	public PropertySet properties;
	
	public static String PROPERTY_APP = "app";
	
	public MetaEnvServerPrepareApp( Meta meta , MetaEnvServerBase base ) {
		super( base , "prepare" );
		this.meta = meta;
		this.base = base;
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		APP = properties.getSystemRequiredStringProperty( PROPERTY_APP );
		properties.finishRawProperties();
	}

	public void resolveLinks( ActionBase action ) throws Exception {
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( base.getProperties() ) )
			return;

		properties.loadFromNodeAttributes( node );
		scatterProperties( action );
		properties.loadFromNodeElements( node );
		properties.resolveRawProperties();
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( !super.isLoaded() )
			return;
		
		properties.saveSplit( doc , root );
	}

}
