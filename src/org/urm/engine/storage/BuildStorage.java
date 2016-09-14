package org.urm.engine.storage;

import org.urm.engine.meta.Meta;

public class BuildStorage {

	Artefactory artefactory;
	Meta meta;
	public LocalFolder buildFolder;
	
	public BuildStorage( Artefactory artefactory , LocalFolder buildFolder ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
		
		this.buildFolder = buildFolder;
	}
}
