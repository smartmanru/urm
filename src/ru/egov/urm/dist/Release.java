package ru.egov.urm.dist;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.meta.MetaSourceProjectItem;
import ru.egov.urm.meta.MetaSourceProjectSet;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.meta.Metadata.VarDISTITEMSOURCE;

public class Release {

	public Metadata meta;
	public Dist dist;
	
	public String RELEASEVER;
	public boolean PROPERTY_OBSOLETE;
	public VarBUILDMODE PROPERTY_BUILDMODE;
	public String PROPERTY_COMPATIBILITY;
	public boolean PROPERTY_CUMULATIVE;
	
	Map<String,ReleaseSet> sourceSetMap = new HashMap<String,ReleaseSet>();
	Map<VarCATEGORY,ReleaseSet> categorySetMap = new HashMap<VarCATEGORY,ReleaseSet>();
	Map<String,ReleaseDelivery> deliveryMap = new HashMap<String,ReleaseDelivery>();
	
	public Release( Metadata meta , Dist dist ) {
		this.meta = meta;
		this.dist = dist;
	}

	public void copy( ActionBase action , Release src ) throws Exception {
		this.meta = src.meta;
		this.RELEASEVER = src.RELEASEVER;
		this.PROPERTY_OBSOLETE = src.PROPERTY_OBSOLETE;
		this.PROPERTY_BUILDMODE = src.PROPERTY_BUILDMODE;
		this.PROPERTY_COMPATIBILITY = src.PROPERTY_COMPATIBILITY;
		this.PROPERTY_CUMULATIVE = src.PROPERTY_CUMULATIVE;
		
		sourceSetMap.clear();
		categorySetMap.clear();
		deliveryMap.clear();
		
		for( Entry<String,ReleaseSet> entry : src.sourceSetMap.entrySet() ) {
			ReleaseSet set = entry.getValue().copy( action , this );
			sourceSetMap.put( entry.getKey() , set );
			
			if( set.isCategorySet( action ) )
				categorySetMap.put( set.CATEGORY , set );
		}
		
		for( Entry<String,ReleaseDelivery> entry : src.deliveryMap.entrySet() ) {
			ReleaseDelivery set = entry.getValue().copy( action , this );
			deliveryMap.put( entry.getKey() , set );
		}
	}
	
	public void create( ActionBase action , String RELEASEVER , String RELEASEFILEPATH ) throws Exception {
		this.RELEASEVER = dist.repo.normalizeReleaseVer( action , RELEASEVER );
		setProperties( action );
		createEmptyXml( action , RELEASEFILEPATH );
	}

	public void setReleaseVer( ActionBase action , String RELEASEVER ) throws Exception {
		this.RELEASEVER = RELEASEVER;
	}
	
	public void setProperties( ActionBase action ) throws Exception {
		PROPERTY_BUILDMODE = action.context.CTX_BUILDMODE;
		PROPERTY_OBSOLETE = action.context.CTX_OBSOLETE;
		PROPERTY_CUMULATIVE = action.context.CTX_CUMULATIVE;
		
		if( action.context.CTX_ALL )
			PROPERTY_COMPATIBILITY = "";
		for( String OLDRELEASE : Common.splitSpaced( action.context.CTX_OLDRELEASE ) ) {
			OLDRELEASE = dist.repo.normalizeReleaseVer( action , OLDRELEASE );
			PROPERTY_COMPATIBILITY = Common.addItemToUniqueSpacedList( PROPERTY_COMPATIBILITY , OLDRELEASE );
		}
	}
	
	public void setProperties( ActionBase action , Release src ) throws Exception {
		PROPERTY_BUILDMODE = src.PROPERTY_BUILDMODE;
		PROPERTY_OBSOLETE = src.PROPERTY_OBSOLETE;
		PROPERTY_COMPATIBILITY = src.PROPERTY_COMPATIBILITY;
		PROPERTY_CUMULATIVE = src.PROPERTY_CUMULATIVE;
	}
	
