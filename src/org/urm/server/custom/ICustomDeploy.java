package org.urm.server.custom;

import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaDistrBinaryItem;
import org.w3c.dom.Node;

public interface ICustomDeploy {

	public void parseDistItem( ActionBase action , CommandCustom custom , MetaDistrBinaryItem item , Node node ) throws Exception;
	
}
