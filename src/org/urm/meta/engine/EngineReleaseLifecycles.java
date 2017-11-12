package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBEnumTypes.*;
import org.urm.engine.EngineTransaction;
import org.urm.engine.blotter.EngineBlotterSet;
import org.urm.engine.blotter.EngineBlotter.BlotterType;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineReleaseLifecycles extends EngineObject {

	public EngineLoader loader;
	
	private Map<String,ReleaseLifecycle> lcMap;
	
	public EngineReleaseLifecycles( EngineLoader loader ) {
		super( null );
		this.loader = loader;
		lcMap = new HashMap<String,ReleaseLifecycle>(); 
	}
	
	@Override
	public String getName() {
		return( "server-lifecycles" );
	}
	
	public void load( String lcFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , lcFile );
		Node root = doc.getDocumentElement();
		
		Node[] list = ConfReader.xmlGetChildren( root , "lifecycle" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ReleaseLifecycle lc = new ReleaseLifecycle( this );
			lc.load( node );
			addLifecycle( lc );
		}
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "lifecycles" );
		Element root = doc.getDocumentElement();
		
		for( String id : Common.getSortedKeys( lcMap ) ) {
			ReleaseLifecycle lc = lcMap.get( id );
			Element node = Common.xmlCreateElement( doc , root , "lifecycle" );
			lc.save( doc , node );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addLifecycle( ReleaseLifecycle lc ) {
		lcMap.put( lc.ID , lc );
	}

	public ReleaseLifecycle findLifecycle( String id ) {
		return( lcMap.get( id ) );
	}

	public ReleaseLifecycle getLifecycle( ActionBase action , String id ) throws Exception {
		ReleaseLifecycle lc = findLifecycle( id );
		if( lc == null )
			action.exit1( _Error.UnknownLifecycle1 , "unknown lifecycle id=" + id , id );
		return( lc );
	}

	public String[] getLifecycles() {
		return( Common.getSortedKeys( lcMap ) );
	}

	public String[] getLifecycles( DBEnumLifecycleType type , boolean enabledOnly ) {
		List<String> list = new LinkedList<String>();
		for( String lcName : Common.getSortedKeys( lcMap ) ) {
			ReleaseLifecycle lc = lcMap.get( lcName );
			if( lc.lcType == type ) {
				if( enabledOnly == false || lc.enabled )
					list.add( lcName );
			}
		}
		return( list.toArray( new String[0] ) );
	}

	public ReleaseLifecycle createLifecycle( EngineTransaction transaction , ReleaseLifecycle lcNew ) throws Exception {
		if( lcMap.get( lcNew.ID ) != null )
			transaction.exit1( _Error.LifecycleAlreadyExists1 , "lifecycle already exists name=" + lcNew.ID , lcNew.ID );
			
		ReleaseLifecycle lc = new ReleaseLifecycle( this );
		lc.setLifecycleData( transaction ,  lcNew );
		lcMap.put( lc.ID , lc );
		return( lc );
	}
	
	public void deleteLifecycle( EngineTransaction transaction , ReleaseLifecycle lc ) throws Exception {
		if( lcMap.get( lc.ID ) == null )
			transaction.exit1( _Error.UnknownLifecycle1 , "unknown lifecycle id=" + lc.ID , lc.ID );
			
		lcMap.remove( lc.ID );
	}

	public ReleaseLifecycle copyLifecycle( EngineTransaction transaction , ReleaseLifecycle lc , String name , String desc ) throws Exception {
		ReleaseLifecycle lcNew = lc.copy( this );
		lcNew.setLifecycleName( transaction , name , desc );
		addLifecycle( lcNew );
		return( lcNew );
	}

	public boolean isUsed( ReleaseLifecycle lc ) {
		EngineBlotterSet blotter = loader.engine.blotter.getBlotterSet( BlotterType.BLOTTER_RELEASE );
		if( blotter.checkLifecycleUsed( lc.ID ) )
			return( true );
		return( false );
	}
	
}
