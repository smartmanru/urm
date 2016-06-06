package org.urm.server.storage;

import org.urm.meta.Metadata;
import org.urm.server.shell.Account;

public class GitMirrorStorage {

	Artefactory artefactory;
	Metadata meta;
	public Account account;
	public Folder mirrorFolder;
	public boolean winBuild;
	
	public GitMirrorStorage( Artefactory artefactory , Account account , Folder mirrorFolder , boolean winBuild ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
		this.account = account;
		this.mirrorFolder = mirrorFolder;
		this.winBuild = winBuild;
	}

}
