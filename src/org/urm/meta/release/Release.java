package org.urm.meta.release;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistItemInfo;
import org.urm.engine.dist.DistLabelInfo;
import org.urm.engine.dist.ReleaseMaster;
import org.urm.engine.dist.ReleaseMasterItem;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.dist._Error;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Release {

	public Meta meta;
	public Dist dist;
	
	public String RELEASEVER;
	
	public boolean MASTER;
	public DBEnumBuildModeType BUILDMODE;
	public String COMPATIBILITY;
	private boolean CUMULATIVE;
	
	Map<String,ReleaseScopeSet> sourceSetMap = new HashMap<String,ReleaseScopeSet>();
	Map<DBEnumScopeCategoryType,ReleaseScopeSet> categorySetMap = new HashMap<DBEnumScopeCategoryType,ReleaseScopeSet>();
	Map<String,ReleaseDelivery> deliveryMap = new HashMap<String,ReleaseDelivery>();

	public ReleaseSchedule schedule;
	public ReleaseMaster master;
	public ReleaseChanges changes;

	public static String ELEMENT_RELEASE = "release";
	public static String ELEMENT_SET = "set";
	public static String ELEMENT_PROJECT = "project";
	public static String ELEMENT_CONFITEM = "confitem";
	public static String ELEMENT_DISTITEM = "distitem";
	public static String ELEMENT_DELIVERY = "delivery";
	public static String ELEMENT_SCHEMA = "schema";
	public static String ELEMENT_DOC = "doc";
	public static String ELEMENT_HISTORY = "history";
	public static String ELEMENT_FILES = "files";
	public static String ELEMENT_PHASE = "phase";
	public static String ELEMENT_SCHEDULE = "schedule";
	public static String ELEMENT_CHANGES = "changes";
	public static String ELEMENT_TICKETSET = "ticketset";
	public static String ELEMENT_TICKET = "ticket";
	public static String ELEMENT_TICKETSETTARGET = "target";

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_ARCHIVEDSTATUS = "archived";
	public static String PROPERTY_MASTER = "master";
	public static String PROPERTY_VERSION = "version";
	public static String PROPERTY_LIFECYCLETYPE = "type";
	public static String PROPERTY_BUILDMODE = "mode"; 
	public static String PROPERTY_COMPATIBILITY = "over";
	public static String PROPERTY_CUMULATIVE = "cumulative";
	
	public static String PROPERTY_RELEASE = "release";
	public static String PROPERTY_DATEADDED = "added";
	public static String PROPERTY_KEY = "key";
	public static String PROPERTY_DELIVERY = "delivery";
	public static String PROPERTY_FOLDER = "folder";
	public static String PROPERTY_FILE = "folder";
	public static String PROPERTY_MD5 = "md5";
	
	public static String PROPERTY_TICKETTARGETTYPE = "type";
	public static String PROPERTY_TICKETTARGETITEM = "item";
	public static String PROPERTY_TICKETTARGETCOMMENTS = "comments";
	public static String PROPERTY_TICKETTARGETACCEPTED = "accepted";
	public static String PROPERTY_TICKETTARGETDESCOPED = "descoped";
	
	public Release( Meta meta , Dist dist ) {
		this.meta = meta;
		this.dist = dist;
		schedule = new ReleaseSchedule( meta , this );
	}

	public Release copy( ActionBase action , Dist rdist ) throws Exception {
		Release rr = new Release( rdist.meta , rdist );
		rr.RELEASEVER = RELEASEVER;
		
		rr.MASTER = MASTER;
		rr.BUILDMODE = BUILDMODE;
		rr.COMPATIBILITY = COMPATIBILITY;
		rr.CUMULATIVE = CUMULATIVE;
		
		rr.copyReleaseScope( action , this );
		rr.schedule = schedule.copy( action , rr.meta , rr , false );
		
		if( changes != null )
			rr.changes = changes.copy( action , meta , rr );
		if( master != null )
			rr.master = master.copy( action , rr );
		
		return( rr );
	}
	
	public void copyReleaseScope( ActionBase action , Release src ) throws Exception {
		descopeAll( action );
		for( Entry<String,ReleaseScopeSet> entry : src.sourceSetMap.entrySet() ) {
			ReleaseScopeSet set = entry.getValue().copy( action , this );
			sourceSetMap.put( entry.getKey() , set );
		}
		
		for( Entry<DBEnumScopeCategoryType,ReleaseScopeSet> entry : src.categorySetMap.entrySet() ) {
			ReleaseScopeSet set = entry.getValue().copy( action , this );
			categorySetMap.put( entry.getKey() , set );
		}
		
		for( Entry<String,ReleaseDelivery> entry : src.deliveryMap.entrySet() ) {
			ReleaseDelivery set = entry.getValue().copy( action , this );
			deliveryMap.put( entry.getKey() , set );
		}
	}

	public void createMaster( ActionBase action , String RELEASEVER , boolean copy ) throws Exception {
		this.RELEASEVER = RELEASEVER;

		schedule.create( action );
		schedule.createProd( action ); 
		
		this.MASTER = true;
		this.BUILDMODE = DBEnumBuildModeType.UNKNOWN;
		this.COMPATIBILITY = "";
		this.CUMULATIVE = true;

		master = new ReleaseMaster( meta , this );
		master.create( action );
		
		if( copy )
			master.addMasterHistory( action , RELEASEVER );
	}
	
	public void addRelease( ActionBase action , Release src ) throws Exception {
		if( this.MASTER )
			action.exitUnexpectedState();
		
		for( Entry<String,ReleaseScopeSet> entry : src.sourceSetMap.entrySet() ) {
			ReleaseScopeSet set = sourceSetMap.get( entry.getKey() );
			ReleaseScopeSet srcset = entry.getValue();
			if( set == null ) {
				set = srcset.copy( action , this );
				sourceSetMap.put( entry.getKey() , set );
			}
			else
				set.addReleaseSet( action , srcset );
		}
		
		for( Entry<DBEnumScopeCategoryType,ReleaseScopeSet> entry : src.categorySetMap.entrySet() ) {
			ReleaseScopeSet set = categorySetMap.get( entry.getKey() );
			ReleaseScopeSet srcset = entry.getValue();
			if( set == null ) {
				set = srcset.copy( action , this );
				categorySetMap.put( entry.getKey() , set );
			}
			else
				set.addReleaseSet( action , srcset );
		}
	}
	
	public boolean isCumulative() {
		return( CUMULATIVE );
	}
	
	public String[] getApplyVersions( ActionBase action ) throws Exception {
		if( dist.release.isCumulative() )
			return( dist.release.getCumulativeVersions() );
		return( new String[] { dist.release.RELEASEVER } );
	}
	
	public void setReleaseVer( ActionBase action , String RELEASEVER ) throws Exception {
		this.RELEASEVER = RELEASEVER;
	}
	
	public void setReleaseDate( ActionBase action , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		if( lc == null )
			schedule.changeReleaseSchedule( action , releaseDate );
		else
			schedule.createReleaseSchedule( action , releaseDate , lc );
	}
	
	public void setProperties( ActionBase action ) throws Exception {
		BUILDMODE = action.context.CTX_BUILDMODE;
		
		if( action.context.CTX_ALL )
			COMPATIBILITY = "";
		for( String OLDRELEASE : Common.splitSpaced( action.context.CTX_OLDRELEASE ) ) {
			OLDRELEASE = DistLabelInfo.normalizeReleaseVer( action , OLDRELEASE );
			if( OLDRELEASE.compareTo( RELEASEVER ) >= 0 )
				action.exit1( _Error.CompatibilityExpectedForEarlierRelease1 , "compatibility is expected for earlier release (version=" + OLDRELEASE + ")" , OLDRELEASE );
			
			COMPATIBILITY = Common.addItemToUniqueSpacedList( COMPATIBILITY , OLDRELEASE );
		}
	}
	
	public void createNormal( ActionBase action , String RELEASEVER , Date releaseDate , ReleaseLifecycle lc , String RELEASEFILEPATH ) throws Exception {
		this.RELEASEVER = DistLabelInfo.normalizeReleaseVer( action , RELEASEVER );
		this.MASTER = false;
		this.CUMULATIVE = action.context.CTX_CUMULATIVE;

		changes = new ReleaseChanges( meta , this );
		schedule.create( action );
		schedule.createReleaseSchedule( action , releaseDate , lc );
		setProperties( action );
		createEmptyXml( action , RELEASEFILEPATH );
	}

	public void createMaster( ActionBase action , String RELEASEVER , String filePath ) throws Exception {
		createMaster( action , RELEASEVER , false );
		Document doc = createXml( action );
		Common.xmlSaveDoc( doc , filePath );
	}
	
	public String[] getSourceSetNames() {
		return( Common.getSortedKeys( sourceSetMap ) );
	}
	
	public ReleaseScopeSet[] getSourceSets() {
		return( sourceSetMap.values().toArray( new ReleaseScopeSet[0] ) );
	}
	
	public ReleaseScopeSet findSourceSet( String name ) {
		return( sourceSetMap.get( name ) );
	}
	
	public ReleaseScopeSet getSourceSet( ActionBase action , String name ) throws Exception {
		ReleaseScopeSet set = findSourceSet( name );
		if( set == null )
			action.exit1( _Error.UnknownReleaseSet1 , "unknown release set=" + name , name );
		return( set );
	}
	
	public Map<DBEnumScopeCategoryType,ReleaseScopeSet> getCategorySets( ActionBase action ) throws Exception {
		return( categorySetMap );
	}
	
	public ReleaseScopeSet findCategorySet( DBEnumScopeCategoryType CATEGORY ) {
		return( categorySetMap.get( CATEGORY ) );
	}
	
	public ReleaseScopeSet getCategorySet( ActionBase action , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		ReleaseScopeSet set = findCategorySet( CATEGORY );
		if( set == null ) {
			String name = Common.getEnumLower( CATEGORY );
			action.exit1( _Error.UnknownReleaseCategorySet1 , "unknown release category set=" + name , name );
		}
		return( set );
	}

	private void loadSets( ActionBase action , Node root , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		Node element = ConfReader.xmlGetFirstChild( root , Common.getEnumLower( CATEGORY ) );
		if( element == null )
			return;
		
		if( CATEGORY.isSourceCategory() ) {
			Node[] sets = ConfReader.xmlGetChildren( element , ELEMENT_SET );
			if( sets == null )
				return;
			
			for( Node node : sets ) {
				ReleaseScopeSet set = new ReleaseScopeSet( meta , this , CATEGORY );
				set.load( action , node );
				registerSet( action , set );
			}
		}
		else {
			ReleaseScopeSet set = new ReleaseScopeSet( meta , this , CATEGORY );
			set.load( action , element );
			if( !set.isEmpty() )
				registerSet( action , set );
		}
	}
	
	public Document load( ActionBase action , String RELEASEFILEPATH ) throws Exception {
		// read xml
		String file = RELEASEFILEPATH;
		
		action.debug( "read release file " + file + "..." );
		Document doc = action.readXmlFile( file );
		Node root = doc.getDocumentElement();

		RELEASEVER = ConfReader.getAttrValue( root , PROPERTY_VERSION );
		if( RELEASEVER.isEmpty() )
			action.exit0( _Error.ReleaseVersionNotSet0 , "release version property is not set, unable to use distributive" );
		
		// properties
		MASTER = getReleasePropertyBoolean( action , root , PROPERTY_MASTER , false );
		BUILDMODE = getReleasePropertyBuildMode( action , root , PROPERTY_BUILDMODE ); 
		COMPATIBILITY = getReleaseProperty( action , root , PROPERTY_COMPATIBILITY );
		CUMULATIVE = getReleasePropertyBoolean( action , root , PROPERTY_CUMULATIVE , false );

		schedule.load( action , root );
		
		if( MASTER ) {
			Node node = ConfReader.xmlGetFirstChild( root , Dist.MASTER_LABEL );
			master = new ReleaseMaster( meta , this );
			master.load( action , node );
		}
		else {
			// get project sets
			for( DBEnumScopeCategoryType CATEGORY : DBEnumScopeCategoryType.getAllReleaseCategories() )
				loadSets( action , root , CATEGORY );
			changes = new ReleaseChanges( meta , this );
			changes.load( action , root );
		}
		
		return( doc );
	}

	private void registerSet( ActionBase action , ReleaseScopeSet set ) throws Exception {
		action.trace( "add set=" + set.NAME + ", category=" + Common.getEnumLower( set.CATEGORY ) );
		if( set.CATEGORY.isSourceCategory() )
			sourceSetMap.put( set.NAME , set );
		else
			categorySetMap.put( set.CATEGORY , set );
		
		for( ReleaseScopeTarget target : set.getTargets() )
			registerTarget( action , target );
	}

	private void registerTargetItem( ActionBase action , ReleaseScopeItem item ) throws Exception {
		action.trace( "add item=" + item.NAME );
		ReleaseDelivery releaseDelivery = registerDelivery( action , item.getDelivery( action ) );
		releaseDelivery.addTargetItem( action , item );
	}
	
	private void registerTarget( ActionBase action , ReleaseScopeTarget target ) throws Exception {
		action.trace( "add target=" + target.NAME );
		if( target.CATEGORY.isSourceCategory() ) {
			for( ReleaseScopeItem item : target.getItems() )
				registerTargetItem( action , item );
		}
		else
		if( target.isDatabaseTarget() ) {
			for( ReleaseScopeItem item : target.getItems() )
				registerTargetItem( action , item );
		}
		else
		if( target.isDocTarget() ) {
			for( ReleaseScopeItem item : target.getItems() )
				registerTargetItem( action , item );
		}
		else {
			ReleaseDelivery releaseDelivery = registerDelivery( action , target.getDelivery( action ) );
			releaseDelivery.addCategoryTarget( action , target );
		}
	}
	
	private ReleaseDelivery registerDelivery( ActionBase action , MetaDistrDelivery distDelivery ) throws Exception {
		ReleaseDelivery releaseDelivery = deliveryMap.get( distDelivery.NAME );
		if( releaseDelivery == null ) {
			action.trace( "add delivery=" + distDelivery.NAME );
			releaseDelivery = new ReleaseDelivery( meta , this , distDelivery );
			deliveryMap.put( distDelivery.NAME , releaseDelivery );
		}
		return( releaseDelivery );
	}

	private void unregisterTargetItem( ActionBase action , ReleaseScopeItem item ) throws Exception {
		MetaDistrDelivery distDelivery = item.getDelivery( action );
		if( distDelivery == null )
			return;
		
		ReleaseDelivery releaseDelivery = deliveryMap.get( distDelivery.NAME );
		if( releaseDelivery == null )
			return;
		
		releaseDelivery.removeTargetItem( action , item );
	}

	private String getReleaseProperty( ActionBase action , Node node , String name ) throws Exception {
		String value = ConfReader.getPropertyValue( node , name );
		if( value == null )
			return( "" );
		return( value );
	}

	private DBEnumBuildModeType getReleasePropertyBuildMode( ActionBase action , Node node , String name ) throws Exception {
		String value = getReleaseProperty( action , node , name );
		if( value.isEmpty() )
			return( DBEnumBuildModeType.UNKNOWN );
		return( DBEnumBuildModeType.valueOf( value.toUpperCase() ) );
	}
	
	private boolean getReleasePropertyBoolean( ActionBase action , Node node , String name , boolean defValue ) throws Exception {
		String value = getReleaseProperty( action , node , name );
		if( value.isEmpty() )
			return( defValue );
		
		return( Common.getBooleanValue( value ) );
	}

	public String getReleaseCandidateTag( ActionBase action ) {
		return( "prod-" + RELEASEVER + "-candidate" );
	}
	
	public ReleaseScopeTarget[] getSourceTargets( ActionBase action , String setName ) throws Exception {
		ReleaseScopeSet set = sourceSetMap.get( setName );
		if( set == null )
			return( new ReleaseScopeTarget[0] );
		return( set.getTargets() );
	}

	public ReleaseScopeTarget[] getCategoryTargets( ActionBase action , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		ReleaseScopeSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( new ReleaseScopeTarget[0] );
		return( set.getTargets() );
	}

	public ReleaseScopeTarget findBuildProject( ActionBase action , String name ) throws Exception {
		MetaSources sources = meta.getSources(); 
		MetaSourceProject sourceProject = sources.getProject( name );
		ReleaseScopeSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return( null );
		
		ReleaseScopeTarget project = set.findTarget( name );
		return( project );
	}
	
	public ReleaseScopeTarget getBuildProject( ActionBase action , String name ) throws Exception {
		ReleaseScopeTarget project = findBuildProject( action , name );
		if( project == null )
			action.exit1( _Error.UnknownReleaseProject1 , "unknown release project=" + name , name );
		
		return( project );
	}

	public ReleaseScopeTarget findCategoryTarget( ActionBase action , DBEnumScopeCategoryType CATEGORY , String KEY ) throws Exception {
		ReleaseScopeSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( null );
		
		ReleaseScopeTarget target = set.findTarget( KEY );
		return( target );
	}
	
	public ReleaseScopeTarget findConfComponent( ActionBase action , String KEY ) throws Exception {
		return( findCategoryTarget( action , DBEnumScopeCategoryType.CONFIG , KEY ) );
	}
	
	public ReleaseScopeTarget getConfComponent( ActionBase action , String KEY ) throws Exception {
		ReleaseScopeTarget comp = findConfComponent( action , KEY );
		if( comp == null )
			action.exit1( _Error.UnknownReleaseComponent1 , "unknown release component=" + KEY , KEY );
		
		return( comp );
	}

	public ReleaseScopeTarget[] getCategoryTargets( DBEnumScopeCategoryType CATEGORY ) {
		ReleaseScopeSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( new ReleaseScopeTarget[0] );
		
		return( set.getTargets() );
	}
	
	public ReleaseScopeTarget[] getConfComponents() {
		return( getCategoryTargets( DBEnumScopeCategoryType.CONFIG ) );
	}

	public ReleaseDelivery[] getDeliveries() {
		return( deliveryMap.values().toArray( new ReleaseDelivery[0] ) );
	}

	public String[] getDeliveryNames() {
		return( Common.getSortedKeys( deliveryMap ) );
	}

	public ReleaseDelivery findDelivery( String name ) {
		ReleaseDelivery delivery = deliveryMap.get( name );
		return( delivery );
	}
	
	public ReleaseDelivery findDeliveryByFolder( String folder ) {
		for( ReleaseDelivery delivery : deliveryMap.values() ) {
			if( delivery.distDelivery.FOLDER.equals( folder ) )
				return( delivery );
		}
		return( null );
	}
	
	public ReleaseDelivery getDelivery( ActionBase action , String name ) throws Exception {
		ReleaseDelivery delivery = deliveryMap.get( name );
		if( delivery == null )
			action.exit1( _Error.UnknownReleaseDelivery1 , "unknown delivery name=" + name , name );
		
		return( delivery );
	}
	
	public ReleaseDelivery getDeliveryByFolder( ActionBase action , String folder ) throws Exception {
		for( ReleaseDelivery delivery : deliveryMap.values() )
			if( delivery.distDelivery.FOLDER.equals( folder ) )
				return( delivery );
		
		action.exit1( _Error.UnknownReleaseDeliveryFolder1 , "unknown delivery folder=" + folder , folder );
		return( null );
	}
	
	public boolean isEmpty() {
		for( ReleaseScopeSet set : sourceSetMap.values() )
			if( !set.isEmpty() )
				return( false );
		for( ReleaseScopeSet set : categorySetMap.values() )
			if( !set.isEmpty() )
				return( false );
		return( true );
	}

	public boolean isEmptyConfiguration() throws Exception {
		ReleaseScopeSet set = findCategorySet( DBEnumScopeCategoryType.CONFIG );
		if( set == null || set.isEmpty() )
			return( true );
		
		return( false );
	}
	
	public boolean isEmptyDatabase() {
		ReleaseScopeSet set = findCategorySet( DBEnumScopeCategoryType.DB );
		if( set == null || set.isEmpty() )
			return( true );
		
		return( false );
	}
	
	public boolean isEmptyDoc() {
		ReleaseScopeSet set = findCategorySet( DBEnumScopeCategoryType.DOC );
		if( set == null || set.isEmpty() )
			return( true );
		
		return( false );
	}
	
	public Document createEmptyXmlDoc( ActionBase action ) throws Exception {
		Document doc = Common.xmlCreateDoc( ELEMENT_RELEASE );
		Element root = doc.getDocumentElement();
		Common.xmlSetElementAttr( doc , root , PROPERTY_VERSION , RELEASEVER );
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_MASTER , Common.getBooleanValue( MASTER ) );
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_BUILDMODE , Common.getEnumLower( BUILDMODE ) );
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_COMPATIBILITY , COMPATIBILITY );
		Common.xmlCreateBooleanPropertyElement( doc , root , PROPERTY_CUMULATIVE , CUMULATIVE );
		
		for( DBEnumScopeCategoryType CATEGORY : DBEnumScopeCategoryType.getAllReleaseCategories() )
			Common.xmlCreateElement( doc , root , Common.getEnumLower( CATEGORY ) );
		
		schedule.save( action , doc , root );
		
		return( doc );
	}
	
	public void createEmptyXml( ActionBase action , String filePath ) throws Exception {
		Document doc = createEmptyXmlDoc( action );
		Common.xmlSaveDoc( doc , filePath );
	}

	public Document createXml( ActionBase action ) throws Exception {
		Document doc = createEmptyXmlDoc( action );
		Element root = doc.getDocumentElement();
		
		if( MASTER ) {
			Element parent = Common.xmlCreateElement( doc , root , Dist.MASTER_LABEL );
			master.save( action , doc , parent );
		}
		else {
			for( ReleaseScopeSet set : sourceSetMap.values() ) {
				Element parent = ( Element )ConfReader.xmlGetFirstChild( root , Common.getEnumLower( set.CATEGORY ) );
				set.createXml( action , doc , parent );
			}
	
			for( ReleaseScopeSet set : categorySetMap.values() ) {
				Element parent = ( Element )ConfReader.xmlGetFirstChild( root , Common.getEnumLower( set.CATEGORY ) );
				set.createXml( action , doc , parent );
			}
			
			changes.save( action , doc , root );
		}
			
		return( doc );
	}

	public void addSourceAll( ActionBase action ) throws Exception {
		MetaSources sources = meta.getSources(); 
		for( MetaSourceProjectSet sourceSet : sources.getSets() )
			addSourceSet( action , sourceSet , true );
	}
	
	public boolean addSourceSet( ActionBase action , MetaSourceProjectSet sourceSet , boolean all ) throws Exception {
		ReleaseScopeSet set = findSourceSet( sourceSet.NAME );
		if( set == null ) {
			set = new ReleaseScopeSet( meta , this , DBEnumScopeCategoryType.PROJECT );
			set.createSourceSet( action , sourceSet , all );
			registerSet( action , set );
			return( true );
		}
		
		if( all == true && set.ALL == false ) {
			if( !set.checkPropsEqualsToOptions( action ) )
				return( false );
			
			deleteSourceSet( action , sourceSet );
			addSourceSet( action , sourceSet , true );
			return( true );
		}
		
		return( true );
	}
	
	public boolean addCategorySet( ActionBase action , DBEnumScopeCategoryType CATEGORY , boolean all ) throws Exception {
		ReleaseScopeSet set = findCategorySet( CATEGORY );
		if( set == null ) {
			set = new ReleaseScopeSet( meta , this , CATEGORY );
			set.createCategorySet( action , CATEGORY , all );
			registerSet( action , set );
			return( true );
		}
		
		if( all == true && set.ALL == false ) {
			if( !set.checkPropsEqualsToOptions( action ) )
				return( false );
			
			deleteCategorySet( action , CATEGORY );
			addCategorySet( action , CATEGORY , true );
			return( true );
		}
		
		return( true );
	}

	public void deleteCategorySet( ActionBase action , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		ReleaseScopeSet set = findCategorySet( CATEGORY );
		if( set == null )
			return;
		
		unregisterSet( action , set );
	}

	public void deleteSourceSet( ActionBase action , MetaSourceProjectSet sourceSet ) throws Exception {
		ReleaseScopeSet set = findSourceSet( sourceSet.NAME );
		if( set == null )
			return;
		
		unregisterSet( action , set );
	}

	private void unregisterTarget( ActionBase action , ReleaseScopeTarget target ) throws Exception {
		if( target.CATEGORY.isSourceCategory() ) {
			for( ReleaseScopeItem item : target.getItems() )
				unregisterTargetItem( action , item );
		}
		else
		if( target.isDatabaseTarget() ) {
			for( ReleaseScopeItem item : target.getItems() )
				unregisterTargetItem( action , item );
		}
		else
		if( target.isDocTarget() ) {
			for( ReleaseScopeItem item : target.getItems() )
				unregisterTargetItem( action , item );
		}
		else {
			MetaDistrDelivery distDelivery = target.getDelivery( action );
			if( distDelivery == null )
				return;
			
			ReleaseDelivery releaseDelivery = deliveryMap.get( distDelivery.NAME );
			if( releaseDelivery == null )
				return;
			
			releaseDelivery.removeCategoryTarget( action , target );
		}
	}

	private void unregisterSet( ActionBase action , ReleaseScopeSet set ) throws Exception {
		for( ReleaseScopeTarget project : set.getTargets() )
			unregisterTarget( action , project );
		
		if( set.CATEGORY.isSourceCategory() )
			sourceSetMap.remove( set.NAME );
		else
			categorySetMap.remove( set.CATEGORY );
	}

	public boolean addDatabaseDelivery( ActionBase action , MetaDistrDelivery delivery , boolean allSchemes ) throws Exception {
		ReleaseScopeSet set = getCategorySet( action , DBEnumScopeCategoryType.DB );
		if( set == null )
			return( false );
		
		ReleaseScopeTarget target = set.findTarget( delivery.NAME );
		if( target == null ) {
			target = set.addDatabaseDelivery( action , delivery , allSchemes );
			registerTarget( action , target );
			return( true );
		}
		
		if( allSchemes == true ) {
			deleteDatabaseDelivery( action , delivery );
			addDatabaseDelivery( action , delivery , true );
			return( true );
		}
		
		return( true );
	}
	
	public boolean addDocDelivery( ActionBase action , MetaDistrDelivery delivery , boolean allDocs ) throws Exception {
		ReleaseScopeSet set = getCategorySet( action , DBEnumScopeCategoryType.DOC );
		if( set == null )
			return( false );
		
		ReleaseScopeTarget target = set.findTarget( delivery.NAME );
		if( target == null ) {
			target = set.addDocDelivery( action , delivery , allDocs );
			registerTarget( action , target );
			return( true );
		}
		
		if( allDocs == true ) {
			deleteDocDelivery( action , delivery );
			addDocDelivery( action , delivery , true );
			return( true );
		}
		
		return( true );
	}
	
	public boolean addProject( ActionBase action , MetaSourceProject sourceProject , boolean allItems ) throws Exception {
		ReleaseScopeSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return( false );
		
		ReleaseScopeTarget project = set.findTarget( sourceProject.NAME );
		if( project == null ) {
			project = set.addSourceProject( action , sourceProject , allItems );
			registerTarget( action , project );
			return( true );
		}
		
		if( !project.checkPropsEqualsToOptions( action ) )
			return( false );
			
		if( allItems == true ) {
			deleteProjectSource( action , sourceProject );
			addProject( action , sourceProject , true );
			return( true );
		}
		
		return( true );
	}

	public void deleteTarget( ActionBase action , ReleaseScopeTarget target ) throws Exception {
		unregisterTarget( action , target );
		target.set.removeTarget( action , target );
	}
	
	public void deleteCategoryTarget( ActionBase action , DBEnumScopeCategoryType CATEGORY , String NAME ) throws Exception {
		ReleaseScopeSet set = getCategorySet( action , CATEGORY );
		if( set == null )
			return;

		ReleaseScopeTarget target = set.findTarget( NAME );
		if( target == null )
			return;

		deleteTarget( action , target );
	}
	
	public void deleteProjectSource( ActionBase action , MetaSourceProject sourceProject ) throws Exception {
		ReleaseScopeSet set = findSourceSet( sourceProject.set.NAME );
		if( set == null )
			return;
		
		ReleaseScopeTarget target = set.findTarget( sourceProject.NAME );
		if( target == null )
			return;
		
		deleteTarget( action , target );
	}

	public void deleteDatabaseDelivery( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		ReleaseScopeSet set = getCategorySet( action , DBEnumScopeCategoryType.DB );
		if( set == null )
			return;
		
		ReleaseScopeTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return;
		
		deleteTarget( action , target );
	}

	public void deleteDocDelivery( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		ReleaseScopeSet set = getCategorySet( action , DBEnumScopeCategoryType.DOC );
		if( set == null )
			return;
		
		ReleaseScopeTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return;
		
		deleteTarget( action , target );
	}

	public boolean addProjectItem( ActionBase action , MetaSourceProject sourceProject , MetaSourceProjectItem sourceItem ) throws Exception {
		if( sourceItem.isInternal() )
			action.exit1( _Error.UnexpectedInternalItem1 , "unexpected call for INTERNAL item=" + sourceItem.NAME , sourceItem.NAME );
		
		ReleaseScopeSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return( false );
		
		if( set.ALL )
			return( true );
		
		ReleaseScopeTarget project = set.findTarget( sourceProject.NAME );
		if( project == null )
			return( false );

		if( project.ALL )
			return( true );
		
		for( ReleaseScopeItem item : project.addSourceItem( action , sourceItem ) )
			registerTargetItem( action , item );
		return( true );
	}

	public boolean addDatabaseSchema( ActionBase action , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		ReleaseScopeSet set = getCategorySet( action , DBEnumScopeCategoryType.DB );
		if( set == null )
			return( false );
		
		if( set.ALL )
			return( true );
		
		ReleaseScopeTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return( false );

		if( target.ALL )
			return( true );
		
		ReleaseScopeItem item = target.addDeliverySchema( action , schema );
		registerTargetItem( action , item );
		return( true );
	}

	public boolean addDoc( ActionBase action , MetaDistrDelivery delivery , MetaProductDoc doc ) throws Exception {
		ReleaseScopeSet set = getCategorySet( action , DBEnumScopeCategoryType.DOC );
		if( set == null )
			return( false );
		
		if( set.ALL )
			return( true );
		
		ReleaseScopeTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return( false );

		if( target.ALL )
			return( true );
		
		ReleaseScopeItem item = target.addDeliveryDoc( action , doc );
		registerTargetItem( action , item );
		return( true );
	}

	public void deleteProjectItem( ActionBase action , ReleaseScopeItem item ) throws Exception {
		item.target.set.makePartial( action );
		item.target.removeSourceItem( action , item );
		unregisterTargetItem( action , item );
	}
	
	public void deleteDatabaseSchema( ActionBase action , ReleaseScopeItem item ) throws Exception {
		item.target.set.makePartial( action );
		item.target.removeDatabaseItem( action , item );
		unregisterTargetItem( action , item );
	}
	
	public void deleteDoc( ActionBase action , ReleaseScopeItem item ) throws Exception {
		item.target.set.makePartial( action );
		item.target.removeDocItem( action , item );
		unregisterTargetItem( action , item );
	}
	
	public void deleteProjectItem( ActionBase action , MetaSourceProject sourceProject , MetaSourceProjectItem sourceItem ) throws Exception {
		ReleaseScopeSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return;

		ReleaseScopeTarget project = set.findTarget( sourceProject.NAME );
		if( project == null )
			return;
		
		ReleaseScopeItem item = project.findProjectItem( sourceItem );
		if( item == null )
			return;

		deleteProjectItem( action , item );
	}
	
	public void deleteDatabaseSchema( ActionBase action , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		ReleaseScopeSet set = getCategorySet( action , DBEnumScopeCategoryType.DB );
		if( set == null )
			return;

		ReleaseScopeTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return;
		
		ReleaseScopeItem item = target.findDeliverySchema( schema );
		if( item == null )
			return;

		deleteDatabaseSchema( action , item );
	}
	
	public void deleteDoc( ActionBase action , MetaDistrDelivery delivery , MetaProductDoc doc ) throws Exception {
		ReleaseScopeSet set = getCategorySet( action , DBEnumScopeCategoryType.DOC );
		if( set == null )
			return;

		ReleaseScopeTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return;
		
		ReleaseScopeItem item = target.findDeliveryDoc( doc );
		if( item == null )
			return;

		deleteDoc( action , item );
	}
	
	public boolean addConfItem( ActionBase action , MetaDistrConfItem item ) throws Exception {
		ReleaseScopeSet set = getCategorySet( action , DBEnumScopeCategoryType.CONFIG );
		if( set.ALL )
			return( true );

		ReleaseScopeTarget target = set.findTarget( item.NAME );
		if( target != null ) {
			if( !target.ALL )
				target.setAll( action , action.context.CTX_REPLACE );
			return( true );
		}
		
		target = set.addConfItem( action , item , action.context.CTX_REPLACE );
		registerTarget( action , target );
		return( true );
	}

	public boolean addManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.ITEMORIGIN_TYPE != DBEnumItemOriginType.MANUAL )
			action.exit1( _Error.UnexpectedNonManualItem1 , "unexpected non-manual item=" + item.NAME , item.NAME );
			
		ReleaseScopeSet set = getCategorySet( action , DBEnumScopeCategoryType.MANUAL );
		if( set.ALL )
			return( true );

		ReleaseScopeTarget target = set.findTarget( item.NAME );
		if( target != null )
			return( true );
		
		target = set.addManualItem( action , item );
		registerTarget( action , target );
		return( true );
	}

	public boolean addDerivedItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.ITEMORIGIN_TYPE != DBEnumItemOriginType.DERIVED )
			action.exit1( _Error.UnexpectedNonManualItem1 , "unexpected non-derived item=" + item.NAME , item.NAME );
			
		ReleaseScopeSet set = getCategorySet( action , DBEnumScopeCategoryType.DERIVED );
		if( set.ALL )
			return( true );

		ReleaseScopeTarget target = set.findTarget( item.NAME );
		if( target != null )
			return( true );
		
		target = set.addDerivedItem( action , item );
		registerTarget( action , target );
		return( true );
	}

	public boolean isCompatible( ActionBase action , String RELEASEVER ) throws Exception {
		if( COMPATIBILITY.isEmpty() )
			return( true );
			
		if( Common.checkPartOfSpacedList( RELEASEVER , COMPATIBILITY ) )
			return( true );
		return( false );
	}

	public String[] getCumulativeVersions() {
		String versions = Common.getSortedUniqueSpacedList( COMPATIBILITY + " " + RELEASEVER );
		String[] list = Common.splitSpaced( versions );
		return( VersionInfo.orderVersions( list ) );
	}

	public void descopeAll( ActionBase action ) throws Exception {
		sourceSetMap.clear();
		categorySetMap.clear();
		deliveryMap.clear();
	}

	public void rebuildDeliveries( ActionBase action ) throws Exception {
		deliveryMap.clear();
		
		for( ReleaseScopeSet set : sourceSetMap.values() ) {
			for( ReleaseScopeTarget target : set.getTargets() ) {
				for( ReleaseScopeItem item : target.getItems() )
					registerTargetItem( action , item );
			}
		}
		
		for( ReleaseScopeSet set : categorySetMap.values() ) {
			for( ReleaseScopeTarget target : set.getTargets() )
				registerTarget( action , target );
		}
	}

	public DBEnumLifecycleType getLifecycleType() {
		if( MASTER )
			return( DBEnumLifecycleType.MAJOR );
		return( VersionInfo.getLifecycleTypeByFullVersion( RELEASEVER ) );
	}

	public void finish( ActionBase action ) throws Exception {
		schedule.finish( action );
	}
	
	public void complete( ActionBase action ) throws Exception {
		schedule.complete( action );
	}
	
	public void reopen( ActionBase action ) throws Exception {
		schedule.reopen( action );
	}

	public void addMasterItem( ActionBase action , Release src , MetaDistrBinaryItem distItem , DistItemInfo info ) throws Exception {
		master.addMasterItem( action , src , distItem , info );
	}

	public ReleaseMasterItem findMasterItem( MetaDistrBinaryItem distItem ) {
		return( master.findMasterItem( distItem ) );
	}
	
}
