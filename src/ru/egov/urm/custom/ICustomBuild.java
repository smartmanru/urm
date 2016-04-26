package ru.egov.urm.custom;

import org.w3c.dom.Node;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaSourceProject;

public interface ICustomBuild {

	public void parseProject( ActionBase action , CommandCustom custom , MetaSourceProject project , Node node ) throws Exception;
	
}
