package org.urm.engine.dist;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.meta.engine.ServerReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseSchedule {

	public Meta meta;
	public Release release;
	
	public String LIFECYCLE;
	
	public ReleaseSchedule( Meta meta , Release release ) {
		this.meta = meta;
		this.release = release;
	}
	
	public ReleaseSchedule copy( ActionBase action , Meta meta , Release release ) throws Exception {
		ReleaseSchedule r = new ReleaseSchedule( meta , release );
		r.LIFECYCLE = LIFECYCLE;
		return( r );
	}

	public void load( ActionBase action , Node node ) throws Exception {
	}
	
	public void save( ActionBase action , Document doc , Element node ) throws Exception {
	}
	
	public void createReleaseSchedule( ActionBase action , Date releaseDate , ServerReleaseLifecycle slc ) throws Exception {
	}
	
	public void changeReleaseSchedule( ActionBase action , Date releaseDate , ServerReleaseLifecycle slc ) throws Exception {
	}
	
}
