package org.urm.db;

import org.urm.engine.properties.ObjectProperties;
import org.w3c.dom.Node;

public abstract class DBSettings {

	public static void loaddb( DBConnection c , int objectId , ObjectProperties properties ) throws Exception {
	}
	
	public static void loadxml( Node root , ObjectProperties properties ) throws Exception {
	}
	
}
