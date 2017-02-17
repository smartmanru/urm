package org.urm.engine.dist;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.Types.VarLCTYPE;
import org.urm.meta.engine.ServerReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductCoreSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseSchedule {

	public Meta meta;
	public Release release;
	
	public String LIFECYCLE;
	public Date releaseDate;
	
	public ReleaseSchedule( Meta meta , Release release ) {
		this.meta = meta;
		this.release = release;
	}
	
	public ReleaseSchedule copy( ActionBase action , Meta meta , Release release ) throws Exception {
		ReleaseSchedule r = new ReleaseSchedule( meta , release );
		r.LIFECYCLE = LIFECYCLE;
		return( r );
	}

	public void load( ActionBase action , Node root ) throws Exception {
		Node node = ConfReader.xmlGetFirstChild( root , "schedule" );
		if( node == null ) {
			LIFECYCLE = "";
			releaseDate = null;
			return;
		}
		
		LIFECYCLE = ConfReader.getPropertyValue( node , "lifecycle" );
		releaseDate = Common.getDateValue( ConfReader.getPropertyValue( node , "releasedate" ) );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Element node = Common.xmlCreateElement( doc , root , "schedule" );
		Common.xmlCreatePropertyElement( doc , node , "lifecycle" , LIFECYCLE );
		Common.xmlCreatePropertyElement( doc , node , "releasedate" , Common.getDateValue( releaseDate ) );
	}
	
	public void createReleaseSchedule( ActionBase action , Date releaseDate , ServerReleaseLifecycle lc ) throws Exception {
		lc = setLifecycle( action , lc );
		setSchedule( action , releaseDate , lc );
	}
	
	public void changeReleaseSchedule( ActionBase action , Date releaseDate , ServerReleaseLifecycle lc ) throws Exception {
		lc = setLifecycle( action , lc );
		setSchedule( action , releaseDate , lc );
	}

	private ServerReleaseLifecycle setLifecycle( ActionBase action , ServerReleaseLifecycle lc ) throws Exception {
		MetaProductCoreSettings core = meta.getProductCoreSettings( action );
		VarLCTYPE type = release.getLifecycleType();
		
		if( type == VarLCTYPE.MAJOR ) {
			String expected = core.RELEASELC_MAJOR;
			if( expected.isEmpty() ) {
				if( lc != null ) {
					LIFECYCLE = lc.ID;
					return( lc );
				}
			}
			else
			if( lc != null ) {
				if( !expected.equals( lc.ID ) )
					action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" , lc.ID );
				LIFECYCLE = lc.ID;
				return( lc );
			}
		}
		else
		if( type == VarLCTYPE.MINOR ) {
			String expected = core.RELEASELC_MINOR;
			if( expected.isEmpty() ) {
				if( lc != null ) {
					LIFECYCLE = lc.ID;
					return( lc );
				}
			}
			else
			if( lc != null ) {
				if( !expected.equals( lc.ID ) )
					action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" , lc.ID );
				LIFECYCLE = lc.ID;
				return( lc );
			}
		}
		else
		if( type == VarLCTYPE.URGENT ) {
			String[] expected = core.RELEASELC_URGENT_LIST;
			if( expected.length == 0 ) {
				if( lc != null ) {
					LIFECYCLE = lc.ID;
					return( lc );
				}
			}
			else
			if( lc != null ) {
				if( Common.getIndexOf( expected , lc.ID ) < 0 )
					action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" , lc.ID );
				LIFECYCLE = lc.ID;
				return( lc );
			}
		}
		
		LIFECYCLE = "";
		return( null );
	}
	
	private void setSchedule( ActionBase action , Date releaseDate , ServerReleaseLifecycle lc ) throws Exception {
		this.releaseDate = releaseDate;
	}
	
}
