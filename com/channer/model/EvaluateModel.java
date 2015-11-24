package com.channer.model;

/**
 * Created by channerduan on 11/23/15.
 */
public class EvaluateModel {
    private double basicUserSourceNum;
    private double pureRequire;
    private double pressure;
    public double pressureLength;

    public double requireRatio;
    public double pressurePreRatio;

    public String showEvaluate() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("require-").append(requireRatio)
                .append(" pressure-").append(pressurePreRatio)
                .append(" lenOfPre-").append(pressureLength);
        return buffer.toString();
    }

    public EvaluateModel(double b, double pu, double pr, double preLen, int today) {
        basicUserSourceNum = b;
        pureRequire = pu;
        pressure = pr;
        pressureLength = preLen;

        requireRatio = pureRequire / basicUserSourceNum;
        pressurePreRatio = pressure / basicUserSourceNum;

        // special method to optimize initial period
        if (today <= 7) {
            pressurePreRatio += 0.1d;
            pressureLength += 1;
        }
        if (today <= 5) {
            pressurePreRatio += 0.1d;
            pressureLength += 1;
        }
        if (today <= 3) {
            pressurePreRatio += 0.05d;
            pressureLength += 1;
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Evaluate-")
                .append(" basicUserSourceNum:").append(basicUserSourceNum)
                .append(" pureRequire:").append(pureRequire)
                .append(" pre:").append(pressure)
                .append(" preLen:").append(pressureLength)
        ;
        return buffer.toString();
    }
}
