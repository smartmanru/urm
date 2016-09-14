package org.urm.common;

public class ExitException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6785595595488058740L;

	public static int BaseCommon = 0;
	public static int BaseCommonAction = 1000;
	public static int BaseCommonJmx = 1001;
	public static int BaseCommonMeta = 1002;
	public static int BaseAction = 2020;
	public static int BaseActionBuild = 2010;
	public static int BaseActionConf = 2011;
	public static int BaseActionDatabase = 2012;
	public static int BaseActionDeploy = 2013;
	public static int BaseActionMain = 2014;
	public static int BaseActionMonitor = 2015;
	public static int BaseActionRelease = 2016;
	public static int BaseActionXDoc = 2017;
	public static int BaseEngine = 2000;
	public static int BaseEngineAction = 2001;
	public static int BaseEngineCustom = 2100;
	public static int BaseEngineDist = 2200;
	public static int BaseEngineExecutor = 2300;
	public static int BaseEngineMeta = 2400;
	public static int BaseEngineShell = 2500;
	public static int BaseEngineStorage = 2600;
	public static int BaseEngineVCS = 2700;
	public static int BaseClient = 3000;
	public static int BasePlugin = 8000;

	public int errorCode;
	public String errorMessage;
	public String[] errorParams;
	
	public ExitException( int errorCode , String message , String[] params ) {
        super( message );
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.errorParams = params;
    }
}
