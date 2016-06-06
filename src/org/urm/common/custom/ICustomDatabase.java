package org.urm.common.custom;

import org.urm.server.action.ActionBase;
import org.urm.server.storage.FileSet;
import org.urm.server.storage.LocalFolder;

public interface ICustomDatabase {

	public String getGroupName( ActionBase action , CommandCustom custom , String groupFolder ) throws Exception;
	public void checkSourceFile( ActionBase action , CommandCustom custom , String groupName , LocalFolder folder , String filePath ) throws Exception;
	public void addSourceFile( ActionBase action , CommandCustom custom , String groupName , LocalFolder folder , String filePath ) throws Exception;
	public void startSourceGroup( ActionBase action , CommandCustom custom , String groupName ) throws Exception;
	public void finishSourceGroup( ActionBase action , CommandCustom custom , String groupName ) throws Exception;
	
	public void copyCustom( ActionBase action , CommandCustom custom , FileSet ALIGNEDNAME , String ALIGNEDID , LocalFolder TARGETDIR ) throws Exception;
	public boolean checkDatabaseDir( ActionBase action , CommandCustom custom , FileSet ALIGNEDNAME , String ALIGNEDID , String DIR , String SCHEMALIST ) throws Exception;
	
}
