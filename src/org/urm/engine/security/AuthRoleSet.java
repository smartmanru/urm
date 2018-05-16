package org.urm.engine.security;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AuthRoleSet {
	public boolean secDev = false;
	public boolean secRel = false;
	public boolean secTest = false;
	public boolean secOpr = false;
	public boolean secInfra = false;

	public AuthRoleSet() {
	}
	
	public AuthRoleSet( AuthRoleSet src ) {
		set( src );
	}
	
	public void set( AuthRoleSet src ) {
		this.secDev = src.secDev;
		this.secRel = src.secRel;
		this.secTest = src.secTest;
		this.secOpr = src.secOpr;
		this.secInfra = src.secInfra;
	}

	public boolean isAny() {
		if( secDev || secRel || secTest || secOpr || secInfra )
			return( true );
		return( false );
	}
	
	public boolean isAll() {
		if( secDev && secRel && secTest && secOpr && secInfra )
			return( true );
		return( false );
	}
	
	public void clear() {
		secDev = false;
		secRel = false;
		secTest = false;
		secOpr = false;
		secInfra = false;
	}
	
	public void add( AuthRoleSet src ) {
		if( src.secDev )
			secDev = true;
		if( src.secRel )
			secRel = true;
		if( src.secTest )
			secTest = true;
		if( src.secOpr )
			secOpr = true;
		if( src.secInfra )
			secInfra = true;
	}

	public void loadPermissions( Node root ) throws Exception {
		secDev = ConfReader.getBooleanAttrValue( root , "devacc" , false );
		secRel = ConfReader.getBooleanAttrValue( root , "relacc" , false );
		secTest = ConfReader.getBooleanAttrValue( root , "testacc" , false );
		secOpr = ConfReader.getBooleanAttrValue( root , "opracc" , false );
		secInfra = ConfReader.getBooleanAttrValue( root , "infacc" , false );
	}

	public void savePermissions( Document doc , Element root ) throws Exception {
		Common.xmlSetElementAttr( doc , root , "devacc" , Common.getBooleanValue( secDev ) );
		Common.xmlSetElementAttr( doc , root , "relacc" , Common.getBooleanValue( secRel ) );
		Common.xmlSetElementAttr( doc , root , "testacc" , Common.getBooleanValue( secTest ) );
		Common.xmlSetElementAttr( doc , root , "opracc" , Common.getBooleanValue( secOpr ) );
		Common.xmlSetElementAttr( doc , root , "infacc" , Common.getBooleanValue( secInfra ) );
	}
	
}
