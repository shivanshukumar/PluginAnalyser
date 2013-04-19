package com.shivanshusingh.pluginanalyser.utils.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface EclipsePlatform {

	/**
	 * Constant string (value "win32") indicating the platform is running on a
	 * Window 32-bit operating system (e.g., Windows 98, NT, 2000).
	 */
	public static final String OS_WIN32 = "win32";//$NON-NLS-1$

	/**
	 * Constant string (value "linux") indicating the platform is running on a
	 * Linux-based operating system.
	 */
	public static final String OS_LINUX = "linux";//$NON-NLS-1$

	/**
	 * Constant string (value "aix") indicating the platform is running on an
	 * AIX-based operating system.
	 */
	public static final String OS_AIX = "aix";//$NON-NLS-1$

	/**
	 * Constant string (value "solaris") indicating the platform is running on a
	 * Solaris-based operating system.
	 */
	public static final String OS_SOLARIS = "solaris";//$NON-NLS-1$

	/**
	 * Constant string (value "hpux") indicating the platform is running on an
	 * HP/UX-based operating system.
	 */
	public static final String OS_HPUX = "hpux";//$NON-NLS-1$

	/**
	 * Constant string (value "qnx") indicating the platform is running on a
	 * QNX-based operating system.
	 */
	public static final String OS_QNX = "qnx";//$NON-NLS-1$

	/**
	 * Constant string (value "macosx") indicating the platform is running on a
	 * Mac OS X operating system.
	 */
	public static final String OS_MACOSX = "macosx";//$NON-NLS-1$

	/**
	 * Constant string (value "unknown") indicating the platform is running on a
	 * machine running an unknown operating system.
	 */
	public static final String OS_UNKNOWN = "unknown";//$NON-NLS-1$

	/**
	 * Constant string (value "x86") indicating the platform is running on an
	 * x86-based architecture.
	 */
	public static final String ARCH_X86 = "x86";//$NON-NLS-1$

	/**
	 * Constant string (value "PA_RISC") indicating the platform is running on
	 * an PA_RISC-based architecture.
	 */
	public static final String ARCH_PA_RISC = "PA_RISC";//$NON-NLS-1$

	/**
	 * Constant string (value "ppc") indicating the platform is running on an
	 * PowerPC-based architecture.
	 */
	public static final String ARCH_PPC = "ppc";//$NON-NLS-1$

	/**
	 * Constant string (value "sparc") indicating the platform is running on an
	 * Sparc-based architecture.
	 */
	public static final String ARCH_SPARC = "sparc";//$NON-NLS-1$

	/**
	 * Constant string (value "x86_64") indicating the platform is running on an
	 * x86 64bit-based architecture.
	 */
	public static final String ARCH_X86_64 = "x86_64";//$NON-NLS-1$

	/**
	 * Constant string (value "ia64") indicating the platform is running on an
	 * IA64-based architecture.
	 */
	public static final String ARCH_IA64 = "ia64"; //$NON-NLS-1$

	/**
	 * Constant string (value "ia64_32") indicating the platform is running on
	 * an IA64 32bit-based architecture.
	 */
	public static final String ARCH_IA64_32 = "ia64_32";//$NON-NLS-1$

	/**
	 * Constant string (value "win32") indicating the platform is running on a
	 * machine using the Windows windowing system.
	 */
	public static final String WS_WIN32 = "win32";//$NON-NLS-1$

	/**
	 * Constant string (value "motif") indicating the platform is running on a
	 * machine using the Motif windowing system.
	 */
	public static final String WS_MOTIF = "motif";//$NON-NLS-1$

	/**
	 * Constant string (value "gtk") indicating the platform is running on a
	 * machine using the GTK windowing system.
	 */
	public static final String WS_GTK = "gtk";//$NON-NLS-1$

	/**
	 * Constant string (value "photon") indicating the platform is running on a
	 * machine using the Photon windowing system.
	 */
	public static final String WS_PHOTON = "photon";//$NON-NLS-1$

	/**
	 * Constant string (value "carbon") indicating the platform is running on a
	 * machine using the Carbon windowing system (Mac OS X).
	 */
	public static final String WS_CARBON = "carbon";//$NON-NLS-1$

	/**
	 * Constant string (value "cocoa") indicating the platform is running on a
	 * machine using the Cocoa windowing system (Mac OS X).
	 */
	public static final String WS_COCOA = "cocoa";//$NON-NLS-1$

	/**
	 * Constant string (value "wpf") indicating the platform is running on a
	 * machine using the WPF windowing system.
	 */
	public static final String WS_WPF = "wpf";//$NON-NLS-1$

	/**
	 * Constant string (value "unknown") indicating the platform is running on a
	 * machine running an unknown windowing system.
	 */
	public static final String WS_UNKNOWN = "unknown";//$NON-NLS-1$

	
	/**
	 * Lists of all  possible values of OS, ARCH and WS parameter
	 */
	List<String> _list_OS=new ArrayList<String>(Arrays.asList(OS_AIX,OS_HPUX,OS_LINUX,OS_MACOSX,OS_QNX,OS_SOLARIS,OS_UNKNOWN,OS_WIN32));
	List<String> _list_ARCH=new ArrayList<String>(Arrays.asList(ARCH_IA64,ARCH_IA64_32,ARCH_PA_RISC,ARCH_PPC,ARCH_SPARC,ARCH_X86,ARCH_X86_64));
	List<String> _list_WS=new ArrayList<String>(Arrays.asList(WS_CARBON,WS_COCOA,WS_GTK,WS_MOTIF,WS_PHOTON,WS_UNKNOWN,WS_WIN32,WS_WPF));

}