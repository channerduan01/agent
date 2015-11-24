package com.channer;

/**
 * Created by channerduan on 11/14/15.
 */
public class CampaignBidUtil {

    /*
        Bid limited bound
     */
    private static final double RcampaignMax = 0.001d;
    private static final double RcampaignMin = 0.0001d;

    public static long getCamBidUpperBound(long cReach, double quality) {
        return (long) Math.floor(((double) cReach) * 1000d * RcampaignMax * quality) - 1;
    }

    public static long getCamBidBottomBound(long cReach, double quality) {
        return (long) Math.ceil(((double) cReach) * 1000d * RcampaignMin / quality) + 1;
    }
}
