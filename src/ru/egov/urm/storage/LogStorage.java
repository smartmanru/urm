package ru.egov.urm.storage;

import ru.egov.urm.meta.Metadata;

public class LogStorage {

	Artefactory artefactory;
	Metadata meta;
	public LocalFolder logFolder;
	
	public LogStorage( Artefactory artefactory , LocalFolder logFolder ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
		this.logFolder = logFolder;
	}
	
}
