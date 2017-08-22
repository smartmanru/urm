package org.urm.engine.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.Types.VarTICKETTYPE;
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
		sets = new HashMap<String,ReleaseTicketSet>();
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
	
	private void removeSet( ReleaseTicketSet set ) {
		sets.remove( set.CODE );
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

	public String[] getSetCodes() {
		return( Common.getSortedKeys( sets ) );
	}
	
	public ReleaseTicketSet getSet( ActionBase action , String code ) throws Exception {
		ReleaseTicketSet set = sets.get( code );
		if( set == null )
			action.exit2( _Error.UnknownReleaseTicketSet2 , "Unknown set=" + code + " in release=" + release.dist.RELEASEDIR , code , release.dist.RELEASEDIR );
		return( set );
	}

	public ReleaseTicketSet findSet( String code ) {
		return( sets.get( code ) );
	}

	public void createSet( ActionBase action , String code , String name , String comments ) throws Exception {
		if( findSet( code ) != null )
			action.exitUnexpectedState();
		
		ReleaseTicketSet set = new ReleaseTicketSet( meta , this );
		set.create( action , code , name , comments );
		addSet( set );
	}
	
	public void createTicket( ActionBase action , ReleaseTicketSet set , VarTICKETTYPE type , String code , String name , String link , String comments , boolean devdone ) throws Exception {
		set.createTicket( action , type , code , name , link , comments , devdone );
	}
	
	public void modifySet( ActionBase action , ReleaseTicketSet set , String code , String name , String comments ) throws Exception {
		ReleaseTicketSet current = findSet( code );
		if( current != null && current != set )
			action.exitUnexpectedState();

		if( current == null )
			removeSet( set );
		set.modify( action , code , name , comments );
		if( current == null )
			addSet( set );
	}
	
	public void dropSet( ActionBase action , ReleaseTicketSet set , boolean descope ) throws Exception {
		if( descope )
			set.setDescoped( action );
		else
			removeSet( set );
	}

	public void acceptSet( ActionBase action , ReleaseTicketSet set ) throws Exception {
		set.acceptAll( action );
	}

	public boolean isCompleted() {
		for( ReleaseTicketSet set : sets.values() ) {
			if( !set.isCompleted() )
				return( false );
		}
		return( true );
	}
	
}
