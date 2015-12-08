package ru.egov.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ru.egov.urm.Common;
import ru.egov.urm.ConfReader;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.run.ActionBase;

public class MetaRelease {

	public Metadata meta;
	
	public String RELEASEVER;
	public boolean PROPERTY_OBSOLETE;
	public VarBUILDMODE PROPERTY_BUILDMODE;
	
	Map<String,MetaReleaseSet> sourceSetMap = new HashMap<String,MetaReleaseSet>();
	Map<VarCATEGORY,MetaReleaseSet> categorySetMap = new HashMap<VarCATEGORY,MetaReleaseSet>();
	Map<String,MetaReleaseDelivery> deliveryMap = new HashMap<String,MetaReleaseDelivery>();
	
	public MetaRelease( Metadata meta ) {
		this.meta = meta;
	}

	public void create( ActionBase action , String RELEASEVER , VarBUILDMODE BUILDMODE , String RELEASEFILEPATH , boolean obsolete ) throws Exception {
		this.RELEASEVER = RELEASEVER;
		this.PROPERTY_BUILDMODE = BUILDMODE;
		this.PROPERTY_OBSOLETE = obsolete;
		createEmptyXml( action , BUILDMODE , obsolete , RELEASEFILEPATH );
	}
	
	public Map<String,MetaReleaseSet> getSourceSets( ActionBase action ) throws Exception {
		return( sourceSetMap );
	}
	
	public MetaReleaseSet findSourceSet( ActionBase action , String name ) throws Exception {
		return( sourceSetMap.get( name ) );
	}
	
	public MetaReleaseSet getSourceSet( ActionBase action , String name ) throws Exception {
		MetaReleaseSet set = findSourceSet( action , name );
		if( set == null )
			action.exit( "unknown release set=" + name );
		return( set );
	}
	
	public Map<VarCATEGORY,MetaReleaseSet> getCategorySets( ActionBase action ) throws Exception {
		return( categorySetMap );
	}
	
	public MetaReleaseSet findCategorySet( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		return( categorySetMap.get( CATEGORY ) );
	}
	
