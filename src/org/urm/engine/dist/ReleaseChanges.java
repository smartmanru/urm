package org.urm.engine.dist;

import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseChanges {

	public Meta meta;
	public Release release;

	private Map<String,ReleaseTicketSet> sets;
	
	public ReleaseChanges( Meta meta , Release release ) {
		this.meta = meta; 
		this.release = release;
	}

	public ReleaseChanges copy( ActionBase action , Meta meta , Release release ) throws Exception {
		ReleaseChanges r = new ReleaseChanges( meta , release );
		
		for( ReleaseTicketSet set : sets.values() ) {
			ReleaseTicketSet rset = set.copy( action , meta , r );
			r.addSet( rset );
		}
		
		return( r );
	}

	private void addSet( ReleaseTicketSet set ) {
		sets.put( set.CODE , set );
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		sets.clear();
		
		Node node = ConfReader.xmlGetFirstChild( root , Release.ELEMENT_CHANGES );
		if( node == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( node , Release.ELEMENT_TICKETSET );
		if( items == null )
			return;

		for( Node setNode : items ) {
			ReleaseTicketSet set = new ReleaseTicketSet( meta , this );
			set.load( action , setNode );
			addSet( set );
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Element node = Common.xmlCreateElement( doc , root , Release.ELEMENT_CHANGES );
		
		for( ReleaseTicketSet set : sets.values() ) {
			Element setElement = Common.xmlCreateElement( doc , node , Release.ELEMENT_TICKETSET );
			set.save( action , doc , setElement );
		}
	}

}
