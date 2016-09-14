package org.urm.engine.custom;

import org.urm.engine.action.ActionBase;
import org.urm.engine.meta.MetaSourceProject;
import org.w3c.dom.Node;

public interface ICustomBuild {

	public void parseProject( ActionBase action , CommandCustom custom , MetaSourceProject project , Node node ) throws Exception;
	
}
