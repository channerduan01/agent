package com.channer;

import com.channer.CampaignData;
import com.channer.model.MarketModel;

/**
 * Created by channerduan on 11/14/15.
 */
public class CampaignBidUtil {

    /*
        Bid limited bound
     */
    private static final double RcampaignMax = 0.001d;
    private static final double RcampaignMin = 0.0001d;
    private static final double[] CampaignDensity = {
    				/*	0.2		0.5		0.8*/
    		/*3*/		0.9d,	1.0d,	1.5d,
    		/*5*/		0.9d,	1.0d,	1.4d,
    		/*10*/		0.8d,	1.0d,	1.8d
    };

    public static long getCamBidUpperBound(long cReach, double quality) {
        return (long) Math.floor(((double) cReach) * 1000d * RcampaignMax * quality) - 1;
    }

    public static long getCamBidBottomBound(long cReach, double quality) {
        return (long) Math.ceil(((double) cReach) * 1000d * RcampaignMin / quality) + 1;
    }
    
    public static double classifyCampaign(long days, long reach, double size){
    	double dens = reach/(days*size);
    	if (days == 10 && dens >= 0.5 ){
    	dens *= 1.2;
    	}    		
    	return dens;
    }
}
