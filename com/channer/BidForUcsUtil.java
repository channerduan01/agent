package com.channer;

import java.util.LinkedHashMap;
import java.util.Map;

public class BidForUcsUtil {

    public static final Integer[] ADJUSTEUCSDLEVEL = {48, 53, 59, 66, 73, 81, 90, 100};
    public static final Double[] ADJUSTEDUCSCOST = {0d, 0.06d, 0.068d, 0.078d, 0.117d, 0.135d, 0.148d, 0.174d};

    public static Map<Integer, Double> calcuMarginUcsLevelUp() {
        Map<Integer, Double> levelUpCostMargin = new LinkedHashMap<Integer, Double>();
        Double temp;
        for (int i = 0; i < ADJUSTEUCSDLEVEL.length-1; i++) {
            temp = ADJUSTEDUCSCOST[i+1] - ADJUSTEDUCSCOST[i];
            levelUpCostMargin.put(ADJUSTEUCSDLEVEL[i], temp);
        }

        return levelUpCostMargin;
    }

}