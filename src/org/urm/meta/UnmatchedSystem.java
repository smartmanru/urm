package org.urm.meta;

import org.urm.meta.engine.AppSystem;

public class UnmatchedSystem {

	public int ID;
	public String NAME;
	public int SV;
	
	public UnmatchedSystem( AppSystem system ) {
		this.ID = system.ID;
		this.NAME = system.NAME;
		this.SV = system.SV;
	}
	
}
