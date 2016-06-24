package org.urm.server.shell;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.urm.server.action.ActionBase;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

public class ShellCoreJNI {

	public interface CStdLib extends Library {
        int syscall(int number, Object... args);
    }
	
	public void setProcessName( String name ) {
		CStdLib c = ( CStdLib )Native.loadLibrary( "c" , CStdLib.class );
		int SYSCALL_PRCTL = 157;
		int PR_SET_NAME = 15;
		c.syscall( SYSCALL_PRCTL , PR_SET_NAME , name , 0 , 0 , 0 );
	}
	
	public interface Kernel32 extends W32API {
	    Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, DEFAULT_OPTIONS);
	    /* http://msdn.microsoft.com/en-us/library/ms683179(VS.85).aspx */
	    HANDLE GetCurrentProcess();
	    /* http://msdn.microsoft.com/en-us/library/ms683215.aspx */
	    int GetProcessId(HANDLE Process);
	}
	
	public interface W32Errors {
	    int NO_ERROR               = 0;
	    int ERROR_INVALID_FUNCTION = 1;
	    int ERROR_FILE_NOT_FOUND   = 2;
	    int ERROR_PATH_NOT_FOUND   = 3;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	public interface W32API extends StdCallLibrary, W32Errors {
	    
	    /** Standard options to use the unicode version of a w32 API. */
		Map UNICODE_OPTIONS = new HashMap() {
	        {
	            put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
	            put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
	        }
	    };
	    
	    /** Standard options to use the ASCII/MBCS version of a w32 API. */
	    Map ASCII_OPTIONS = new HashMap() {
	        {
	            put(OPTION_TYPE_MAPPER, W32APITypeMapper.ASCII);
	            put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.ASCII);
	        }
	    };

	    Map DEFAULT_OPTIONS = Boolean.getBoolean("w32.ascii") ? ASCII_OPTIONS : UNICODE_OPTIONS;
	    
	    public class HANDLE extends PointerType {
	    	@Override
	        public Object fromNative(Object nativeValue, FromNativeContext context) {
	            Object o = super.fromNative(nativeValue, context);
	            if (INVALID_HANDLE_VALUE.equals(o))
	                return INVALID_HANDLE_VALUE;
	            return o;
	        }
	    }

	    /** Constant value representing an invalid HANDLE. */
	    HANDLE INVALID_HANDLE_VALUE = new HANDLE() { 
	        { super.setPointer(Pointer.createConstant(-1)); }
	        @Override
	        public void setPointer(Pointer p) { 
	            throw new UnsupportedOperationException("Immutable reference");
	        }
	    };
	}	

	public int getWindowsProcessId( ActionBase action , Process process ) throws Exception {
		try {
			  Field f = process.getClass().getDeclaredField( "handle" );
			  f.setAccessible( true );				
			  long handle = f.getLong( process );
			    
			  Kernel32 kernel = Kernel32.INSTANCE;
			  W32API.HANDLE winHandle = new W32API.HANDLE();
			  winHandle.setPointer( Pointer.createConstant( handle ) );
			  return( kernel.GetProcessId( winHandle ) );
		} catch (Throwable e) {
			action.exit( "unable to get windows process id" );
		}

		return( -1 );
	}
	
}
