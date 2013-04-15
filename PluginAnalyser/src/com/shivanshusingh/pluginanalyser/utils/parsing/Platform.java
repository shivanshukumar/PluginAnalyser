package com.shivanshusingh.pluginanalyser.utils.parsing;

public interface Platform {

	/**
	 * Constant string (value "win32") indicating the platform is running on a
	 * Window 32-bit operating system (e.g., Windows 98, NT, 2000).
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String OS_WIN32 = "win32";//$NON-NLS-1$

	/**
	 * Constant string (value "linux") indicating the platform is running on a
	 * Linux-based operating system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String OS_LINUX = "linux";//$NON-NLS-1$

	/**
	 * Constant string (value "aix") indicating the platform is running on an
	 * AIX-based operating system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String OS_AIX = "aix";//$NON-NLS-1$

	/**
	 * Constant string (value "solaris") indicating the platform is running on a
	 * Solaris-based operating system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String OS_SOLARIS = "solaris";//$NON-NLS-1$

	/**
	 * Constant string (value "hpux") indicating the platform is running on an
	 * HP/UX-based operating system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String OS_HPUX = "hpux";//$NON-NLS-1$

	/**
	 * Constant string (value "qnx") indicating the platform is running on a
	 * QNX-based operating system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String OS_QNX = "qnx";//$NON-NLS-1$

	/**
	 * Constant string (value "macosx") indicating the platform is running on a
	 * Mac OS X operating system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String OS_MACOSX = "macosx";//$NON-NLS-1$

	/**
	 * Constant string (value "unknown") indicating the platform is running on a
	 * machine running an unknown operating system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String OS_UNKNOWN = "unknown";//$NON-NLS-1$

	/**
	 * Constant string (value "x86") indicating the platform is running on an
	 * x86-based architecture.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String ARCH_X86 = "x86";//$NON-NLS-1$

	/**
	 * Constant string (value "PA_RISC") indicating the platform is running on
	 * an PA_RISC-based architecture.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String ARCH_PA_RISC = "PA_RISC";//$NON-NLS-1$

	/**
	 * Constant string (value "ppc") indicating the platform is running on an
	 * PowerPC-based architecture.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String ARCH_PPC = "ppc";//$NON-NLS-1$

	/**
	 * Constant string (value "sparc") indicating the platform is running on an
	 * Sparc-based architecture.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String ARCH_SPARC = "sparc";//$NON-NLS-1$

	/**
	 * Constant string (value "x86_64") indicating the platform is running on an
	 * x86 64bit-based architecture.
	 * 
	 * @since 3.1
	 */
	public static final String ARCH_X86_64 = "x86_64";//$NON-NLS-1$

	/**
	 * Constant string (value "ia64") indicating the platform is running on an
	 * IA64-based architecture.
	 * 
	 * @since 3.0
	 */
	public static final String ARCH_IA64 = "ia64"; //$NON-NLS-1$

	/**
	 * Constant string (value "ia64_32") indicating the platform is running on
	 * an IA64 32bit-based architecture.
	 * 
	 * @since 3.1
	 */
	public static final String ARCH_IA64_32 = "ia64_32";//$NON-NLS-1$

	/**
	 * Constant string (value "win32") indicating the platform is running on a
	 * machine using the Windows windowing system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String WS_WIN32 = "win32";//$NON-NLS-1$

	/**
	 * Constant string (value "motif") indicating the platform is running on a
	 * machine using the Motif windowing system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String WS_MOTIF = "motif";//$NON-NLS-1$

	/**
	 * Constant string (value "gtk") indicating the platform is running on a
	 * machine using the GTK windowing system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String WS_GTK = "gtk";//$NON-NLS-1$

	/**
	 * Constant string (value "photon") indicating the platform is running on a
	 * machine using the Photon windowing system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String WS_PHOTON = "photon";//$NON-NLS-1$

	/**
	 * Constant string (value "carbon") indicating the platform is running on a
	 * machine using the Carbon windowing system (Mac OS X).
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String WS_CARBON = "carbon";//$NON-NLS-1$

	/**
	 * Constant string (value "cocoa") indicating the platform is running on a
	 * machine using the Cocoa windowing system (Mac OS X).
	 * 
	 * @since 3.5
	 */
	public static final String WS_COCOA = "cocoa";//$NON-NLS-1$

	/**
	 * Constant string (value "wpf") indicating the platform is running on a
	 * machine using the WPF windowing system.
	 * 
	 * @since 3.3
	 */
	public static final String WS_WPF = "wpf";//$NON-NLS-1$

	/**
	 * Constant string (value "unknown") indicating the platform is running on a
	 * machine running an unknown windowing system.
	 * <p>
	 * Note this constant has been moved from the deprecated
	 * org.eclipse.core.boot.BootLoader class and its value has not changed.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String WS_UNKNOWN = "unknown";//$NON-NLS-1$

}