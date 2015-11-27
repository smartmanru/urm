package ru.egov.urm.storage;

import ru.egov.urm.meta.Metadata;

public class GitMirrorStorage {

	Artefactory artefactory;
	Metadata meta;
	public LocalFolder mirrorFolder;
	
	public GitMirrorStorage( Artefactory artefactory , LocalFolder mirrorFolder ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
		this.mirrorFolder = mirrorFolder;
	}

}
