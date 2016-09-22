package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.meta.Meta;

public class ActionCreateProd extends ActionBase { 

	Meta meta;
	String RELEASEVER;
	
	public ActionCreateProd( ActionBase action , Meta meta , String stream , String RELEASEVER ) {
		super( action , stream );
		this.meta = meta;
		this.RELEASEVER = RELEASEVER;
	}

	@Override protected boolean executeSimple() throws Exception {
		DistRepository repo = artefactory.getDistRepository( this , meta );
		repo.createProd( this , RELEASEVER );
		return( true );
	}
	
}
