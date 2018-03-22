package org.urm.meta.release;

import java.util.Date;

public class ReleaseDistTarget {

	public static String PROPERTY_FILE = "file";
	public static String PROPERTY_FILE_PATH = "filepath";
	public static String PROPERTY_FILE_HASH = "filehash";
	public static String PROPERTY_FILE_SIZE = "filesize";
	public static String PROPERTY_FILE_TIME = "filetime";

	public int ID;
	public int RELEASETARGET_ID;
	public String TARGETFILE;
	public String TARGETFILE_PATH;
	public String TARGETFILE_HASH;
	public long TARGETFILE_SIZE;
	public Date TARGETFILE_TIME;
	public Integer SOURCE_DIST_ID;
	public int RV;
	
}
