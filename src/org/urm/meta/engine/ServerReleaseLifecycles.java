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
import org.urm.engine.ServerTransaction;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerObject;
import org.urm.meta.Types.VarLCTYPE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerReleaseLifecycles extends ServerObject {

	public ServerLoader loader;
	
	private Map<String,ServerReleaseLifecycle> lcMap;
	
	public ServerReleaseLifecycles( ServerLoader loader ) {
		super( null );
		this.loader = loader;
		lcMap = new HashMap<String,ServerReleaseLifecycle>(); 
	}
	
	public void load( String lcFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , lcFile );
		Node root = doc.getDocumentElement();
		
		Node[] list = ConfReader.xmlGetChildren( root , "lifecycle" );
		if( list == null )
			return;
		
		for( Node node : list ) {
			ServerReleaseLifecycle lc = new ServerReleaseLifecycle( this );
			lc.load( node );
			addLifecycle( lc );
		}
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "lifecycles" );
		Element root = doc.getDocumentElement();
		
		for( String id : Common.getSortedKeys( lcMap ) ) {
			ServerReleaseLifecycle lc = lcMap.get( id );
			Element node = Common.xmlCreateElement( doc , root , "lifecycle" );
			lc.save( doc , node );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addLifecycle( ServerReleaseLifecycle lc ) {
		lcMap.put( lc.ID , lc );
	}

	public ServerReleaseLifecycle findLifecycle( String id ) {
		return( lcMap.get( id ) );
	}

	public ServerReleaseLifecycle getLifecycle( ActionBase action , String id ) throws Exception {
		ServerReleaseLifecycle lc = findLifecycle( id );
		if( lc == null )
			action.exit1( _Error.UnknownLifecycle1 , "unknown lifecycle id=" + id , id );
		return( lc );
	}

	public String[] getLifecycles() {
		return( Common.getSortedKeys( lcMap ) );
	}

	public String[] getLifecycles( VarLCTYPE type , boolean enabledOnly ) {
		List<String> list = new LinkedList<String>();
		for( String lcName : Common.getSortedKeys( lcMap ) ) {
			ServerReleaseLifecycle lc = lcMap.get( lcName );
			if( lc.lcType == type ) {
				if( enabledOnly == false || lc.enabled )
					list.add( lcName );
			}
		}
		return( list.toArray( new String[0] ) );
	}

	public ServerReleaseLifecycle createLifecycle( ServerTransaction transaction , ServerReleaseLifecycle lcNew ) throws Exception {
		if( lcMap.get( lcNew.ID ) != null )
			transaction.exit1( _Error.LifecycleAlreadyExists1 , "lifecycle already exists name=" + lcNew.ID , lcNew.ID );
			
		ServerReleaseLifecycle lc = new ServerReleaseLifecycle( this );
		lc.setLifecycleData( transaction ,  lcNew );
		lcMap.put( lc.ID , lc );
		return( lc );
	}
	
	public void deleteLifecycle( ServerTransaction transaction , ServerReleaseLifecycle lc ) throws Exception {
		if( lcMap.get( lc.ID ) == null )
			transaction.exit1( _Error.UnknownLifecycle1 , "unknown lifecycle id=" + lc.ID , lc.ID );
			
		lcMap.remove( lc.ID );
	}
	
}
