package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.dist.DistRepository;

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
