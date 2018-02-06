package org.urm.db.env;

import org.urm.action.ActionBase;
import org.urm.meta.Types;
import org.urm.meta.product._Error;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaEnvServerDeployment {

	public void refreshProperties() throws Exception {
		String value = super.getStringProperty( action , PROPERTY_DEPLOYMODE );
		if( value.isEmpty() )
			value = "cold";
		deployMode = Types.getDeployMode( value , false );
		DEPLOYPATH = super.getStringProperty( action , PROPERTY_DEPLOYPATH );
		DBNAME = super.getStringProperty( action , PROPERTY_DBNAME );
		DBUSER = super.getStringProperty( action , PROPERTY_DBUSER );
		
		value = super.getStringProperty( action , PROPERTY_NODETYPE );
		nodeType = Types.getNodeType( value , VarNODETYPE.SELF );
		
		COMP = super.getStringProperty( action , PROPERTY_COMPONENT );
		if( !COMP.isEmpty() ) {
			itemType = VarDEPLOYITEMTYPE.COMP;
			return;
		}
		
		DISTITEM = super.getStringProperty( action , PROPERTY_DISTITEM );
		if( !DISTITEM.isEmpty() ) {
			itemType = VarDEPLOYITEMTYPE.BINARY;
			return;
		}
		
		CONFITEM = super.getStringProperty( action , PROPERTY_CONFITEM );
		if( !CONFITEM.isEmpty() ) {
			itemType = VarDEPLOYITEMTYPE.CONF;
			return;
		}
		
		SCHEMA = super.getStringProperty( action , PROPERTY_SCHEMA );
		if( !SCHEMA.isEmpty() ) {
			itemType = VarDEPLOYITEMTYPE.SCHEMA;
			
			if( DBNAME.isEmpty() )
				DBNAME = SCHEMA;
			if( DBUSER.isEmpty() )
				DBUSER = DBNAME;
			return;
		}
		
		itemType = VarDEPLOYITEMTYPE.UNKNOWN;
		action.exit1( _Error.UnexpectedDeploymentType1 , "unexpected deployment type found, server=" + server.NAME , server.NAME );
	}
	
	public void load( ActionBase action , Node node ) throws Exception {
		if( !super.initCreateStarted( server.getProperties() ) )
			return;

		super.loadFromNodeAttributes( action , node , false );
		scatterProperties( action );
		
		super.initFinished();
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		super.saveSplit( doc , root );
	}
	
}
