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
	public static int BaseServer = 2000;
	public static int BaseServerAction = 2001;
	public static int BaseServerActionBuild = 2010;
	public static int BaseServerActionConf = 2011;
	public static int BaseServerActionDatabase = 2012;
	public static int BaseServerActionDeploy = 2013;
	public static int BaseServerActionMain = 2014;
	public static int BaseServerActionMonitor = 2015;
	public static int BaseServerActionRelease = 2016;
	public static int BaseServerActionXDoc = 2017;
	public static int BaseServerCustom = 2100;
	public static int BaseServerDist = 2200;
	public static int BaseServerExecutor = 2300;
	public static int BaseServerMeta = 2400;
	public static int BaseServerShell = 2500;
	public static int BaseServerStorage = 2600;
	public static int BaseServerVCS = 2700;
	public static int BaseClient = 3000;
	public static int BasePlugin = 8000;
	
	public ExitException( int errorCode , String message , String[] params ) {
        super( message );
    }
}