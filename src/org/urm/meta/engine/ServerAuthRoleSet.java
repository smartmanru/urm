package org.urm.meta.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerAuthRoleSet {
	public boolean admin = false;
	public boolean secDev = false;
	public boolean secRel = false;
	public boolean secOpr = false;
	public boolean secInfra = false;

	public ServerAuthRoleSet() {
	}
	
	public ServerAuthRoleSet( ServerAuthRoleSet src ) {
		this.admin = src.admin;
		this.secDev = src.secDev;
		this.secRel = src.secRel;
		this.secOpr = src.secOpr;
		this.secInfra = src.secInfra;
	}

	public boolean isAny() {
		if( admin || secDev || secRel || secOpr || secInfra )
			return( true );
		return( false );
	}
	
	public void clear() {
		admin = false;
		secDev = false;
		secRel = false;
		secOpr = false;
		secInfra = false;
	}
	
	public void add( ServerAuthRoleSet src ) {
		if( src.admin )
			admin = true;
		if( src.secDev )
			secDev = true;
		if( src.secRel )
			secRel = true;
		if( src.secOpr )
			secOpr = true;
		if( src.secInfra )
			secInfra = true;
	}

	public void loadPermissions( Node root ) throws Exception {
		admin = ConfReader.getBooleanAttrValue( root , "admacc" , false );
		secDev = ConfReader.getBooleanAttrValue( root , "devacc" , false );
		secRel = ConfReader.getBooleanAttrValue( root , "relacc" , false );
		secOpr = ConfReader.getBooleanAttrValue( root , "opracc" , false );
		secInfra = ConfReader.getBooleanAttrValue( root , "infacc" , false );
	}

	public void savePermissions( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "admacc" , Common.getBooleanValue( admin ) );
		Common.xmlSetElementAttr( doc , root , "devacc" , Common.getBooleanValue( secDev ) );
		Common.xmlSetElementAttr( doc , root , "relacc" , Common.getBooleanValue( secRel ) );
		Common.xmlSetElementAttr( doc , root , "opracc" , Common.getBooleanValue( secOpr ) );
		Common.xmlSetElementAttr( doc , root , "infacc" , Common.getBooleanValue( secInfra ) );
	}
	
}
