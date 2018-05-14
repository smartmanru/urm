package org.urm.meta.loader;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.engine.DBEngineAuth;
import org.urm.db.engine.DBEngineBase;
import org.urm.db.engine.DBEngineBuilders;
import org.urm.db.engine.DBEngineInfrastructure;
import org.urm.db.engine.DBEngineLifecycles;
import org.urm.db.engine.DBEngineMirrors;
import org.urm.db.engine.DBEngineMonitoring;
import org.urm.db.engine.DBEngineResources;
import org.urm.db.engine.DBEngineSettings;
import org.urm.engine.Engine;
import org.urm.engine.AuthService;
import org.urm.engine.DataService;
import org.urm.engine.data.EngineBase;
import org.urm.engine.data.EngineBuilders;
import org.urm.engine.data.EngineContext;
import org.urm.engine.data.EngineInfrastructure;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.data.EngineMirrors;
import org.urm.engine.data.EngineMonitoring;
import org.urm.engine.data.EngineResources;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.engine._Error;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineLoaderCore {

	private EngineLoader loader;
	private DataService data;
	public RunContext execrc;
	public Engine engine;
	
	private EngineSettings settingsNew;
	private EngineResources resourcesNew;
	private EngineBuilders buildersNew;
	private EngineMirrors mirrorsNew;
	private EngineBase baseNew;
	private EngineInfrastructure infraNew;
	private EngineLifecycles lifecyclesNew;
	private EngineMonitoring monitoringNew;
	
	public EngineLoaderCore( EngineLoader loader , DataService data ) {
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

	public EngineBase getBase() {
		if( baseNew != null )
			return( baseNew );
		return( data.getEngineBase() );
	}

	public EngineMonitoring getMonitoring() {
		if( infraNew != null )
			return( monitoringNew );
		return( data.getMonitoring() );
	}

	public EngineLifecycles getLifecycles() {
		if( lifecyclesNew != null )
			return( lifecyclesNew );
		return( data.getReleaseLifecycles() );
	}

	public void exportEngine() throws Exception {
		exportCore( true );
		exportAuth( engine.getAuth() );
	}
	
	private void exportCore( boolean includingSystems ) throws Exception {
		trace( "export engine core data ..." );
		exportxmlSettings();
		exportxmlResources();
		exportxmlInfrastructure();
		
		exportxmlBase();
		exportxmlReleaseLifecycles();
		exportxmlMonitoring();
		exportxmlMirrors();
		exportxmlBuilders();
	}

	private void exportAuth( AuthService auth ) throws Exception {
		trace( "export engine auth data ..." );
		exportxmlAuth( auth );
	}
	
	public void setData() {
		TransactionBase transaction = loader.getTransaction();
		
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
		
		if( mirrorsNew != null ) {
			transaction.setMirrors( mirrorsNew );
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

	private void importxmlResources() throws Exception {
		trace( "import engine resources data ..." );
		String resourcesFile = getResourcesFile();
		Document doc = ConfReader.readXmlFile( execrc , resourcesFile );
		Node root = doc.getDocumentElement();

		resourcesNew = new EngineResources( engine ); 
		DBEngineResources.importxml( loader , resourcesNew , root );
	}
	
	private void importxmlMirrors() throws Exception {
		trace( "import engine mirrors data ..." );
		String mirrorsFile = getMirrorsFile();
		Document doc = ConfReader.readXmlFile( execrc , mirrorsFile );
		Node root = doc.getDocumentElement();

		mirrorsNew = new EngineMirrors( engine );
		DBEngineMirrors.importxml( loader , mirrorsNew , root );
	}
	
	private void importxmlBuilders() throws Exception {
		trace( "import engine builders data ..." );
		String buildersFile = getBuildersFile();
		Document doc = ConfReader.readXmlFile( execrc , buildersFile );
		Node root = doc.getDocumentElement();

		buildersNew = new EngineBuilders( engine );
		DBEngineBuilders.importxml( loader , buildersNew , root );
	}
	
	private void loaddbResources() throws Exception {
		trace( "load engine resources data ..." );
		resourcesNew = new EngineResources( engine ); 
		DBEngineResources.loaddb( loader , resourcesNew );
	}
	
	private void loaddbMirrors() throws Exception {
		trace( "load engine mirrors data ..." );
		mirrorsNew = new EngineMirrors( engine );
		DBEngineMirrors.loaddb( loader , mirrorsNew );
	}
	
	private void loaddbBuilders() throws Exception {
		trace( "load engine builders data ..." );
		buildersNew = new EngineBuilders( engine );
		DBEngineBuilders.loaddb( loader , buildersNew );
	}

	public void importxmlAuth( AuthService auth ) throws Exception {
		trace( "import engine auth data ..." );
		String authFile = getAuthFile();
		Document doc = ConfReader.readXmlFile( execrc , authFile );
		Node root = doc.getDocumentElement();
		
		DBEngineAuth.importxml( loader , auth , root );
	}

	public void loaddbAuth( AuthService auth ) throws Exception {
		trace( "load engine auth data ..." );
		DBEngineAuth.loaddb( loader , auth );
	}

	private void exportxmlResources() throws Exception {
		trace( "export engine resources data ..." );
		String propertyFile = getResourcesFile();
		Document doc = Common.xmlCreateDoc( "resources" );
		Element root = doc.getDocumentElement();
		DBEngineResources.exportxml( loader , data.getResources() , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}
		
	private void exportxmlMirrors() throws Exception {
		trace( "export engine mirrors data ..." );
		String propertyFile = getMirrorsFile();
		Document doc = Common.xmlCreateDoc( "mirrors" );
		Element root = doc.getDocumentElement();
		DBEngineMirrors.exportxml( loader , data.getMirrors() , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}
	
	private void exportxmlBuilders() throws Exception {
		trace( "export engine builders data ..." );
		String propertyFile = getBuildersFile();
		Document doc = Common.xmlCreateDoc( "builders" );
		Element root = doc.getDocumentElement();
		DBEngineBuilders.exportxml( loader , data.getBuilders() , doc , root );
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
	
	private void exportxmlAuth( AuthService auth ) throws Exception {
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

	private String getResourcesFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "resources.xml" );
		return( propertyFile );
	}

	private String getMirrorsFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "mirrors.xml" );
		return( propertyFile );
	}

	private String getBuildersFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "builders.xml" );
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
		importxmlResources();
		importxmlInfrastructure();
		importxmlMirrors();
		importxmlBase();
		importxmlReleaseLifecycles();
		importxmlMonitoring();
		importxmlBuilders();
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
		loaddbResources();
		loaddbInfrastructure();
		loaddbMirrors();
		loaddbBase();
		loaddbReleaseLifecycles();
		loaddbMonitoring();
		loaddbBuilders();
	}
	
}
