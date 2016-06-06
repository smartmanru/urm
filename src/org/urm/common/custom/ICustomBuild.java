package org.urm.common.custom;

import org.urm.meta.MetaSourceProject;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public interface ICustomBuild {

	public void parseProject( ActionBase action , CommandCustom custom , MetaSourceProject project , Node node ) throws Exception;
	
}
