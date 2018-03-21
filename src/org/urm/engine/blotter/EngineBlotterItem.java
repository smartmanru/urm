package org.urm.engine.blotter;

import org.urm.engine.BlotterService.BlotterType;

public class EngineBlotterItem {

	public EngineBlotterSet blotterSet;
	
	public boolean toberemoved;
	public boolean removed;
	
	public String ID;

	public EngineBlotterItem( EngineBlotterSet blotterSet , String ID ) {
		this.blotterSet = blotterSet;
		this.ID = ID;
		removed = false;
	}
	
	public void setTobeRemoved() {
		toberemoved = true;
	}
	
	public void setRemoved() {
		removed = true;
	}
	
	public boolean isRootItem() {
		return( blotterSet.type == BlotterType.BLOTTER_ROOT );
	}
	
	public boolean isBuildItem() {
		return( blotterSet.type == BlotterType.BLOTTER_BUILD );
	}
	
	public boolean isReleaseItem() {
		return( blotterSet.type == BlotterType.BLOTTER_RELEASE );
	}
	
	public boolean isDeployItem() {
		return( blotterSet.type == BlotterType.BLOTTER_DEPLOY );
	}
	
}
