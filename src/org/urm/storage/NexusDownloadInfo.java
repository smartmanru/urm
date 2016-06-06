package org.urm.storage;

public class NexusDownloadInfo {

	public LocalFolder artefactoryFolder;
	
	public String DOWNLOAD_URL;
	public String DOWNLOAD_URL_REQUEST;
	public String DOWNLOAD_FILENAME;
	public String BASENAME;
	public String EXT;
	
	public NexusDownloadInfo( LocalFolder artefactoryFolder ) {
		this.artefactoryFolder = artefactoryFolder;
	}
}
