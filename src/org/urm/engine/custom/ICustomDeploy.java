package org.urm.engine.custom;

import org.urm.action.ActionBase;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.w3c.dom.Node;

public interface ICustomDeploy {

	public void parseDistItem( ActionBase action , CommandCustom custom , MetaDistrBinaryItem item , Node node ) throws Exception;
	
}
