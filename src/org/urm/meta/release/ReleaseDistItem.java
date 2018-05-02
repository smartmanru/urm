package org.urm.meta.release;

import java.util.Date;

import org.urm.engine.dist.DistItemInfo;

public class ReleaseDistItem {

	public static String PROPERTY_FILE = "file";
	public static String PROPERTY_FILE_FOLDER = "folder";
	public static String PROPERTY_FILE_HASH = "filehash";
	public static String PROPERTY_FILE_SIZE = "filesize";
	public static String PROPERTY_FILE_TIME = "filetime";

	public Release release;
	public ReleaseDist releaseDist;
	
	public int ID;
	public int DISTTARGET_ID;
	public String TARGETFILE;
	public String TARGETFILE_FOLDER;
	public String TARGETFILE_HASH;
	public Long TARGETFILE_SIZE;
	public Date TARGETFILE_TIME;
	public String SOURCE_RELEASEDIR;
	public Date SOURCE_RELEASETIME;
	public int RV;

	public ReleaseDistItem( Release release , ReleaseDist releaseDist ) {
		this.release = release;
		this.releaseDist = releaseDist;
	}
	
	public ReleaseDistItem copy( Release rrelease , ReleaseDist releaseDist ) {
		ReleaseDistItem r = new ReleaseDistItem( rrelease , releaseDist );
		
		r.ID = ID;
		r.DISTTARGET_ID = DISTTARGET_ID;
		r.TARGETFILE = TARGETFILE;
		r.TARGETFILE_FOLDER = TARGETFILE_FOLDER;
		r.TARGETFILE_HASH = TARGETFILE_HASH;
		r.TARGETFILE_SIZE = TARGETFILE_SIZE;
		r.TARGETFILE_TIME = TARGETFILE_TIME;
		r.SOURCE_RELEASEDIR = SOURCE_RELEASEDIR;
		r.SOURCE_RELEASETIME = SOURCE_RELEASETIME;
		r.RV = RV;
		
		return( r );
	}
	
	public void create( int DISTTARGET_ID ,	String TARGETFILE , String TARGETFILE_FOLDER , String TARGETFILE_HASH ,	Long TARGETFILE_SIZE , Date TARGETFILE_TIME , 
			String SOURCE_RELEASEDIR , Date SOURCE_RELEASETIME ) {
		this.DISTTARGET_ID = DISTTARGET_ID;
		this.TARGETFILE = TARGETFILE;
		this.TARGETFILE_FOLDER = TARGETFILE_FOLDER;
		this.TARGETFILE_HASH = TARGETFILE_HASH;
		this.TARGETFILE_SIZE = TARGETFILE_SIZE;
		this.TARGETFILE_TIME = TARGETFILE_TIME;
		this.SOURCE_RELEASEDIR = SOURCE_RELEASEDIR;
		this.SOURCE_RELEASETIME = SOURCE_RELEASETIME;
	}
	
	public void create( ReleaseDistTarget target , DistItemInfo info ) {
		this.DISTTARGET_ID = target.ID;
		this.TARGETFILE = info.getFinalName();
		this.TARGETFILE_FOLDER = info.getDistItemFolder();
		this.TARGETFILE_HASH = info.getMD5();
		this.TARGETFILE_SIZE = info.getSize();
		this.TARGETFILE_TIME = info.getTimestamp();
		this.SOURCE_RELEASEDIR = "";
		this.SOURCE_RELEASETIME = null;
	}
	
}
