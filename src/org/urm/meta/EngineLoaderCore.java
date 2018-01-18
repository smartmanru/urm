package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.engine.DBEngineAuth;
import org.urm.db.engine.DBEngineBase;
import org.urm.db.engine.DBEngineBuilders;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.engine.DBEngineInfrastructure;
import org.urm.db.engine.DBEngineLifecycles;
import org.urm.db.engine.DBEngineMirrors;
import org.urm.db.engine.DBEngineMonitoring;
import org.urm.db.engine.DBEngineResources;
import org.urm.db.engine.DBEngineSettings;
import org.urm.engine.Engine;
import org.urm.engine.storage.LocalFolder;
import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineBuilders;
import org.urm.meta.engine.EngineContext;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineLifecycles;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.engine._Error;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineLoaderCore {

	public static String ELEMENT_RESOURCES = "resources";
	public static String ELEMENT_MIRRORS = "mirror";
	public static String ELEMENT_BUILDERS = "build";
	public static String ELEMENT_DIRECTORY = "directory";
	
	private EngineLoader loader;
	private EngineData data;
	public RunContext execrc;
	public Engine engine;
	
	private EngineSettings settingsNew;
	private EngineResources resourcesNew;
	private EngineBuilders buildersNew;
	private EngineDirectory directoryNew;
	private EngineMirrors mirrorsNew;
	private EngineBase baseNew;
	private EngineInfrastructure infraNew;
	private EngineLifecycles lifecyclesNew;
	private EngineMonitoring monitoringNew;
	
	public EngineLoaderCore( EngineLoader loader , EngineData data ) {
		this.loader = loader;
		this.data = data;
		this.execrc = loader.execrc;
		this.engine = loader.engine;
	}
	
	public EngineSettings getSettings() {
		if( settingsNew != null )
			return( settingsNew );
		return( data.getEngineSettings() );
	}

	public EngineResources getResources() {
		if( resourcesNew != null )
			return( resourcesNew );
		return( data.getResources() );
	}

	public EngineBuilders getBuilders() {
		if( buildersNew != null )
			return( buildersNew );
		return( data.getBuilders() );
	}

	public EngineDirectory getDirectory() {
		if( directoryNew != null )
			return( directoryNew );
		return( data.getDirectory() );
	}

	public EngineMirrors getMirrors() {
		if( mirrorsNew != null )
			return( mirrorsNew );
		return( data.getMirrors() );
	}
	
	public EngineInfrastructure getInfrastructure() {
		if( infraNew != null )
			return( infraNew );
		return( data.getInfrastructure() );
	}

	public EngineMonitoring getMonitoring() {
		if( infraNew != null )
			return( monitoringNew );
		return( data.getMonitoring() );
	}

	public void exportEngine() throws Exception {
		exportCore( true );
		exportAuth( engine.getAuth() );
	}
	
	private void exportCore( boolean includingSystems ) throws Exception {
		trace( "export engine core data ..." );
		exportxmlSettings();
		exportxmlBase();
		exportxmlReleaseLifecycles();
		exportxmlMonitoring();
		exportxmlRegistry();
		exportxmlInfrastructure();
	}

	private void exportAuth( EngineAuth auth ) throws Exception {
		trace( "export engine auth data ..." );
		exportxmlAuth( auth );
	}
	
	public void setData() {
		if( settingsNew != null ) {
			data.setSettings( settingsNew );
			settingsNew = null;
		}
		
		if( resourcesNew != null ) {
			data.setResources( resourcesNew );
			resourcesNew = null;
		}
		
		if( buildersNew != null ) {
			data.setBuilders( buildersNew );
			buildersNew = null;
		}
		
		if( directoryNew != null ) {
			data.setDirectory( directoryNew );
			directoryNew = null;
		}
		
		if( mirrorsNew != null ) {
			data.setMirrors( mirrorsNew );
			mirrorsNew = null;
		}
		
		if( baseNew != null ) {
			data.setBase( baseNew );
			baseNew = null;
		}
		
		if( infraNew != null ) {
			data.setInfrastructure( infraNew );
			infraNew = null;
		}
		
		if( lifecyclesNew != null ) {
			data.setLifecycles( lifecyclesNew );
			lifecyclesNew = null;
		}
		
		if( monitoringNew != null ) {
			data.setMonitoring( monitoringNew );
			monitoringNew = null;
		}
	}
	
	private void importxmlEngineSettings() throws Exception {
		trace( "import engine settings data ..." );
		String propertyFile = getEngineSettingsFile();
		Document doc = ConfReader.readXmlFile( execrc , propertyFile );
		if( doc == null )
			Common.exit1( _Error.UnableReadEnginePropertyFile1 , "unable to read engine property file " + propertyFile , propertyFile );
		
		Node root = doc.getDocumentElement();
		
		settingsNew = new EngineSettings( engine );
		DBEngineSettings.importxml( loader , settingsNew , root );
	}

	private void loaddbEngineSettings() throws Exception {
		trace( "load engine settings data ..." );
		settingsNew = new EngineSettings( engine );
		DBEngineSettings.loaddb( loader , settingsNew );
	}

	private void importxmlBase() throws Exception {
		trace( "import engine base data ..." );
		String baseFile = getBaseFile();
		Document doc = ConfReader.readXmlFile( execrc , baseFile );
		Node root = doc.getDocumentElement();
		
		baseNew = new EngineBase( engine );
		DBEngineBase.importxml( loader , baseNew , root );
	}

	private void loaddbBase() throws Exception {
		trace( "load engine base data ..." );
		baseNew = new EngineBase( engine );
		DBEngineBase.loaddb( loader , baseNew );
	}

	private void importxmlInfrastructure() throws Exception {
		trace( "import engine infrastructure data ..." );
		String infraFile = getInfrastructureFile();
		Document doc = ConfReader.readXmlFile( execrc , infraFile );
		Node root = doc.getDocumentElement();
		
		infraNew = new EngineInfrastructure( engine );
		DBEngineInfrastructure.importxml( loader , infraNew , root );
	}

	private void loaddbInfrastructure() throws Exception {
		trace( "load engine infrastructure data ..." );
		infraNew = new EngineInfrastructure( engine );
		DBEngineInfrastructure.loaddb( loader , infraNew );
	}

	private void importxmlReleaseLifecycles() throws Exception {
		String lcFile = getReleaseLifecyclesFile();
		
		trace( "import engine lifecycles data from " + lcFile + " ..." );
		Document doc = ConfReader.readXmlFile( execrc , lcFile );
		Node root = doc.getDocumentElement();
		
		lifecyclesNew = new EngineLifecycles( engine );
		DBEngineLifecycles.importxml( loader , lifecyclesNew , root );
	}

	private void loaddbReleaseLifecycles() throws Exception {
		trace( "load release lifecycles data ..." );
		lifecyclesNew = new EngineLifecycles( engine );
		DBEngineLifecycles.loaddb( loader , lifecyclesNew );
	}

	private void importxmlMonitoring() throws Exception {
		trace( "import engine infrastructure data ..." );
		String monFile = getMonitoringFile();
		Document doc = ConfReader.readXmlFile( execrc , monFile );
		Node root = doc.getDocumentElement();
		
		monitoringNew = new EngineMonitoring( engine );
		DBEngineMonitoring.importxml( loader , monitoringNew , root );
	}

	private void loaddbMonitoring() throws Exception {
		trace( "load engine monitoring data ..." );
		monitoringNew = new EngineMonitoring( engine );
		DBEngineMonitoring.loaddb( loader , monitoringNew );
	}

	private void importxmlRegistry() throws Exception {
		trace( "import engine registry data ..." );
		String registryFile = getRegistryFile();
		Document doc = ConfReader.readXmlFile( execrc , registryFile );
		Node root = doc.getDocumentElement();
		
		Node node;
		node = ConfReader.xmlGetFirstChild( root , ELEMENT_RESOURCES );
		resourcesNew = new EngineResources( engine ); 
		DBEngineResources.importxml( loader , resourcesNew , node );
		node = ConfReader.xmlGetFirstChild( root , ELEMENT_MIRRORS );
		mirrorsNew = new EngineMirrors( engine );
		DBEngineMirrors.importxml( loader , mirrorsNew , node );
		node = ConfReader.xmlGetFirstChild( root , ELEMENT_BUILDERS );
		buildersNew = new EngineBuilders( engine );
		DBEngineBuilders.importxml( loader , buildersNew , node );
	}

	private void loaddbRegistry() throws Exception {
		trace( "load engine registry data ..." );
		resourcesNew = new EngineResources( engine ); 
		DBEngineResources.loaddb( loader , resourcesNew );
		mirrorsNew = new EngineMirrors( engine );
		DBEngineMirrors.loaddb( loader , mirrorsNew );
		buildersNew = new EngineBuilders( engine );
		DBEngineBuilders.loaddb( loader , buildersNew );
	}

	public void loaddbDirectory() throws Exception {
		trace( "load engine directory data ..." );
		directoryNew = new EngineDirectory( engine );
		DBEngineDirectory.loaddb( loader , directoryNew );
	}
	
	public void importxmlDirectory() throws Exception {
		trace( "import engine directory data ..." );
		String registryFile = getRegistryFile();
		Document doc = ConfReader.readXmlFile( execrc , registryFile );
		Node root = doc.getDocumentElement();
		
		directoryNew = new EngineDirectory( engine );
		Node node = ConfReader.xmlGetFirstChild( root , ELEMENT_DIRECTORY );
		DBEngineDirectory.importxml( loader , directoryNew , node );
	}
	
	public void importxmlAuth( EngineAuth auth ) throws Exception {
		trace( "import engine auth data ..." );
		String authFile = getAuthFile();
		Document doc = ConfReader.readXmlFile( execrc , authFile );
		Node root = doc.getDocumentElement();
		
		DBEngineAuth.importxml( loader , auth , root );
	}

	public void loaddbAuth( EngineAuth auth ) throws Exception {
		trace( "load engine auth data ..." );
		DBEngineAuth.loaddb( loader , auth );
	}

	private void exportxmlRegistry() throws Exception {
		trace( "export engine registry data ..." );
		String propertyFile = getRegistryFile();
		Document doc = Common.xmlCreateDoc( "registry" );
		Element root = doc.getDocumentElement();
		
		Element node;
		node = Common.xmlCreateElement( doc , root , ELEMENT_RESOURCES );
		DBEngineResources.exportxml( loader , data.getResources() , doc , node );
		node = Common.xmlCreateElement( doc , root , ELEMENT_MIRRORS );
		DBEngineMirrors.exportxml( loader , data.getMirrors() , doc , node );
		node = Common.xmlCreateElement( doc , root , ELEMENT_BUILDERS );
		DBEngineBuilders.exportxml( loader , data.getBuilders() , doc , node );
		
		EngineDirectory directory = data.getDirectory();
		node = Common.xmlCreateElement( doc , root , "directory" );
		DBEngineDirectory.exportxml( loader , directory , doc , node );
		
		Common.xmlSaveDoc( doc , propertyFile );
	}

	private void exportxmlBase() throws Exception {
		trace( "export engine base data ..." );
		String propertyFile = getBaseFile();
		EngineBase base = data.getEngineBase();
		Document doc = Common.xmlCreateDoc( "base" );
		Element root = doc.getDocumentElement();
		DBEngineBase.exportxml( loader , base , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	private void exportxmlInfrastructure() throws Exception {
		trace( "export engine infrastructure data ..." );
		String propertyFile = getInfrastructureFile();
		EngineInfrastructure infra = data.getInfrastructure();
		Document doc = Common.xmlCreateDoc( "infrastructure" );
		Element root = doc.getDocumentElement();
		DBEngineInfrastructure.exportxml( loader , infra , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	private void exportxmlReleaseLifecycles() throws Exception {
		trace( "export engine lifecycles data ..." );
		String propertyFile = getReleaseLifecyclesFile();
		EngineLifecycles lifecycles = data.getReleaseLifecycles();
		Document doc = Common.xmlCreateDoc( "lifecycles" );
		Element root = doc.getDocumentElement();
		DBEngineLifecycles.exportxml( loader , lifecycles , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	private void exportxmlMonitoring() throws Exception {
		trace( "export engine monitoring data ..." );
		String propertyFile = getMonitoringFile();
		EngineMonitoring mon = data.getMonitoring();
		Document doc = Common.xmlCreateDoc( "monitoring" );
		Element root = doc.getDocumentElement();
		DBEngineMonitoring.savexml( loader , mon , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	private void exportxmlSettings() throws Exception {
		trace( "export engine settings data ..." );
		EngineSettings settings = data.getEngineSettings();
		String propertyFile = getEngineSettingsFile();
		Document doc = Common.xmlCreateDoc( "engine" );
		Element root = doc.getDocumentElement();
		DBEngineSettings.exportxml( loader , settings , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}
	
	private void exportxmlAuth( EngineAuth auth ) throws Exception {
		trace( "export engine auth data ..." );
		String authFile = getAuthFile();
		Document doc = Common.xmlCreateDoc( "auth" );
		Element root = doc.getDocumentElement();
		DBEngineAuth.exportxml( loader , auth , doc , root );
		Common.xmlSaveDoc( doc , authFile );
	}

	private String getEngineSettingsFile() {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "server.xml" );
		return( propertyFile );
	}
	
	private String getBaseFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "base.xml" );
		return( propertyFile );
	}

	private String getInfrastructureFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "networks.xml" );
		return( propertyFile );
	}

	private String getReleaseLifecyclesFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "lifecycles.xml" );
		return( propertyFile );
	}

	private String getMonitoringFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "monitoring.xml" );
		return( propertyFile );
	}

	private String getRegistryFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "registry.xml" );
		return( propertyFile );
	}

	private String getAuthFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "auth.xml" );
		return( propertyFile );
	}

	public void trace( String s ) {
		loader.trace( s );
	}

	public void importxmlCore() throws Exception {
		DBConnection c = loader.getConnection();
		
		int version = c.getNextCoreVersion();
		trace( "create new engine core version=" + version + " ..." );
		importxmlEngineSettings();
		importxmlBase();
		importxmlReleaseLifecycles();
		importxmlMonitoring();
		importxmlRegistry();
		importxmlInfrastructure();
		loader.saveConnection( true );
		
		// create distributive folder
		EngineSettings settings = loader.getSettings();
		EngineContext sc = settings.getServerContext();
		ActionBase action = loader.getAction();
		LocalFolder folder = action.getLocalFolder( sc.DIST_ROOT );
		folder.ensureExists( action );
		
		trace( "successfully completed import of engine core data" );
	}

	public void loaddbCore() throws Exception {
		DBConnection c = loader.getConnection();
		
		trace( "load engine core data, version=" + c.getCoreVersion() + " ..." );
		loaddbEngineSettings();
		loaddbBase();
		loaddbReleaseLifecycles();
		loaddbMonitoring();
		loaddbRegistry();
		loaddbInfrastructure();
	}
	
}
