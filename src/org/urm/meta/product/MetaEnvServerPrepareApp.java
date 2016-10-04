package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.PropertyController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServerPrepareApp extends PropertyController {

	protected Meta meta;
	public MetaEnvServerBase base;

	public String APP;
	
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

	public MetaEnvServerPrepareApp copy( ActionBase action , Meta meta , MetaEnvServerBase base ) throws Exception {
		MetaEnvServerPrepareApp r = new MetaEnvServerPrepareApp( meta , base );
		r.initCopyStarted( this , base.getProperties() );
		r.scatterProperties( action );
		r.resolveLinks( action );
		r.initFinished();
		return( r );
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
