package ru.egov.urm.storage;

import ru.egov.urm.meta.Metadata;

public class BuildStorage {

	Artefactory artefactory;
	Metadata meta;
	public LocalFolder buildFolder;
	
	public BuildStorage( Artefactory artefactory , LocalFolder buildFolder ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
		
		this.buildFolder = buildFolder;
	}
}
