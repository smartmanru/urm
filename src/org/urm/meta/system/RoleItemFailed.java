package org.urm.meta.system;

import org.urm.meta.env.MetaEnvServerNode;

public class RoleItemFailed {
	public RoleItemFailed( String role , MetaEnvServerNode node ) {
		this.role = role;
		this.node = node;
	}
	
	public String role;
	public MetaEnvServerNode node;
}
