package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.meta.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductPolicy {

	public static String PROPERTY_RELEASELC_MAJOR = "minor";
	public static String PROPERTY_RELEASELC_MINOR = "major";
	public static String PROPERTY_RELEASELC_URGENTANY = "urgentany";
	public static String PROPERTY_RELEASELC_URGENTS = "urgentset";

	public Meta meta;
	
	public String RELEASELC_MAJOR;
	public String RELEASELC_MINOR;
	public boolean releaseLCUrgentAll;
	public String[] RELEASELC_URGENT_LIST;
	
	public MetaProductPolicy( ProductMeta storage , Meta meta ) {
		releaseLCUrgentAll = false;
	}

	public MetaProductPolicy copy( ActionBase action , Meta rmeta ) throws Exception {
		MetaProductPolicy r = new MetaProductPolicy( rmeta.getStorage() , rmeta );
		
		// stored
		r.RELEASELC_MAJOR = RELEASELC_MAJOR;
		r.RELEASELC_MINOR = RELEASELC_MINOR;
		r.releaseLCUrgentAll = releaseLCUrgentAll;
		r.RELEASELC_URGENT_LIST = RELEASELC_URGENT_LIST.clone();
		
		return( r );
	}

	public void createPolicy( TransactionBase transaction ) throws Exception {
		RELEASELC_MAJOR = "";
		RELEASELC_MINOR = "";
		RELEASELC_URGENT_LIST = new String[0];
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		Node core = ConfReader.xmlGetFirstChild( root , "core" );
		if( core == null ) {
			RELEASELC_MAJOR = "";
			RELEASELC_MINOR = "";
			releaseLCUrgentAll = false;
			RELEASELC_URGENT_LIST = new String[0];
			return;
		}
		
		RELEASELC_MAJOR = ConfReader.getPropertyValue( core , PROPERTY_RELEASELC_MAJOR , "" );
		RELEASELC_MINOR = ConfReader.getPropertyValue( core , PROPERTY_RELEASELC_MINOR , "" );
		releaseLCUrgentAll = ConfReader.getBooleanPropertyValue( core , PROPERTY_RELEASELC_URGENTANY , false );
		String URGENTS = "";
		if( !releaseLCUrgentAll )
			URGENTS = ConfReader.getPropertyValue( core , PROPERTY_RELEASELC_URGENTS , "" );
		RELEASELC_URGENT_LIST = Common.splitSpaced( URGENTS );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_RELEASELC_MAJOR , RELEASELC_MAJOR );
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_RELEASELC_MINOR , RELEASELC_MINOR );
		Common.xmlCreateBooleanPropertyElement( doc , root , PROPERTY_RELEASELC_URGENTANY , releaseLCUrgentAll );
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_RELEASELC_URGENTS , Common.getList( RELEASELC_URGENT_LIST ) );
	}
	
	public void setLifecycles( EngineTransaction transaction , String major , String minor , boolean urgentsAll , String[] urgents ) throws Exception {
		RELEASELC_MAJOR = major;
		RELEASELC_MINOR = minor;
		releaseLCUrgentAll = urgentsAll;
		if( !urgentsAll )
			RELEASELC_URGENT_LIST = urgents.clone();
		else
			RELEASELC_URGENT_LIST = new String[0];
	}
	
}