	public MetaReleaseSet getCategorySet( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		MetaReleaseSet set = findCategorySet( action , CATEGORY );
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
				MetaReleaseSet set = new MetaReleaseSet( meta , CATEGORY );
				set.load( action , node );
				registerSet( action , set );
			}
		}
		else {
			MetaReleaseSet set = new MetaReleaseSet( meta , CATEGORY );
			set.load( action , element );
			registerSet( action , set );
		}
			
	}
	
	public Document load( ActionBase action , String RELEASEVER , String RELEASEFILEPATH ) throws Exception {
		this.RELEASEVER = RELEASEVER; 
		
		// read xml
		String file = RELEASEFILEPATH;
		
		action.debug( "read release file " + file + "..." );
		Document doc = ConfReader.readXmlFile( action , file );
		Node root = doc.getDocumentElement();
		
		// properties
		PROPERTY_BUILDMODE = getReleasePropertyBuildMode( action , root , "buildMode" ); 
		PROPERTY_OBSOLETE = getReleasePropertyBoolean( action , root , "obsolete" , true );

		// get projectsets
		for( VarCATEGORY CATEGORY : meta.getAllReleaseCategories( action ) )
			loadSets( action , root , CATEGORY );
		
		return( doc );
	}

	private void registerSet( ActionBase action , MetaReleaseSet set ) throws Exception {
		action.trace( "add set=" + set.NAME + ", category=" + Common.getEnumLower( set.CATEGORY ) );
		if( meta.isSourceCategory( action , set.CATEGORY ) )
			sourceSetMap.put( set.NAME , set );
		else
			categorySetMap.put( set.CATEGORY , set );
		
		for( MetaReleaseTarget target : set.getTargets( action ).values() )
			registerTarget( action , target );
	}

	private void registerTargetItem( ActionBase action , MetaReleaseTargetItem item ) throws Exception {
		action.trace( "add item=" + item.NAME );
		MetaReleaseDelivery releaseDelivery = registerDelivery( action , item.getDelivery( action ) );
		releaseDelivery.addTargetItem( action , item );
	}
	
	private void registerTarget( ActionBase action , MetaReleaseTarget target ) throws Exception {
		action.trace( "add target=" + target.NAME );
		if( meta.isSourceCategory( action , target.CATEGORY ) ) {
			for( MetaReleaseTargetItem item : target.getItems( action ).values() )
				registerTargetItem( action , item );
		}
		else {
			MetaReleaseDelivery releaseDelivery = registerDelivery( action , target.getDelivery( action ) );
			releaseDelivery.addCategoryTarget( action , target );
		}
	}
	
	private MetaReleaseDelivery registerDelivery( ActionBase action , MetaDistrDelivery distDelivery ) throws Exception {
		action.trace( "add delivery=" + distDelivery.NAME );
		MetaReleaseDelivery releaseDelivery = deliveryMap.get( distDelivery.NAME );
		if( releaseDelivery == null ) {
			releaseDelivery = new MetaReleaseDelivery( this , distDelivery );
			deliveryMap.put( distDelivery.NAME , releaseDelivery );
		}
		return( releaseDelivery );
	}

	private void unregisterTargetItem( ActionBase action , MetaReleaseTargetItem item ) throws Exception {
		MetaDistrDelivery distDelivery = item.getDelivery( action );
		if( distDelivery == null )
			return;
		
		MetaReleaseDelivery releaseDelivery = deliveryMap.get( distDelivery.NAME );
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
			action.exit( "build mode is empty - attribute " + name );
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
	
	public MetaReleaseTarget[] getSourceTargets( ActionBase action , String setName ) throws Exception {
		MetaReleaseSet set = sourceSetMap.get( setName );
		if( set == null )
			return( new MetaReleaseTarget[0] );
		return( set.getTargets( action ).values().toArray( new MetaReleaseTarget[0] ) );
	}

	public MetaReleaseTarget[] getCategoryTargets( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		MetaReleaseSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( new MetaReleaseTarget[0] );
		return( set.getTargets( action ).values().toArray( new MetaReleaseTarget[0] ) );
	}

	public MetaReleaseTarget findBuildProject( ActionBase action , String name ) throws Exception {
		MetaSourceProject sourceProject = meta.sources.getProject( action , name );
		MetaReleaseSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return( null );
		
		MetaReleaseTarget project = set.findTargetByOriginalName( action , name );
		return( project );
	}
	
	public MetaReleaseTarget getBuildProject( ActionBase action , String name ) throws Exception {
		MetaReleaseTarget project = findBuildProject( action , name );
		if( project == null )
			action.exit( "unknown release project=" + name );
		
		return( project );
	}

	public MetaReleaseTarget findCategoryTarget( ActionBase action , VarCATEGORY CATEGORY , String KEY ) throws Exception {
		MetaReleaseSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( null );
		
		MetaReleaseTarget target = set.findTargetByOriginalName( action , KEY );
		return( target );
	}
	
	public MetaReleaseTarget findConfComponent( ActionBase action , String KEY ) throws Exception {
		return( findCategoryTarget( action , VarCATEGORY.CONFIG , KEY ) );
	}
	
	public MetaReleaseTarget getConfComponent( ActionBase action , String KEY ) throws Exception {
		MetaReleaseTarget comp = findConfComponent( action , KEY );
		if( comp == null )
			action.exit( "unknown release component=" + KEY );
		
		return( comp );
	}

	public Map<String,MetaReleaseTarget> getCategoryComponents( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		MetaReleaseSet set = categorySetMap.get( CATEGORY );
		if( set == null )
			return( new HashMap<String,MetaReleaseTarget>() );
		
		return( set.map );
	}
	
	public Map<String,MetaReleaseTarget> getConfComponents( ActionBase action ) throws Exception {
		return( getCategoryComponents( action , VarCATEGORY.CONFIG ) );
	}

	public Map<String,MetaReleaseDelivery> getDeliveries( ActionBase action ) throws Exception {
		return( deliveryMap );
	}

	public MetaReleaseDelivery findDelivery( ActionBase action , String folder ) throws Exception {
		MetaReleaseDelivery delivery = deliveryMap.get( folder );
		return( delivery );
	}
	
	public MetaReleaseDelivery getDelivery( ActionBase action , String name ) throws Exception {
		MetaReleaseDelivery delivery = deliveryMap.get( name );
		if( delivery == null )
			action.exit( "unknown delivery folder=" + name );
		
		return( delivery );
	}
	
	public MetaReleaseDelivery getDeliveryByFolder( ActionBase action , String folder ) throws Exception {
		for( MetaReleaseDelivery delivery : deliveryMap.values() )
			if( delivery.distDelivery.FOLDERPATH.equals( folder ) )
				return( delivery );
		
		action.exit( "unknown delivery folder=" + folder );
		return( null );
	}
	
	public boolean isEmpty( ActionBase action ) throws Exception {
		for( MetaReleaseSet set : sourceSetMap.values() )
			if( !set.isEmpty( action ) )
				return( false );
		for( MetaReleaseSet set : categorySetMap.values() )
			if( !set.isEmpty( action ) )
				return( false );
		return( true );
	}

	public boolean isEmptyConfiguration( ActionBase action ) throws Exception {
		MetaReleaseSet set = findCategorySet( action , VarCATEGORY.CONFIG );
		if( set == null || set.isEmpty( action ) )
			return( true );
		
		return( false );
	}
	
	public boolean isEmptyDatabase( ActionBase action ) throws Exception {
		MetaReleaseSet set = findCategorySet( action , VarCATEGORY.DB );
		if( set == null || set.isEmpty( action ) )
			return( true );
		
		return( false );
	}
	
	public static Document createEmptyXmlDoc( ActionBase action , VarBUILDMODE BUILDMODE , boolean obsolete ) throws Exception {
		Document doc = Common.xmlCreateDoc( "release" );
		Element root = doc.getDocumentElement();
		Common.xmlCreatePropertyElement( doc , root , "buildMode" , Common.getEnumLower( BUILDMODE ) );
		Common.xmlCreateBooleanPropertyElement( doc , root , "obsolete" , obsolete );
		
		for( VarCATEGORY CATEGORY : action.meta.getAllReleaseCategories( action ) )
			Common.xmlCreateElement( doc , root , Common.getEnumLower( CATEGORY ) );
		return( doc );
	}
	
	public static void createEmptyXml( ActionBase action , VarBUILDMODE BUILDMODE , boolean obsolete , String filePath ) throws Exception {
		Document doc = createEmptyXmlDoc( action , BUILDMODE , obsolete );
		Common.xmlSaveDoc( doc , filePath );
	}

	public Document createXml( ActionBase action ) throws Exception {
		Document doc = createEmptyXmlDoc( action , PROPERTY_BUILDMODE , PROPERTY_OBSOLETE );
		Element root = doc.getDocumentElement();
		
		for( MetaReleaseSet set : sourceSetMap.values() ) {
			Element parent = ( Element )ConfReader.xmlGetFirstChild( action , root , Common.getEnumLower( set.CATEGORY ) );
			set.createXml( action , doc , parent );
		}

		for( MetaReleaseSet set : categorySetMap.values() ) {
			Element parent = ( Element )ConfReader.xmlGetFirstChild( action , root , Common.getEnumLower( set.CATEGORY ) );
			set.createXml( action , doc , parent );
		}
			
		return( doc );
	}

	public boolean addSourceSet( ActionBase action , MetaSourceProjectSet sourceSet , boolean all ) throws Exception {
		MetaReleaseSet set = findSourceSet( action , sourceSet.NAME );
		if( set == null ) {
			set = new MetaReleaseSet( meta , sourceSet.CATEGORY );
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
		MetaReleaseSet set = findCategorySet( action , CATEGORY );
		if( set == null ) {
			set = new MetaReleaseSet( meta , CATEGORY );
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
		MetaReleaseSet set = findCategorySet( action , CATEGORY );
		if( set == null )
			return;
		
		unregisterSet( action , set );
	}

	public void deleteSourceSet( ActionBase action , MetaSourceProjectSet sourceSet ) throws Exception {
		MetaReleaseSet set = findSourceSet( action , sourceSet.NAME );
		if( set == null )
			return;
		
		unregisterSet( action , set );
	}

	private void unregisterTarget( ActionBase action , MetaReleaseTarget target ) throws Exception {
		if( meta.isSourceCategory( action , target.CATEGORY ) ) {
			for( MetaReleaseTargetItem item : target.getItems( action ).values() )
				unregisterTargetItem( action , item );
		}
		else {
			MetaDistrDelivery distDelivery = target.getDelivery( action );
			if( distDelivery == null )
				return;
			
			MetaReleaseDelivery releaseDelivery = deliveryMap.get( distDelivery.NAME );
			if( releaseDelivery == null )
				return;
			
			releaseDelivery.removeCategoryTarget( action , target );
		}
	}

	private void unregisterSet( ActionBase action , MetaReleaseSet set ) throws Exception {
		for( MetaReleaseTarget project : set.getTargets( action ).values() )
			unregisterTarget( action , project );
		
		if( meta.isSourceCategory( action , set.CATEGORY ) )
			sourceSetMap.remove( set.NAME );
		else
			categorySetMap.remove( set.CATEGORY );
	}
	
	public boolean addProject( ActionBase action , MetaSourceProject sourceProject , boolean allItems ) throws Exception {
		MetaReleaseSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return( false );
		
		MetaReleaseTarget project = set.findTargetByOriginalName( action , sourceProject.PROJECT );
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

	public void deleteTarget( ActionBase action , MetaReleaseTarget target ) throws Exception {
		unregisterTarget( action , target );
		target.set.removeTarget( action , target );
	}
	
	public void deleteCategoryTarget( ActionBase action , VarCATEGORY CATEGORY , String NAME ) throws Exception {
		MetaReleaseSet set = getCategorySet( action , CATEGORY );
		if( set == null )
			return;

		MetaReleaseTarget target = set.findTargetByOriginalName( action , NAME );
		if( target == null )
			return;

		deleteTarget( action , target );
	}
	
	public void deleteProjectSource( ActionBase action , MetaSourceProject sourceProject ) throws Exception {
		MetaReleaseSet set = findSourceSet( action , sourceProject.set.NAME );
		if( set == null )
			return;
		
		MetaReleaseTarget target = set.findTargetByOriginalName( action , sourceProject.PROJECT );
		if( target == null )
			return;
		
		deleteTarget( action , target );
	}

	public boolean addProjectItem( ActionBase action , MetaSourceProject sourceProject , MetaSourceProjectItem sourceItem ) throws Exception {
		if( sourceItem.INTERNAL )
			action.exit( "unexpected call for INTERNAL item=" + sourceItem.ITEMNAME );
		
		MetaReleaseSet set = sourceSetMap.get( sourceProject.CATEGORY );
		if( set == null )
			return( false );
		
		if( set.ALL )
			return( true );
		
		MetaReleaseTarget project = set.findTargetByOriginalName( action , sourceProject.PROJECT );
		if( project == null )
			return( false );

		if( project.ALL )
			return( true );
		
		MetaReleaseTargetItem item = project.getItem( action , sourceItem.ITEMNAME );
		if( item != null ) {
			if( !item.checkPropsEqualsToOptions( action ) )
				return( false );
		}
		
		item = project.addSourceItem( action , sourceItem );
		registerTargetItem( action , item );
		return( true );
	}

	public void deleteProjectItem( ActionBase action , MetaReleaseTargetItem item ) throws Exception {
		item.releaseProject.set.makePartial( action );
		item.releaseProject.removeSourceItem( action , item );
		unregisterTargetItem( action , item );
	}
	
	public void deleteProjectItem( ActionBase action , VarCATEGORY CATEGORY , MetaSourceProject sourceProject , MetaSourceProjectItem sourceItem ) throws Exception {
		MetaReleaseSet set = sourceSetMap.get( sourceProject.set.NAME );
		if( set == null )
			return;

		MetaReleaseTarget project = set.findTargetByOriginalName( action , sourceProject.PROJECT );
		if( project == null )
			return;
		
		MetaReleaseTargetItem item = project.getItem( action , sourceItem.ITEMNAME );
		if( item == null )
			return;

		deleteProjectItem( action , item );
	}
	
	public boolean addConfItem( ActionBase action , MetaDistrConfItem item ) throws Exception {
		MetaReleaseSet set = getCategorySet( action , VarCATEGORY.CONFIG );
		if( set.ALL )
			return( true );

		MetaReleaseTarget target = set.findCategoryTarget( action , VarCATEGORY.CONFIG , item.KEY );
		if( target != null ) {
			if( !target.ALL )
				target.setAll( action , action.options.OPT_REPLACE );
			return( true );
		}
		
		target = set.addConfItem( action , item , action.options.OPT_REPLACE );
		registerTarget( action , target );
		return( true );
	}

	public boolean addDatabaseItem( ActionBase action , MetaDistrDelivery item ) throws Exception {
		if( !item.hasDatabaseItems( action ) ) {
			action.log( "no database items in delivery=" + item.NAME );
			return( false );
		}
		
		MetaReleaseSet set = getCategorySet( action , VarCATEGORY.DB );
		if( set.ALL )
			return( true );

		MetaReleaseTarget target = set.findCategoryTarget( action , VarCATEGORY.DB , item.NAME );
		if( target != null )
			return( true );
		
		target = set.addDatabaseItem( action , item );
		registerTarget( action , target );
		return( true );
	}

	public boolean addManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( !item.MANUAL )
			action.exit( "unexpected non-manual item=" + item.KEY );
			
		MetaReleaseSet set = getCategorySet( action , VarCATEGORY.MANUAL );
		if( set.ALL )
			return( true );

		MetaReleaseTarget target = set.findCategoryTarget( action , VarCATEGORY.MANUAL , item.KEY );
		if( target != null )
			return( true );
		
		target = set.addManualItem( action , item );
		registerTarget( action , target );
		return( true );
	}

}