	public void createProd( ActionBase action , String RELEASEVER , String filePath ) throws Exception {
		this.RELEASEVER = RELEASEVER;
		this.PROPERTY_BUILDMODE = VarBUILDMODE.MAJORBRANCH;
		this.PROPERTY_OBSOLETE = true;
		this.PROPERTY_CUMULATIVE = false;
		
		addSourceAll( action );
		addCategorySet( action , VarCATEGORY.MANUAL , true );
		
		Document doc = createXml( action );
		Common.xmlSaveDoc( doc , filePath );
	}
	
	public Map<String,ReleaseSet> getSourceSets( ActionBase action ) throws Exception {
		return( sourceSetMap );
	}
	
	public ReleaseSet findSourceSet( ActionBase action , String name ) throws Exception {
		return( sourceSetMap.get( name ) );
	}
	
	public ReleaseSet getSourceSet( ActionBase action , String name ) throws Exception {
		ReleaseSet set = findSourceSet( action , name );
		if( set == null )
			action.exit( "unknown release set=" + name );
		return( set );
	}
	
	public Map<VarCATEGORY,ReleaseSet> getCategorySets( ActionBase action ) throws Exception {
		return( categorySetMap );
	}
	
	public ReleaseSet findCategorySet( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		return( categorySetMap.get( CATEGORY ) );
	}
	
	public ReleaseSet getCategorySet( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		ReleaseSet set = findCategorySet( action , CATEGORY );
		if( set == null )
			action.exit( "unknown release category set=" + CATEGORY );
		return( set );
	}

	private void loadSets( ActionBase action , Node root , VarCATEGORY CATEGORY ) throws Exception {
		Node element = ConfReader.xmlGetFirstChild( action , root , Common.getEnumLower( CATEGORY ) );
		if( element == null )
			return;
		
		if( meta.isSourceCategory( action , CATEGORY ) ) {
			Node[] sets = ConfReader.xmlGetChildren( action , element , "set" );
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
			registerSet( action , set );
		}
			
	}
	
	public Document load( ActionBase action , String RELEASEFILEPATH ) throws Exception {
		// read xml
		String file = RELEASEFILEPATH;
		
		action.debug( "read release file " + file + "..." );
		Document doc = ConfReader.readXmlFile( action , file );
		Node root = doc.getDocumentElement();

		RELEASEVER = ConfReader.getAttrValue( action , root , "version" );
		if( RELEASEVER.isEmpty() )
			action.exit( "release version property is not set, unable to use distributive" );
		
		// properties
		PROPERTY_BUILDMODE = getReleasePropertyBuildMode( action , root , "buildMode" ); 
		PROPERTY_OBSOLETE = getReleasePropertyBoolean( action , root , "obsolete" , true );
		PROPERTY_COMPATIBILITY = getReleaseProperty( action , root , "over" );
		PROPERTY_CUMULATIVE = getReleasePropertyBoolean( action , root , "cumulative" , false );

		// get projectsets
		for( VarCATEGORY CATEGORY : meta.getAllReleaseCategories( action ) )
			loadSets( action , root , CATEGORY );
		
		return( doc );
	}

	private void registerSet( ActionBase action , ReleaseSet set ) throws Exception {
		action.trace( "add set=" + set.NAME + ", category=" + Common.getEnumLower( set.CATEGORY ) );
		if( meta.isSourceCategory( action , set.CATEGORY ) )
			sourceSetMap.put( set.NAME , set );
		else
			categorySetMap.put( set.CATEGORY , set );
		
		for( ReleaseTarget target : set.getTargets( action ).values() )
			registerTarget( action , target );
	}

	private void registerTargetItem( ActionBase action , ReleaseTargetItem item ) throws Exception {
		action.trace( "add item=" + item.NAME );
		ReleaseDelivery releaseDelivery = registerDelivery( action , item.getDelivery( action ) );
		releaseDelivery.addTargetItem( action , item );
	}
	
	private void registerTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		action.trace( "add target=" + target.NAME );
		if( meta.isSourceCategory( action , target.CATEGORY ) ) {
			for( ReleaseTargetItem item : target.getItems( action ).values() )
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
		Node prop = ConfReader.xmlGetNamedNode( action , node , "property" , name );
		if( prop == null )
			return( "" );
		
		String value = ConfReader.getAttrValue( action , prop , "value" );
		return( value );
	}

