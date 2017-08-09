package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.Types;
import org.urm.meta.Types.*;
import org.urm.meta.product.Meta;

public class ReleaseTicketSetTarget {

	public Meta meta;
	public ReleaseTicketSet set;

	public VarTICKETSETTARGETTYPE type;
	public String ITEM;
	
	public ReleaseTicketSetTarget( Meta meta , ReleaseTicketSet set ) {
		this.meta = meta; 
		this.set = set;
	}

	public ReleaseTicketSetTarget copy( ActionBase action , Meta meta , ReleaseTicketSet set ) throws Exception {
		ReleaseTicketSetTarget r = new ReleaseTicketSetTarget( meta , set );
		r.ITEM = ITEM;
		r.type = type;
		return( r );
	}

	public void load( ActionBase action , String value ) throws Exception {
		String[] items = Common.split( value , ":" );
		if( items.length != 2 )
			action.exitUnexpectedState();
		
		type = Types.getTicketSetTargetType( items[0] , true );
		ITEM = items[1];
	}

	public String save( ActionBase action ) throws Exception {
		String value = Common.getEnumLower( type ) + ":" + ITEM;
		return( value );
	}
	
}
