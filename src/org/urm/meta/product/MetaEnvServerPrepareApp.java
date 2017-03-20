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
	public String getName() {
		return( APP );
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		APP = super.getStringPropertyRequired( action , PROPERTY_APP );
		super.finishRawProperties();
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

		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		super.loadFromNodeElements( action , node , true );
		super.resolveRawProperties();
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveSplit( doc , root );
	}

}