	private VarBUILDMODE getReleasePropertyBuildMode( ActionBase action , Node node , String name ) throws Exception {
		String value = getReleaseProperty( action , node , name );
		if( value.isEmpty() )
			return( VarBUILDMODE.UNKNOWN );
		return( VarBUILDMODE.valueOf( value.toUpperCase() ) );
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
		return( set.getTargets( action ).values().toArray( new ReleaseTarget[0] ) );
	}

	public ReleaseTarget[] getCategoryTargets( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		ReleaseSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( new ReleaseTarget[0] );
		return( set.getTargets( action ).values().toArray( new ReleaseTarget[0] ) );
	}

	public ReleaseTarget findBuildProject( ActionBase action , String name ) throws Exception {
		MetaSourceProject sourceProject = meta.sources.getProject( action , name );
		ReleaseSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return( null );
		
		ReleaseTarget project = set.findTarget( action , name );
		return( project );
	}
	
	public ReleaseTarget getBuildProject( ActionBase action , String name ) throws Exception {
		ReleaseTarget project = findBuildProject( action , name );
		if( project == null )
			action.exit( "unknown release project=" + name );
		
		return( project );
	}

	public ReleaseTarget findCategoryTarget( ActionBase action , VarCATEGORY CATEGORY , String KEY ) throws Exception {
		ReleaseSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( null );
		
		ReleaseTarget target = set.findTarget( action , KEY );
		return( target );
	}
	
	public ReleaseTarget findConfComponent( ActionBase action , String KEY ) throws Exception {
		return( findCategoryTarget( action , VarCATEGORY.CONFIG , KEY ) );
	}
	
	public ReleaseTarget getConfComponent( ActionBase action , String KEY ) throws Exception {
		ReleaseTarget comp = findConfComponent( action , KEY );
		if( comp == null )
			action.exit( "unknown release component=" + KEY );
		
		return( comp );
	}

	public Map<String,ReleaseTarget> getCategoryComponents( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		ReleaseSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( new HashMap<String,ReleaseTarget>() );
		
		return( set.map );
	}
	
	public Map<String,ReleaseTarget> getConfComponents( ActionBase action ) throws Exception {
		return( getCategoryComponents( action , VarCATEGORY.CONFIG ) );
	}

	public Map<String,ReleaseDelivery> getDeliveries( ActionBase action ) throws Exception {
		return( deliveryMap );
	}

	public ReleaseDelivery findDelivery( ActionBase action , String name ) throws Exception {
		ReleaseDelivery delivery = deliveryMap.get( name );
		return( delivery );
	}
	
	public ReleaseDelivery findDeliveryByFolder( ActionBase action , String folder ) throws Exception {
		for( ReleaseDelivery delivery : deliveryMap.values() ) {
			if( delivery.distDelivery.FOLDER.equals( folder ) )
				return( delivery );
		}
		return( null );
	}
	
	public ReleaseDelivery getDelivery( ActionBase action , String name ) throws Exception {
		ReleaseDelivery delivery = deliveryMap.get( name );
		if( delivery == null )
			action.exit( "unknown delivery folder=" + name );
		
		return( delivery );
	}
	
	public ReleaseDelivery getDeliveryByFolder( ActionBase action , String folder ) throws Exception {
		for( ReleaseDelivery delivery : deliveryMap.values() )
			if( delivery.distDelivery.FOLDER.equals( folder ) )
				return( delivery );
		
		action.exit( "unknown delivery folder=" + folder );
		return( null );
	}
	
	public boolean isEmpty( ActionBase action ) throws Exception {
		for( ReleaseSet set : sourceSetMap.values() )
			if( !set.isEmpty( action ) )
				return( false );
		for( ReleaseSet set : categorySetMap.values() )
			if( !set.isEmpty( action ) )
				return( false );
		return( true );
	}

	public boolean isEmptyConfiguration( ActionBase action ) throws Exception {
		ReleaseSet set = findCategorySet( action , VarCATEGORY.CONFIG );
		if( set == null || set.isEmpty( action ) )
			return( true );
		
		return( false );
	}
	
	public boolean isEmptyDatabase( ActionBase action ) throws Exception {
		ReleaseSet set = findCategorySet( action , VarCATEGORY.DB );
		if( set == null || set.isEmpty( action ) )
			return( true );
		
		return( false );
	}
	
