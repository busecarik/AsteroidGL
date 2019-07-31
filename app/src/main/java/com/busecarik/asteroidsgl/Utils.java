package com.busecarik.asteroidsgl;

import android.util.Log;

public class Utils {

    public static final double TO_DEG = 180.0/Math.PI;
    public static final double TO_RAD = Math.PI/180.0;


    public static void expect(final boolean condition, final String tag) {
        Utils.expect(condition, tag, "Expectation was broken.");
    }
    public static void expect(final boolean condition, final String tag, final String message) {
        if(!condition) {
            Log.e(tag, message);
        }
    }
    public static void require(final boolean condition) {
        Utils.require(condition, "Assertion failed!");
    }
    public static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
