package org.urm.custom;

import org.urm.action.ActionBase;
import org.urm.meta.MetaSourceProject;
import org.w3c.dom.Node;

public interface ICustomBuild {

	public void parseProject( ActionBase action , CommandCustom custom , MetaSourceProject project , Node node ) throws Exception;
	
}