	public Document createEmptyXmlDoc( ActionBase action ) throws Exception {
		Document doc = Common.xmlCreateDoc( "release" );
		Element root = doc.getDocumentElement();
		Common.xmlSetElementAttr( doc , root , "version" , RELEASEVER );
		Common.xmlCreatePropertyElement( doc , root , "buildMode" , Common.getEnumLower( PROPERTY_BUILDMODE ) );
		Common.xmlCreateBooleanPropertyElement( doc , root , "obsolete" , PROPERTY_OBSOLETE );
		Common.xmlCreatePropertyElement( doc , root , "over" , PROPERTY_COMPATIBILITY );
		Common.xmlCreateBooleanPropertyElement( doc , root , "cumulative" , PROPERTY_CUMULATIVE );
		
		for( VarCATEGORY CATEGORY : action.meta.getAllReleaseCategories( action ) )
			Common.xmlCreateElement( doc , root , Common.getEnumLower( CATEGORY ) );
		return( doc );
	}
	
	public void createEmptyXml( ActionBase action , String filePath ) throws Exception {
		Document doc = createEmptyXmlDoc( action );
		Common.xmlSaveDoc( doc , filePath );
	}

	public Document createXml( ActionBase action ) throws Exception {
		Document doc = createEmptyXmlDoc( action );
		Element root = doc.getDocumentElement();
		
		for( ReleaseSet set : sourceSetMap.values() ) {
			Element parent = ( Element )ConfReader.xmlGetFirstChild( action , root , Common.getEnumLower( set.CATEGORY ) );
			set.createXml( action , doc , parent );
		}

		for( ReleaseSet set : categorySetMap.values() ) {
			Element parent = ( Element )ConfReader.xmlGetFirstChild( action , root , Common.getEnumLower( set.CATEGORY ) );
			set.createXml( action , doc , parent );
		}
			
		return( doc );
	}

	public void addSourceAll( ActionBase action ) throws Exception {
		for( MetaSourceProjectSet sourceSet : meta.sources.getSets( action ).values() )
			addSourceSet( action , sourceSet , true );
	}
	
