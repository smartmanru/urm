package org.urm.server.storage;

import org.urm.server.meta.Meta;
import org.urm.server.shell.Account;

public class GitMirrorStorage {

	Artefactory artefactory;
	Meta meta;
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
