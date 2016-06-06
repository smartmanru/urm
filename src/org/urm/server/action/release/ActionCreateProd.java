package org.urm.server.action.release;

import org.urm.dist.DistRepository;
import org.urm.server.action.ActionBase;

public class ActionCreateProd extends ActionBase { 

	String RELEASEVER;
	
	public ActionCreateProd( ActionBase action , String stream , String RELEASEVER ) {
		super( action , stream );
		this.RELEASEVER = RELEASEVER;
	}

	@Override protected boolean executeSimple() throws Exception {
		DistRepository repo = artefactory.getDistRepository( this );
		repo.createProd( this , RELEASEVER );
		return( true );
	}
	
}