	public boolean addSourceSet( ActionBase action , MetaSourceProjectSet sourceSet , boolean all ) throws Exception {
		ReleaseSet set = findSourceSet( action , sourceSet.NAME );
		if( set == null ) {
			set = new ReleaseSet( meta , this , sourceSet.CATEGORY );
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
	
	public boolean addCategorySet( ActionBase action , VarCATEGORY CATEGORY , boolean all ) throws Exception {
		ReleaseSet set = findCategorySet( action , CATEGORY );
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

	public void deleteCategorySet( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		ReleaseSet set = findCategorySet( action , CATEGORY );
		if( set == null )
			return;
		
		unregisterSet( action , set );
	}

	public void deleteSourceSet( ActionBase action , MetaSourceProjectSet sourceSet ) throws Exception {
		ReleaseSet set = findSourceSet( action , sourceSet.NAME );
		if( set == null )
			return;
		
		unregisterSet( action , set );
	}

	private void unregisterTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		if( meta.isSourceCategory( action , target.CATEGORY ) ) {
			for( ReleaseTargetItem item : target.getItems( action ).values() )
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
		for( ReleaseTarget project : set.getTargets( action ).values() )
			unregisterTarget( action , project );
		
		if( meta.isSourceCategory( action , set.CATEGORY ) )
			sourceSetMap.remove( set.NAME );
		else
			categorySetMap.remove( set.CATEGORY );
	}
	
	public boolean addProject( ActionBase action , MetaSourceProject sourceProject , boolean allItems ) throws Exception {
		ReleaseSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return( false );
		
		ReleaseTarget project = set.findTarget( action , sourceProject.PROJECT );
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
	
	public void deleteCategoryTarget( ActionBase action , VarCATEGORY CATEGORY , String NAME ) throws Exception {
		ReleaseSet set = getCategorySet( action , CATEGORY );
		if( set == null )
			return;

		ReleaseTarget target = set.findTarget( action , NAME );
		if( target == null )
			return;

		deleteTarget( action , target );
	}
	
	public void deleteProjectSource( ActionBase action , MetaSourceProject sourceProject ) throws Exception {
		ReleaseSet set = findSourceSet( action , sourceProject.set.NAME );
		if( set == null )
			return;
		
		ReleaseTarget target = set.findTarget( action , sourceProject.PROJECT );
		if( target == null )
			return;
		
		deleteTarget( action , target );
	}

	public boolean addProjectItem( ActionBase action , MetaSourceProject sourceProject , MetaSourceProjectItem sourceItem ) throws Exception {
		if( sourceItem.INTERNAL )
			action.exit( "unexpected call for INTERNAL item=" + sourceItem.ITEMNAME );
		
		ReleaseSet set = sourceSetMap.get( sourceProject.CATEGORY );
		if( set == null )
			return( false );
		
		if( set.ALL )
			return( true );
		
		ReleaseTarget project = set.findTarget( action , sourceProject.PROJECT );
		if( project == null )
			return( false );

		if( project.ALL )
			return( true );
		
		ReleaseTargetItem item = project.getItem( action , sourceItem.ITEMNAME );
		if( item != null ) {
			if( !item.checkPropsEqualsToOptions( action ) )
				return( false );
		}
		
		item = project.addSourceItem( action , sourceItem );
		registerTargetItem( action , item );
		return( true );
	}

	public void deleteProjectItem( ActionBase action , ReleaseTargetItem item ) throws Exception {
		item.target.set.makePartial( action );
		item.target.removeSourceItem( action , item );
		unregisterTargetItem( action , item );
	}
	
	public void deleteProjectItem( ActionBase action , VarCATEGORY CATEGORY , MetaSourceProject sourceProject , MetaSourceProjectItem sourceItem ) throws Exception {
		ReleaseSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return;

		ReleaseTarget project = set.findTarget( action , sourceProject.PROJECT );
		if( project == null )
			return;
		
		ReleaseTargetItem item = project.getItem( action , sourceItem.ITEMNAME );
		if( item == null )
			return;

		deleteProjectItem( action , item );
	}
	
	public boolean addConfItem( ActionBase action , MetaDistrConfItem item ) throws Exception {
		ReleaseSet set = getCategorySet( action , VarCATEGORY.CONFIG );
		if( set.ALL )
			return( true );

		ReleaseTarget target = set.findTarget( action , item.KEY );
		if( target != null ) {
			if( !target.ALL )
				target.setAll( action , action.context.CTX_REPLACE );
			return( true );
		}
		
		target = set.addConfItem( action , item , action.context.CTX_REPLACE );
		registerTarget( action , target );
		return( true );
	}

	public boolean addDatabaseItem( ActionBase action , MetaDistrDelivery item ) throws Exception {
		if( !item.hasDatabaseItems( action ) ) {
			action.error( "no database items in delivery=" + item.NAME );
			return( false );
		}
		
		ReleaseSet set = getCategorySet( action , VarCATEGORY.DB );
		if( set.ALL )
			return( true );

		ReleaseTarget target = set.findTarget( action , item.NAME );
		if( target != null )
			return( true );
		
		target = set.addDatabaseItem( action , item );
		registerTarget( action , target );
		return( true );
	}

	public boolean addManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.DISTSOURCE != VarDISTITEMSOURCE.MANUAL )
			action.exit( "unexpected non-manual item=" + item.KEY );
			
		ReleaseSet set = getCategorySet( action , VarCATEGORY.MANUAL );
		if( set.ALL )
			return( true );

		ReleaseTarget target = set.findTarget( action , item.KEY );
		if( target != null )
			return( true );
		
		target = set.addManualItem( action , item );
		registerTarget( action , target );
		return( true );
	}

	public boolean isCompatible( ActionBase action , String RELEASEVER ) throws Exception {
		if( PROPERTY_COMPATIBILITY.isEmpty() )
			return( true );
			
		if( Common.checkPartOfSpacedList( RELEASEVER , PROPERTY_COMPATIBILITY ) )
			return( true );
		return( false );
	}

	public String[] getCumulativeVersions( ActionBase action ) throws Exception {
		String versions = Common.getSortedUniqueSpacedList( PROPERTY_COMPATIBILITY );
		versions = Common.getPartAfterFirst( versions , " " );
		return( Common.splitSpaced( versions ) );
	}
	
}
