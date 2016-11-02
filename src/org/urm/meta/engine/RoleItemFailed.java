package org.urm.meta.engine;

import org.urm.meta.product.MetaEnvServerNode;

public class RoleItemFailed {
	public RoleItemFailed( String role , MetaEnvServerNode node ) {
		this.role = role;
		this.node = node;
	}
	
	public String role;
	public MetaEnvServerNode node;
}
