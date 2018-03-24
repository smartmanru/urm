package org.urm.meta.release;

import java.util.Date;

public class ReleaseDistItem {

	public static String PROPERTY_FILE = "file";
	public static String PROPERTY_FILE_PATH = "filepath";
	public static String PROPERTY_FILE_HASH = "filehash";
	public static String PROPERTY_FILE_SIZE = "filesize";
	public static String PROPERTY_FILE_TIME = "filetime";

	public Release release;
	public ReleaseDist releaseDist;
	
	public int ID;
	public int DELIVERYTARGET_ID;
	public String TARGETFILE;
	public String TARGETFILE_PATH;
	public String TARGETFILE_HASH;
	public long TARGETFILE_SIZE;
	public Date TARGETFILE_TIME;
	public String SOURCE_RELEASEDIR;
	public String SOURCE_RELEASETIME;
	public int RV;

	public ReleaseDistItem( Release release , ReleaseDist releaseDist ) {
		this.release = release;
		this.releaseDist = releaseDist;
	}
	
	public ReleaseDistItem copy( Release rrelease , ReleaseDist releaseDist ) {
		ReleaseDistItem r = new ReleaseDistItem( rrelease , releaseDist );
		
		r.ID = ID;
		r.DELIVERYTARGET_ID = DELIVERYTARGET_ID;
		r.TARGETFILE = TARGETFILE;
		r.TARGETFILE_PATH = TARGETFILE_PATH;
		r.TARGETFILE_HASH = TARGETFILE_HASH;
		r.TARGETFILE_SIZE = TARGETFILE_SIZE;
		r.TARGETFILE_TIME = TARGETFILE_TIME;
		r.SOURCE_RELEASEDIR = SOURCE_RELEASEDIR;
		r.SOURCE_RELEASETIME = SOURCE_RELEASETIME;
		r.RV = RV;
		
		return( r );
	}
	
}
