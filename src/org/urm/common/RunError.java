package org.urm.common;

public class RunError extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6785595595488058740L;

	public static int BaseCommon = 100000;
	public static int BaseCommonAction = 101000;
	public static int BaseCommonJmx = 102000;
	public static int BaseCommonMeta = 103000;
	public static int BaseEngine = 110000;
	public static int BaseEngineAction = 111000;
	public static int BaseEngineCustom = 112000;
	public static int BaseEngineDist = 113000;
	public static int BaseEngineExecutor = 114000;
	public static int BaseEngineMeta = 115000;
	public static int BaseEngineShell = 116000;
	public static int BaseEngineStorage = 117000;
	public static int BaseEngineVCS = 118000;
	public static int BaseAction = 200000;
	public static int BaseActionBuild = 201000;
	public static int BaseActionConf = 202000;
	public static int BaseActionDatabase = 203000;
	public static int BaseActionDeploy = 204000;
	public static int BaseActionMain = 205000;
	public static int BaseActionMonitor = 206000;
	public static int BaseActionRelease = 207000;
	public static int BaseActionXDoc = 208000;
	public static int BaseClient = 300000;
	public static int BasePlugin = 800000;
	
	public static int InternalBase = 500;

	private int errorCode;
	private String errorMessage;
	private String[] errorParams;
	
	public RunError( int errorCode , String message , String[] params ) {
        super( message );
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.errorParams = params;
    }
	
	public RunError( Throwable cause , int errorCode , String message , String[] params ) {
        super( message , cause );
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.errorParams = params;
    }

	public RunError getApplicationRootCause() {
		Throwable cause = super.getCause();
		if( cause == null )
			return( this );
		
		if( cause.getClass() != RunError.class )
			return( this );
		
		RunError runCause = ( RunError )cause;
		runCause = runCause.getApplicationRootCause();
		if( runCause == null )
			return( this );
		
		return( runCause );
	}

	public RunError getUserRootCause() {
		if( ( errorCode % 1000 ) >= RunError.InternalBase )
			return( null );
		
		Throwable cause = super.getCause();
		if( cause == null )
			return( this );
		
		if( cause.getClass() != RunError.class )
			return( this );
		
		RunError runCause = ( RunError )cause;
		runCause = runCause.getUserRootCause();
		if( runCause == null )
			return( this );
		
		return( runCause );
	}

	public int getCode() {
		return( errorCode );
	}
	
	public String getMessage() {
		return( errorMessage );
	}
	
	public String[] getParams() {
		return( errorParams );
	}
	
}
