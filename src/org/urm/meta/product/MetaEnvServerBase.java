package org.urm.meta.product;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertyController;
import org.urm.engine.ServerTransaction;
import org.urm.meta.engine.ServerBaseItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaEnvServerBase extends PropertyController {

	protected Meta meta;
	public MetaEnvServer server;

	public String ID;
	public Map<String,MetaEnvServerPrepareApp> prepareMap;
	
	public static String PROPERTY_ID = "id";
	public static String ELEMENT_PREPARE = "prepare";
	
	public MetaEnvServerBase( Meta meta , MetaEnvServer server ) {
		super( server , "base" );
		this.meta = meta;
		this.server = server;
		prepareMap = new HashMap<String,MetaEnvServerPrepareApp>();
	}

	@Override
	public String getName() {
		return( ID );
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		ID = super.getStringPropertyRequired( action , PROPERTY_ID );
		super.finishRawProperties();
	}

	public MetaEnvServerBase copy( ActionBase action , Meta meta , MetaEnvServer server ) throws Exception {
		MetaEnvServerBase r = new MetaEnvServerBase( meta , server );
		r.initCopyStarted( this , server.getProperties() );
		r.scatterProperties( action );
		
		for( MetaEnvServerPrepareApp prepare : prepareMap.values() ) {
			MetaEnvServerPrepareApp rprepare = prepare.copy( action , meta , r );
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

		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		
		super.loadFromNodeElements( action , node , true );
		super.resolveRawProperties();
		
		loadPrepare( action , node );
		super.initFinished();
	}
		
	public void createBase( ActionBase action , ServerBaseItem item ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			return;

		super.setStringProperty( PROPERTY_ID , item.ID );
		super.finishProperties( action );
		super.initFinished();
		
		scatterProperties( action );
	}
	
	private void loadPrepare( ActionBase action , Node node ) throws Exception {
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
		super.saveSplit( doc , root );
		
		for( MetaEnvServerPrepareApp prepare : prepareMap.values() ) {
			Element prepareElement = Common.xmlCreateElement( doc , root , ELEMENT_PREPARE );
			prepare.save( action , doc , prepareElement );
		}
	}

	public void setItem( ServerTransaction transaction , ServerBaseItem item ) throws Exception {
		super.setSystemStringProperty( PROPERTY_ID , item.ID );
		super.updateProperties( transaction );
	}
	
}
