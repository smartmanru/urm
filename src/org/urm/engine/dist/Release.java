package org.urm.engine.dist;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.Types;
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
import org.urm.meta.Types.*;
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
	
	Map<String,ReleaseSet> sourceSetMap = new HashMap<String,ReleaseSet>();
	Map<EnumScopeCategory,ReleaseSet> categorySetMap = new HashMap<EnumScopeCategory,ReleaseSet>();
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

	public static String PROPERTY_VERSION = "version";
	public static String PROPERTY_MASTER = "master";
	public static String PROPERTY_BUILDMODE = "mode"; 
	public static String PROPERTY_OBSOLETE = "obsolete";
	public static String PROPERTY_COMPATIBILITY = "over";
	public static String PROPERTY_CUMULATIVE = "cumulative";
	
	public static String PROPERTY_BUILDBRANCH = "buildbranch";
	public static String PROPERTY_BUILDTAG = "buildtag";
	public static String PROPERTY_BUILDVERSION = "buildversion";
	public static String PROPERTY_PARTIAL = "partial";
	public static String PROPERTY_ALL = "all";
	
	public static String PROPERTY_RELEASE = "release";
	public static String PROPERTY_DATEADDED = "added";
	public static String PROPERTY_KEY = "key";
	public static String PROPERTY_DELIVERY = "delivery";
	public static String PROPERTY_FOLDER = "folder";
	public static String PROPERTY_FILE = "folder";
	public static String PROPERTY_MD5 = "md5";
	public static String PROPERTY_LIFECYCLE = "lifecycle";
	public static String PROPERTY_STARTED = "started";
	public static String PROPERTY_RELEASEDATE = "releasedate";
	public static String PROPERTY_RELEASEDATEACTUAL = "releaseactual";
	public static String PROPERTY_COMPLETEDATEACTUAL = "completeactual";
	public static String PROPERTY_PHASE = "phase";
	public static String PROPERTY_RELEASEDSTATUS = "released";
	public static String PROPERTY_COMPLETEDSTATUS = "completed";
	public static String PROPERTY_ARCHIVEDSTATUS = "archived";
	public static String PROPERTY_DAYS = "days";
	public static String PROPERTY_NORMALDAYS = "normaldays";
	public static String PROPERTY_UNLIMITED = "unlimited";
	public static String PROPERTY_STARTDATE = "startdate";
	public static String PROPERTY_RELEASESTAGE = "release";
	public static String PROPERTY_FINISHED = "finished";
	public static String PROPERTY_FINISHDATE = "finishdate";
	
	public static String PROPERTY_TICKETSETCODE = "code";
	public static String PROPERTY_TICKETSETCOMMENTS = "comments";
	public static String PROPERTY_TICKETSETSTATUS = "status";
	
	public static String PROPERTY_TICKETCODE = "code";
	public static String PROPERTY_TICKETCOMMENTS = "comments";
	public static String PROPERTY_TICKETLINK = "link";
	public static String PROPERTY_TICKETOWNER = "owner";
	public static String PROPERTY_TICKETDEV = "dev";
	public static String PROPERTY_TICKETQA = "qa";
	public static String PROPERTY_TICKETTYPE = "type";
	public static String PROPERTY_TICKETSTATUS = "status";
	public static String PROPERTY_TICKETACTIVE = "active";
	public static String PROPERTY_TICKETACCEPTED = "accepted";
	public static String PROPERTY_TICKETDESCOPED = "descoped";

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
		for( Entry<String,ReleaseSet> entry : src.sourceSetMap.entrySet() ) {
			ReleaseSet set = entry.getValue().copy( action , this );
			sourceSetMap.put( entry.getKey() , set );
		}
		
		for( Entry<EnumScopeCategory,ReleaseSet> entry : src.categorySetMap.entrySet() ) {
			ReleaseSet set = entry.getValue().copy( action , this );
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
		
		for( Entry<String,ReleaseSet> entry : src.sourceSetMap.entrySet() ) {
			ReleaseSet set = sourceSetMap.get( entry.getKey() );
			ReleaseSet srcset = entry.getValue();
			if( set == null ) {
				set = srcset.copy( action , this );
				sourceSetMap.put( entry.getKey() , set );
			}
			else
				set.addReleaseSet( action , srcset );
		}
		
		for( Entry<EnumScopeCategory,ReleaseSet> entry : src.categorySetMap.entrySet() ) {
			ReleaseSet set = categorySetMap.get( entry.getKey() );
			ReleaseSet srcset = entry.getValue();
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
	
	public ReleaseSet[] getSourceSets() {
		return( sourceSetMap.values().toArray( new ReleaseSet[0] ) );
	}
	
	public ReleaseSet findSourceSet( String name ) {
		return( sourceSetMap.get( name ) );
	}
	
	public ReleaseSet getSourceSet( ActionBase action , String name ) throws Exception {
		ReleaseSet set = findSourceSet( name );
		if( set == null )
			action.exit1( _Error.UnknownReleaseSet1 , "unknown release set=" + name , name );
		return( set );
	}
	
	public Map<EnumScopeCategory,ReleaseSet> getCategorySets( ActionBase action ) throws Exception {
		return( categorySetMap );
	}
	
	public ReleaseSet findCategorySet( EnumScopeCategory CATEGORY ) {
		return( categorySetMap.get( CATEGORY ) );
	}
	
	public ReleaseSet getCategorySet( ActionBase action , EnumScopeCategory CATEGORY ) throws Exception {
		ReleaseSet set = findCategorySet( CATEGORY );
		if( set == null ) {
			String name = Common.getEnumLower( CATEGORY );
			action.exit1( _Error.UnknownReleaseCategorySet1 , "unknown release category set=" + name , name );
		}
		return( set );
	}

	private void loadSets( ActionBase action , Node root , EnumScopeCategory CATEGORY ) throws Exception {
		Node element = ConfReader.xmlGetFirstChild( root , Common.getEnumLower( CATEGORY ) );
		if( element == null )
			return;
		
		if( Types.isSourceCategory( CATEGORY ) ) {
			Node[] sets = ConfReader.xmlGetChildren( element , ELEMENT_SET );
			if( sets == null )
				return;
			
			for( Node node : sets ) {
				ReleaseSet set = new ReleaseSet( meta , this , CATEGORY );
				set.load( action , node );
				registerSet( action , set );
			}
		}
		else {
			ReleaseSet set = new ReleaseSet( meta , this , CATEGORY );
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
			for( EnumScopeCategory CATEGORY : Types.getAllReleaseCategories() )
				loadSets( action , root , CATEGORY );
			changes = new ReleaseChanges( meta , this );
			changes.load( action , root );
		}
		
		return( doc );
	}

	private void registerSet( ActionBase action , ReleaseSet set ) throws Exception {
		action.trace( "add set=" + set.NAME + ", category=" + Common.getEnumLower( set.CATEGORY ) );
		if( Types.isSourceCategory( set.CATEGORY ) )
			sourceSetMap.put( set.NAME , set );
		else
			categorySetMap.put( set.CATEGORY , set );
		
		for( ReleaseTarget target : set.getTargets() )
			registerTarget( action , target );
	}

	private void registerTargetItem( ActionBase action , ReleaseTargetItem item ) throws Exception {
		action.trace( "add item=" + item.NAME );
		ReleaseDelivery releaseDelivery = registerDelivery( action , item.getDelivery( action ) );
		releaseDelivery.addTargetItem( action , item );
	}
	
	private void registerTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		action.trace( "add target=" + target.NAME );
		if( Types.isSourceCategory( target.CATEGORY ) ) {
			for( ReleaseTargetItem item : target.getItems() )
				registerTargetItem( action , item );
		}
		else
		if( target.isDatabaseTarget() ) {
			for( ReleaseTargetItem item : target.getItems() )
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

	private void unregisterTargetItem( ActionBase action , ReleaseTargetItem item ) throws Exception {
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
	
	public ReleaseTarget[] getSourceTargets( ActionBase action , String setName ) throws Exception {
		ReleaseSet set = sourceSetMap.get( setName );
		if( set == null )
			return( new ReleaseTarget[0] );
		return( set.getTargets() );
	}

	public ReleaseTarget[] getCategoryTargets( ActionBase action , EnumScopeCategory CATEGORY ) throws Exception {
		ReleaseSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( new ReleaseTarget[0] );
		return( set.getTargets() );
	}

	public ReleaseTarget findBuildProject( ActionBase action , String name ) throws Exception {
		MetaSources sources = meta.getSources(); 
		MetaSourceProject sourceProject = sources.getProject( name );
		ReleaseSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return( null );
		
		ReleaseTarget project = set.findTarget( name );
		return( project );
	}
	
	public ReleaseTarget getBuildProject( ActionBase action , String name ) throws Exception {
		ReleaseTarget project = findBuildProject( action , name );
		if( project == null )
			action.exit1( _Error.UnknownReleaseProject1 , "unknown release project=" + name , name );
		
		return( project );
	}

	public ReleaseTarget findCategoryTarget( ActionBase action , EnumScopeCategory CATEGORY , String KEY ) throws Exception {
		ReleaseSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( null );
		
		ReleaseTarget target = set.findTarget( KEY );
		return( target );
	}
	
	public ReleaseTarget findConfComponent( ActionBase action , String KEY ) throws Exception {
		return( findCategoryTarget( action , EnumScopeCategory.CONFIG , KEY ) );
	}
	
	public ReleaseTarget getConfComponent( ActionBase action , String KEY ) throws Exception {
		ReleaseTarget comp = findConfComponent( action , KEY );
		if( comp == null )
			action.exit1( _Error.UnknownReleaseComponent1 , "unknown release component=" + KEY , KEY );
		
		return( comp );
	}

	public ReleaseTarget[] getCategoryTargets( EnumScopeCategory CATEGORY ) {
		ReleaseSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( new ReleaseTarget[0] );
		
		return( set.getTargets() );
	}
	
	public ReleaseTarget[] getConfComponents() {
		return( getCategoryTargets( EnumScopeCategory.CONFIG ) );
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
		for( ReleaseSet set : sourceSetMap.values() )
			if( !set.isEmpty() )
				return( false );
		for( ReleaseSet set : categorySetMap.values() )
			if( !set.isEmpty() )
				return( false );
		return( true );
	}

	public boolean isEmptyConfiguration() throws Exception {
		ReleaseSet set = findCategorySet( EnumScopeCategory.CONFIG );
		if( set == null || set.isEmpty() )
			return( true );
		
		return( false );
	}
	
	public boolean isEmptyDatabase() {
		ReleaseSet set = findCategorySet( EnumScopeCategory.DB );
		if( set == null || set.isEmpty() )
			return( true );
		
		return( false );
	}
	
	public boolean isEmptyDoc() {
		ReleaseSet set = findCategorySet( EnumScopeCategory.DOC );
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
		
		for( EnumScopeCategory CATEGORY : Types.getAllReleaseCategories() )
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
			for( ReleaseSet set : sourceSetMap.values() ) {
				Element parent = ( Element )ConfReader.xmlGetFirstChild( root , Common.getEnumLower( set.CATEGORY ) );
				set.createXml( action , doc , parent );
			}
	
			for( ReleaseSet set : categorySetMap.values() ) {
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
		ReleaseSet set = findSourceSet( sourceSet.NAME );
		if( set == null ) {
			set = new ReleaseSet( meta , this , EnumScopeCategory.PROJECT );
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
	
	public boolean addCategorySet( ActionBase action , EnumScopeCategory CATEGORY , boolean all ) throws Exception {
		ReleaseSet set = findCategorySet( CATEGORY );
		if( set == null ) {
			set = new ReleaseSet( meta , this , CATEGORY );
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

	public void deleteCategorySet( ActionBase action , EnumScopeCategory CATEGORY ) throws Exception {
		ReleaseSet set = findCategorySet( CATEGORY );
		if( set == null )
			return;
		
		unregisterSet( action , set );
	}

	public void deleteSourceSet( ActionBase action , MetaSourceProjectSet sourceSet ) throws Exception {
		ReleaseSet set = findSourceSet( sourceSet.NAME );
		if( set == null )
			return;
		
		unregisterSet( action , set );
	}

	private void unregisterTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		if( Types.isSourceCategory( target.CATEGORY ) ) {
			for( ReleaseTargetItem item : target.getItems() )
				unregisterTargetItem( action , item );
		}
		else
		if( target.isDatabaseTarget() ) {
			for( ReleaseTargetItem item : target.getItems() )
				unregisterTargetItem( action , item );
		}
		else
		if( target.isDocTarget() ) {
			for( ReleaseTargetItem item : target.getItems() )
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

	private void unregisterSet( ActionBase action , ReleaseSet set ) throws Exception {
		for( ReleaseTarget project : set.getTargets() )
			unregisterTarget( action , project );
		
		if( Types.isSourceCategory( set.CATEGORY ) )
			sourceSetMap.remove( set.NAME );
		else
			categorySetMap.remove( set.CATEGORY );
	}

	public boolean addDatabaseDelivery( ActionBase action , MetaDistrDelivery delivery , boolean allSchemes ) throws Exception {
		ReleaseSet set = getCategorySet( action , EnumScopeCategory.DB );
		if( set == null )
			return( false );
		
		ReleaseTarget target = set.findTarget( delivery.NAME );
		if( target == null ) {
			target = set.addDatabaseDelivery( action , delivery , allSchemes );
			registerTarget( action , target );
			return( true );
		}
		
		if( allSchemes == true && target.ALL == false ) {
			deleteDatabaseDelivery( action , delivery );
			addDatabaseDelivery( action , delivery , true );
			return( true );
		}
		
		return( true );
	}
	
	public boolean addDocDelivery( ActionBase action , MetaDistrDelivery delivery , boolean allDocs ) throws Exception {
		ReleaseSet set = getCategorySet( action , EnumScopeCategory.DOC );
		if( set == null )
			return( false );
		
		ReleaseTarget target = set.findTarget( delivery.NAME );
		if( target == null ) {
			target = set.addDocDelivery( action , delivery , allDocs );
			registerTarget( action , target );
			return( true );
		}
		
		if( allDocs == true && target.ALL == false ) {
			deleteDocDelivery( action , delivery );
			addDocDelivery( action , delivery , true );
			return( true );
		}
		
		return( true );
	}
	
	public boolean addProject( ActionBase action , MetaSourceProject sourceProject , boolean allItems ) throws Exception {
		ReleaseSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return( false );
		
		ReleaseTarget project = set.findTarget( sourceProject.NAME );
		if( project == null ) {
			project = set.addSourceProject( action , sourceProject , allItems );
			registerTarget( action , project );
			return( true );
		}
		
		if( !project.checkPropsEqualsToOptions( action ) )
			return( false );
			
		if( allItems == true && project.ALL == false ) {
			deleteProjectSource( action , sourceProject );
			addProject( action , sourceProject , true );
			return( true );
		}
		
		return( true );
	}

	public void deleteTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		unregisterTarget( action , target );
		target.set.removeTarget( action , target );
	}
	
	public void deleteCategoryTarget( ActionBase action , EnumScopeCategory CATEGORY , String NAME ) throws Exception {
		ReleaseSet set = getCategorySet( action , CATEGORY );
		if( set == null )
			return;

		ReleaseTarget target = set.findTarget( NAME );
		if( target == null )
			return;

		deleteTarget( action , target );
	}
	
	public void deleteProjectSource( ActionBase action , MetaSourceProject sourceProject ) throws Exception {
		ReleaseSet set = findSourceSet( sourceProject.set.NAME );
		if( set == null )
			return;
		
		ReleaseTarget target = set.findTarget( sourceProject.NAME );
		if( target == null )
			return;
		
		deleteTarget( action , target );
	}

	public void deleteDatabaseDelivery( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		ReleaseSet set = getCategorySet( action , EnumScopeCategory.DB );
		if( set == null )
			return;
		
		ReleaseTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return;
		
		deleteTarget( action , target );
	}

	public void deleteDocDelivery( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		ReleaseSet set = getCategorySet( action , EnumScopeCategory.DOC );
		if( set == null )
			return;
		
		ReleaseTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return;
		
		deleteTarget( action , target );
	}

	public boolean addProjectItem( ActionBase action , MetaSourceProject sourceProject , MetaSourceProjectItem sourceItem ) throws Exception {
		if( sourceItem.isInternal() )
			action.exit1( _Error.UnexpectedInternalItem1 , "unexpected call for INTERNAL item=" + sourceItem.NAME , sourceItem.NAME );
		
		ReleaseSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return( false );
		
		if( set.ALL )
			return( true );
		
		ReleaseTarget project = set.findTarget( sourceProject.NAME );
		if( project == null )
			return( false );

		if( project.ALL )
			return( true );
		
		for( ReleaseTargetItem item : project.addSourceItem( action , sourceItem ) )
			registerTargetItem( action , item );
		return( true );
	}

	public boolean addDatabaseSchema( ActionBase action , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		ReleaseSet set = getCategorySet( action , EnumScopeCategory.DB );
		if( set == null )
			return( false );
		
		if( set.ALL )
			return( true );
		
		ReleaseTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return( false );

		if( target.ALL )
			return( true );
		
		ReleaseTargetItem item = target.addDeliverySchema( action , schema );
		registerTargetItem( action , item );
		return( true );
	}

	public boolean addDoc( ActionBase action , MetaDistrDelivery delivery , MetaProductDoc doc ) throws Exception {
		ReleaseSet set = getCategorySet( action , EnumScopeCategory.DOC );
		if( set == null )
			return( false );
		
		if( set.ALL )
			return( true );
		
		ReleaseTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return( false );

		if( target.ALL )
			return( true );
		
		ReleaseTargetItem item = target.addDeliveryDoc( action , doc );
		registerTargetItem( action , item );
		return( true );
	}

	public void deleteProjectItem( ActionBase action , ReleaseTargetItem item ) throws Exception {
		item.target.set.makePartial( action );
		item.target.removeSourceItem( action , item );
		unregisterTargetItem( action , item );
	}
	
	public void deleteDatabaseSchema( ActionBase action , ReleaseTargetItem item ) throws Exception {
		item.target.set.makePartial( action );
		item.target.removeDatabaseItem( action , item );
		unregisterTargetItem( action , item );
	}
	
	public void deleteDoc( ActionBase action , ReleaseTargetItem item ) throws Exception {
		item.target.set.makePartial( action );
		item.target.removeDocItem( action , item );
		unregisterTargetItem( action , item );
	}
	
	public void deleteProjectItem( ActionBase action , MetaSourceProject sourceProject , MetaSourceProjectItem sourceItem ) throws Exception {
		ReleaseSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return;

		ReleaseTarget project = set.findTarget( sourceProject.NAME );
		if( project == null )
			return;
		
		ReleaseTargetItem item = project.findProjectItem( sourceItem );
		if( item == null )
			return;

		deleteProjectItem( action , item );
	}
	
	public void deleteDatabaseSchema( ActionBase action , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		ReleaseSet set = getCategorySet( action , EnumScopeCategory.DB );
		if( set == null )
			return;

		ReleaseTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return;
		
		ReleaseTargetItem item = target.findDeliverySchema( schema );
		if( item == null )
			return;

		deleteDatabaseSchema( action , item );
	}
	
	public void deleteDoc( ActionBase action , MetaDistrDelivery delivery , MetaProductDoc doc ) throws Exception {
		ReleaseSet set = getCategorySet( action , EnumScopeCategory.DOC );
		if( set == null )
			return;

		ReleaseTarget target = set.findTarget( delivery.NAME );
		if( target == null )
			return;
		
		ReleaseTargetItem item = target.findDeliveryDoc( doc );
		if( item == null )
			return;

		deleteDoc( action , item );
	}
	
	public boolean addConfItem( ActionBase action , MetaDistrConfItem item ) throws Exception {
		ReleaseSet set = getCategorySet( action , EnumScopeCategory.CONFIG );
		if( set.ALL )
			return( true );

		ReleaseTarget target = set.findTarget( item.NAME );
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
			
		ReleaseSet set = getCategorySet( action , EnumScopeCategory.MANUAL );
		if( set.ALL )
			return( true );

		ReleaseTarget target = set.findTarget( item.NAME );
		if( target != null )
			return( true );
		
		target = set.addManualItem( action , item );
		registerTarget( action , target );
		return( true );
	}

	public boolean addDerivedItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.ITEMORIGIN_TYPE != DBEnumItemOriginType.DERIVED )
			action.exit1( _Error.UnexpectedNonManualItem1 , "unexpected non-derived item=" + item.NAME , item.NAME );
			
		ReleaseSet set = getCategorySet( action , EnumScopeCategory.DERIVED );
		if( set.ALL )
			return( true );

		ReleaseTarget target = set.findTarget( item.NAME );
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
		
		for( ReleaseSet set : sourceSetMap.values() ) {
			for( ReleaseTarget target : set.getTargets() ) {
				for( ReleaseTargetItem item : target.getItems() )
					registerTargetItem( action , item );
			}
		}
		
		for( ReleaseSet set : categorySetMap.values() ) {
			for( ReleaseTarget target : set.getTargets() )
				registerTarget( action , target );
		}
	}

	public DBEnumLifecycleType getLifecycleType() {
		if( MASTER )
			return( DBEnumLifecycleType.MAJOR );
		return( VersionInfo.getLifecycleType( RELEASEVER ) );
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
