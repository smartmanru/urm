package org.urm.meta.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.EngineTransaction;
import org.urm.meta.ProductContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetaProductCoreSettings {

	public Meta meta;
	public MetaProductSettings settings;
	
	public String CONFIG_PRODUCT;
	public String CONFIG_PRODUCTHOME;
	public int CONFIG_LASTPRODTAG;
	public int CONFIG_NEXTPRODTAG;
	public int CONFIG_VERSION_BRANCH_MAJOR;
	public int CONFIG_VERSION_BRANCH_MINOR;
	public int CONFIG_VERSION_BRANCH_NEXTMAJOR;
	public int CONFIG_VERSION_BRANCH_NEXTMINOR;

	public String RELEASELC_MAJOR;
	public String RELEASELC_MINOR;
	public boolean releaseLCUrgentAll;
	public String[] RELEASELC_URGENT_LIST;
	
	public static String PROPERTY_PRODUCT_NAME = "product";
	public static String PROPERTY_PRODUCT_HOME = "product.home";
	public static String PROPERTY_LASTPRODTAG = MetaProductVersion.PROPERTY_PROD_LASTTAG;
	public static String PROPERTY_NEXTPRODTAG = MetaProductVersion.PROPERTY_PROD_NEXTTAG;
	public static String PROPERTY_VERSION_BRANCH_MAJOR = MetaProductVersion.PROPERTY_MAJOR_FIRST;
	public static String PROPERTY_VERSION_BRANCH_MINOR = MetaProductVersion.PROPERTY_MAJOR_LAST;
	public static String PROPERTY_VERSION_BRANCH_NEXTMAJOR = MetaProductVersion.PROPERTY_NEXT_MAJOR_FIRST;
	public static String PROPERTY_VERSION_BRANCH_NEXTMINOR = MetaProductVersion.PROPERTY_NEXT_MAJOR_LAST;
	
	public static String PROPERTY_RELEASELC_MAJOR = "minor";
	public static String PROPERTY_RELEASELC_MINOR = "major";
	public static String PROPERTY_RELEASELC_URGENTANY = "urgentany";
	public static String PROPERTY_RELEASELC_URGENTS = "urgentset";

	public MetaProductCoreSettings( Meta meta , MetaProductSettings settings ) {
		this.meta = meta;
		this.settings = settings;
		releaseLCUrgentAll = false;
	}

	public MetaProductCoreSettings copy( ActionBase action , Meta meta , MetaProductSettings rsettings ) throws Exception {
		MetaProductCoreSettings r = new MetaProductCoreSettings( meta , rsettings );

		// context
		r.CONFIG_PRODUCT = CONFIG_PRODUCT;
		r.CONFIG_PRODUCTHOME = CONFIG_PRODUCTHOME;
		
		r.CONFIG_LASTPRODTAG = CONFIG_LASTPRODTAG;
		r.CONFIG_NEXTPRODTAG = CONFIG_NEXTPRODTAG;
		r.CONFIG_VERSION_BRANCH_MAJOR = CONFIG_VERSION_BRANCH_MAJOR;
		r.CONFIG_VERSION_BRANCH_MINOR = CONFIG_VERSION_BRANCH_MINOR;
		r.CONFIG_VERSION_BRANCH_NEXTMAJOR = CONFIG_VERSION_BRANCH_NEXTMAJOR;
		r.CONFIG_VERSION_BRANCH_NEXTMINOR = CONFIG_VERSION_BRANCH_NEXTMINOR;

		// stored
		r.RELEASELC_MAJOR = RELEASELC_MAJOR;
		r.RELEASELC_MINOR = RELEASELC_MINOR;
		r.releaseLCUrgentAll = releaseLCUrgentAll;
		r.RELEASELC_URGENT_LIST = RELEASELC_URGENT_LIST.clone();
		
		return( r );
	}

	public void load( ActionBase action , ProductContext productContext , Node root ) throws Exception {
		setContextProperties( action , productContext );
		
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
		Element core = Common.xmlCreateElement( doc , root , "core" );
		Common.xmlCreatePropertyElement( doc , core , PROPERTY_RELEASELC_MAJOR , RELEASELC_MAJOR );
		Common.xmlCreatePropertyElement( doc , core , PROPERTY_RELEASELC_MINOR , RELEASELC_MINOR );
		Common.xmlCreateBooleanPropertyElement( doc , core , PROPERTY_RELEASELC_URGENTANY , releaseLCUrgentAll );
		Common.xmlCreatePropertyElement( doc , core , PROPERTY_RELEASELC_URGENTS , Common.getList( RELEASELC_URGENT_LIST ) );
	}
	
	public void create( ActionBase action , ProductContext productContext ) throws Exception {
		setContextProperties( action , productContext );
		
		RELEASELC_MAJOR = "";
		RELEASELC_MINOR = "";
		RELEASELC_URGENT_LIST = new String[0];
	}
	
	public void setContextProperties( ActionBase action , ProductContext productContext ) throws Exception {
		CONFIG_PRODUCT = productContext.CONFIG_PRODUCT;
		CONFIG_PRODUCTHOME = productContext.CONFIG_PRODUCTHOME;
		
		CONFIG_LASTPRODTAG = productContext.CONFIG_LASTPRODTAG;
		CONFIG_NEXTPRODTAG = productContext.CONFIG_NEXTPRODTAG;
		CONFIG_VERSION_BRANCH_MAJOR = productContext.CONFIG_VERSION_BRANCH_MAJOR;
		CONFIG_VERSION_BRANCH_MINOR = productContext.CONFIG_VERSION_BRANCH_MINOR;
		CONFIG_VERSION_BRANCH_NEXTMAJOR = productContext.CONFIG_VERSION_BRANCH_NEXTMAJOR;
		CONFIG_VERSION_BRANCH_NEXTMINOR = productContext.CONFIG_VERSION_BRANCH_NEXTMINOR;
		
		settings.setManualStringProperty( PROPERTY_PRODUCT_NAME , CONFIG_PRODUCT );
		settings.setManualPathProperty( PROPERTY_PRODUCT_HOME , CONFIG_PRODUCTHOME , action.shell );
		
		settings.setManualNumberProperty( MetaProductVersion.PROPERTY_MAJOR_FIRST , CONFIG_VERSION_BRANCH_MAJOR );
		settings.setManualNumberProperty( MetaProductVersion.PROPERTY_MAJOR_LAST , CONFIG_VERSION_BRANCH_MINOR );
		settings.setManualNumberProperty( MetaProductVersion.PROPERTY_NEXT_MAJOR_FIRST , CONFIG_VERSION_BRANCH_NEXTMAJOR );
		settings.setManualNumberProperty( MetaProductVersion.PROPERTY_NEXT_MAJOR_LAST , CONFIG_VERSION_BRANCH_NEXTMINOR );
		settings.setManualNumberProperty( MetaProductVersion.PROPERTY_PROD_LASTTAG , CONFIG_LASTPRODTAG );
		settings.setManualNumberProperty( MetaProductVersion.PROPERTY_PROD_NEXTTAG , CONFIG_NEXTPRODTAG );
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
