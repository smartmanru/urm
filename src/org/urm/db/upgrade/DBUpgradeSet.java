package org.urm.db.upgrade;

import org.urm.meta.EngineLoader;

public abstract class DBUpgradeSet {

	public int versionFrom;
	public int versionTo;
	abstract public void upgrade( EngineLoader loader ) throws Exception;
	
	public DBUpgradeSet( int versionFrom , int versionTo ) {
		this.versionFrom = versionFrom;
		this.versionTo = versionTo;
	}
	
}
