//
//  LFXLog.java
//  LIFX
//
//  Created by Jarrod Boyes on 24/03/14.
//  Copyright (c) 2014 LIFX Labs. All rights reserved.
//

package lifx.java.android.util;

import android.util.Log;

public class LFXLog {
    private static boolean info = true;
    private static boolean error = true;
    private static boolean warning = true;
    private static boolean verbose = true;
    private static boolean debug = true;

    public static void i(String tag, String input) {
        if (info) Log.i(tag, input);
    }

    public static void e(String tag, String input) {
        if (error) Log.e(tag, input);
    }

    public static void w(String tag, String input) {
        if (warning) Log.w(tag, input);
    }

    public static void v(String tag, String input) {
        if (verbose) Log.v(tag, input);
    }

    public static void d(String tag, String input) {
        if (debug) Log.d(tag, input);
    }

    public static boolean isInfoEnabled() {
        return info;
    }

    public static void setInfoLogging(boolean info) {
        LFXLog.info = info;
    }

    public static boolean isErrorEnabled() {
        return error;
    }

    public static void setErrorLogging(boolean error) {
        LFXLog.error = error;
    }

    public static boolean isWarningEnabled() {
        return warning;
    }

    public static void setWarningLogging(boolean warning) {
        LFXLog.warning = warning;
    }

    public static boolean isVerboseEnabled() {
        return verbose;
    }

    public static void setVerboseLogging(boolean verbose) {
        LFXLog.verbose = verbose;
    }

    public static boolean isDebugEnabled() {
        return debug;
    }

    public static void setDebugLogging(boolean debug) {
        LFXLog.debug = debug;
    }
}
