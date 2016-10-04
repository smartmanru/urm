package org.urm.engine.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.common.PropertySet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServerBase extends PropertyController {

	protected Meta meta;
	public MetaEnvServer server;

	public String ID;
	public Map<String,MetaEnvServerPrepareApp> prepareMap;
	public PropertySet properties;
	
	public static String PROPERTY_ID = "id";

	public static String ELEMENT_PREPARE = "prepare";
	
	public MetaEnvServerBase( Meta meta , MetaEnvServer server ) {
		super( server , "base" );
		this.meta = meta;
		this.server = server;
	}

	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		ID = properties.getSystemRequiredStringProperty( PROPERTY_ID );
		properties.finishRawProperties();
	}

	public MetaEnvServerBase copy( ActionBase action , Meta meta , MetaEnvServer server ) throws Exception {
		MetaEnvServerBase r = new MetaEnvServerBase( meta , server );
		r.initCopyStarted( this , server.getProperties() );
		r.scatterProperties( action );
		
		for( MetaEnvServerPrepareApp prepare : prepareMap.values() ) {
			MetaEnvServerPrepareApp rprepare = prepare.copy( action , meta , this );
			r.addPrepare( rprepare );
		}
		
		r.resolveLinks( action );
		r.initFinished();
		return( r );
	}
	
	public void resolveLinks( ActionBase action ) throws Exception {
		for( MetaEnvServerPrepareApp prepare : prepareMap.values() )
			prepare.resolveLinks( action );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			return;

		properties.loadFromNodeAttributes( node );
		scatterProperties( action );
		
		properties.loadFromNodeElements( node );
		properties.resolveRawProperties();
		
		loadPrepare( action , node );
		super.initFinished();
	}
		
	private void loadPrepare( ActionBase action , Node node ) throws Exception {
		prepareMap = new HashMap<String,MetaEnvServerPrepareApp>();
		
		Node[] items = ConfReader.xmlGetChildren( node , ELEMENT_PREPARE );
		if( items == null )
			return;
		
		for( Node snnode : items ) {
			MetaEnvServerPrepareApp sn = new MetaEnvServerPrepareApp( meta , this );
			sn.load( action , snnode );
			addPrepare( sn );
		}
	}
	
	private void addPrepare( MetaEnvServerPrepareApp sn ) {
		prepareMap.put( sn.APP , sn );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		if( !super.isLoaded() )
			return;
		
		properties.saveSplit( doc , root );
		
		for( MetaEnvServerPrepareApp prepare : prepareMap.values() ) {
			Element prepareElement = Common.xmlCreateElement( doc , root , ELEMENT_PREPARE );
			prepare.save( action , doc , prepareElement );
		}
	}
	
}
