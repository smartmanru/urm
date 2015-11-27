package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeSet;

public class ActionChangeHosts extends ActionBase {

	String cmd;
	String opHost;
	String opAddress;
	
	public ActionChangeHosts( ActionBase action , String stream , String cmd , String opHost , String opAddress ) {
		super( action , stream );
		
		this.cmd = cmd;
		this.opHost = opHost;
		this.opAddress = opAddress;
	}

	@Override protected boolean executeAccount( ActionScopeSet set , String hostLogin ) throws Exception {
		if( cmd.equals( "set" ) )
			executeSet( hostLogin );
		
		if( cmd.equals( "delete" ) )
			executeDelete( hostLogin );
		
		if( cmd.equals( "check" ) )
			executeCheck( hostLogin );
		
		return( true );
	}
	
	private void executeSet( String hostLogin ) throws Exception {
		super.executeCmdLive( hostLogin , "cat /etc/hosts | grep -v " + opHost + " | grep -v " + opAddress + 
			" > /etc/hosts.new; echo " + Common.getQuoted( opHost + " " + opAddress ) + 
			" >> /etc/hosts.new; mv /etc/hosts.new /etc/hosts" );
	}

	private void executeDelete( String hostLogin ) throws Exception {
		if( !opAddress.isEmpty() )
			super.executeCmdLive( hostLogin , "cat /etc/hosts | grep -v " + opHost + " | grep -v " + opAddress +
				" > /etc/hosts.new; mv /etc/hosts.new /etc/hosts" );
		else
			super.executeCmdLive( hostLogin , "cat /etc/hosts | grep -v " + opHost + 
				" > /etc/hosts.new; mv /etc/hosts.new /etc/hosts" );
	}

	private void executeCheck( String hostLogin ) throws Exception {
		if( opAddress.isEmpty() ) {
			String res = super.executeCmdGetValue( hostLogin , "cat /etc/hosts | grep " + opHost );
			if( res.isEmpty() ) {
				log( hostLogin + ": missing " + opHost );
				return;
			}
	
			if( res.indexOf( "\n" ) >= 0 ) {
				log( hostLogin + ": duplicate " + opHost + " (" + res + ")" );
				return;
			}
			
			log( hostLogin + ": " + res );
		}	
		else {
			String res = super.executeCmdGetValue( hostLogin , "cat /etc/hosts | egrep " + 
				Common.getQuoted( opHost + "|" + opAddress ) );
			if( res.isEmpty() ) {
				log( hostLogin + ": missing " + opHost );
				return;
			}
	
			if( res.indexOf( "\n" ) >= 0 ) {
				log( hostLogin + ": duplicate " + opHost + " (" + res + ")" );
				return;
			}
	
			String F_HOSTADDR = Common.getPartAfterLast( res , " " );
			String F_HOSTNAME = Common.getPartBeforeFirst( res ,  " " );
	
			if( F_HOSTNAME.equals( opHost ) == false || F_HOSTADDR.equals( opAddress ) == false ) {
				log( hostLogin + ": " + res + " - not matched (" + opHost + " " + opAddress + ")" );
				return;
			}
	
			log( hostLogin + ": " + res + " - ok" );
		}
	}

}
