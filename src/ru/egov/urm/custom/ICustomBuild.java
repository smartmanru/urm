package ru.egov.urm.custom;

import org.w3c.dom.Node;

import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.run.ActionBase;

public interface ICustomBuild {

	public void parseProject( ActionBase action , MetaSourceProject project , Node node ) throws Exception;
	
}
