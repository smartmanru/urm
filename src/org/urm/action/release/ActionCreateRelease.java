package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.meta.product.Meta;

public class ActionCreateRelease extends ActionBase {

	Meta meta;
	public Dist release;
	String RELEASELABEL;
	
	public ActionCreateRelease( ActionBase action , Meta meta , String stream , String RELEASELABEL ) {
		super( action , stream );
		this.meta = meta;
		this.RELEASELABEL = RELEASELABEL;
	}

	@Override protected boolean executeSimple() throws Exception {
		release = artefactory.createDist( this , meta , RELEASELABEL );
		return( true );
	}
	
}
