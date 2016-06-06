package org.urm.server.custom;

import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaSourceProject;
import org.w3c.dom.Node;

public interface ICustomBuild {

	public void parseProject( ActionBase action , CommandCustom custom , MetaSourceProject project , Node node ) throws Exception;
	
}
