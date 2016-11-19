package org.urm.common;

public class RunError extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6785595595488058740L;

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
		if( ( errorCode % 1000 ) >= RunErrorClass.InternalBase )
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
