package org.urm.common.custom;

import org.urm.meta.MetaDistrBinaryItem;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Node;

public interface ICustomDeploy {

	public void parseDistItem( ActionBase action , CommandCustom custom , MetaDistrBinaryItem item , Node node ) throws Exception;
	
}
