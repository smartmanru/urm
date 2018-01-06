package org.urm.engine.status;

import org.urm.engine.status.ScopeState.FACTVALUE;

public class FactValue {
	
	public FactValue( FACTVALUE type , String data ) {
		this.type = type;
		this.data = data;
	};
	
	public FACTVALUE type;
	public String data;

}
