package org.urm.engine.dist;

import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.release.Release;

public class ReleaseBuildScopeProjectItem {

	public Release release;
	public MetaSourceProjectItem item;
	
	public ReleaseBuildScopeProjectItem( Release release , MetaSourceProjectItem item ) {
		this.release = release;
		this.item = item;
	}
	
}
