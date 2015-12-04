package com.channer;

/**
 * Created by channerduan on 12/3/15.
 */
public class MathUtil {
    public static double sigmoid(double src) {
        return 1.0 / (1.0 + Math.exp(-src));
    }
}
