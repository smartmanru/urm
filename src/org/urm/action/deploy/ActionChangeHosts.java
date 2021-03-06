package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.common.Common;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.Shell;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;

public class ActionChangeHosts extends ActionBase {

	String cmd;
	String opHost;
	String opAddress;
	
	public ActionChangeHosts( ActionBase action , String stream , String cmd , String opHost , String opAddress ) {
		super( action , stream , "Change hosts, cmd=" + cmd + ", host=" + opHost + ", address=" + opAddress );
		
		this.cmd = cmd;
		this.opHost = opHost;
		this.opAddress = opAddress;
	}

	@Override protected SCOPESTATE executeAccount( ScopeState state , ActionScopeSet set , Account account ) throws Exception {
		if( cmd.equals( "set" ) )
			executeSet( account );
		
		if( cmd.equals( "delete" ) )
			executeDelete( account );
		
		if( cmd.equals( "check" ) )
			executeCheck( account );
		
		return( SCOPESTATE.RunSuccess );
	}
	
	private void executeSet( Account account ) throws Exception {
		super.executeCmdLive( account , "cat /etc/hosts | grep -v " + opHost + " | grep -v " + opAddress + 
			" > /etc/hosts.new; echo " + Common.getQuoted( opHost + " " + opAddress ) + 
			" >> /etc/hosts.new; mv /etc/hosts.new /etc/hosts" , Shell.WAIT_DEFAULT );
	}

	private void executeDelete( Account account ) throws Exception {
		if( !opAddress.isEmpty() )
			super.executeCmdLive( account , "cat /etc/hosts | grep -v " + opHost + " | grep -v " + opAddress +
				" > /etc/hosts.new; mv /etc/hosts.new /etc/hosts" , Shell.WAIT_DEFAULT );
		else
			super.executeCmdLive( account , "cat /etc/hosts | grep -v " + opHost + 
				" > /etc/hosts.new; mv /etc/hosts.new /etc/hosts" , Shell.WAIT_DEFAULT );
	}

	private void executeCheck( Account account ) throws Exception {
		if( opAddress.isEmpty() ) {
			String res = super.executeCmdGetValue( account , "cat /etc/hosts | grep " + opHost , Shell.WAIT_DEFAULT );
			if( res.isEmpty() ) {
				error( account.getPrintName() + ": missing " + opHost );
				return;
			}
	
			if( res.indexOf( "\n" ) >= 0 ) {
				error( account.getPrintName() + ": duplicate " + opHost + " (" + res + ")" );
				return;
			}
			
			info( account.getPrintName() + ": " + res );
		}	
		else {
			String res = super.executeCmdGetValue( account , "cat /etc/hosts | egrep " + 
				Common.getQuoted( opHost + "|" + opAddress ) , Shell.WAIT_DEFAULT );
			if( res.isEmpty() ) {
				info( account.getPrintName() + ": missing " + opHost );
				return;
			}
	
			if( res.indexOf( "\n" ) >= 0 ) {
				error( account.getPrintName() + ": duplicate " + opHost + " (" + res + ")" );
				return;
			}
	
			String F_HOSTADDR = Common.getPartAfterLast( res , " " );
			String F_HOSTNAME = Common.getPartBeforeFirst( res ,  " " );
	
			if( F_HOSTNAME.equals( opHost ) == false || F_HOSTADDR.equals( opAddress ) == false ) {
				error( account.getPrintName() + ": " + res + " - not matched (" + opHost + " " + opAddress + ")" );
				return;
			}
	
			info( account.getPrintName() + ": " + res + " - ok" );
		}
	}

}
