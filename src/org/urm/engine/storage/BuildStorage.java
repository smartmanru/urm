package org.urm.engine.storage;

import org.urm.meta.product.Meta;

public class BuildStorage {

	Artefactory artefactory;
	Meta meta;
	public LocalFolder buildFolder;
	
	public BuildStorage( Artefactory artefactory , Meta meta , LocalFolder buildFolder ) {
		this.artefactory = artefactory;
		this.meta = meta;
		
		this.buildFolder = buildFolder;
	}
}
