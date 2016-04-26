package ru.egov.urm.custom;

import org.w3c.dom.Node;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDistrBinaryItem;

public interface ICustomDeploy {

	public void parseDistItem( ActionBase action , CommandCustom custom , MetaDistrBinaryItem item , Node node ) throws Exception;
	
}
