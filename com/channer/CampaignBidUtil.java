package com.channer;

import com.channer.CampaignData;
import com.channer.model.MarketModel;
import com.channer.FuzzyFunction;

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
    		/*3*/		0.9d,	1.0d,	1.4d,
    		/*5*/		0.9d,	1.0d,	1.3d,
    		/*10*/		0.8d,	1.0d,	1.5d
    };
    private static double Wintrend = 0.0d;
    private static double Losetrend = 0.0d;
    private static FuzzyFunction[] fuzzy = new FuzzyFunction[5];

    public static long getCamBidUpperBound(long cReach, double quality) {
        return (long) Math.floor(((double) cReach) * 1000d * RcampaignMax * quality) - 1;
    }

    public static long getCamBidBottomBound(long cReach, double quality) {
        return (long) Math.ceil(((double) cReach) * 1000d * RcampaignMin / quality);
    }
    
    /*
     * Modified: Haoyang
     */
    
    public static void initialFuzzy(double max)
    {
    	fuzzy[0] = new FuzzyFunction(0.0d,0.2*max,0.8);
    	fuzzy[1] = new FuzzyFunction(0.1*max,0.2*max,0.3*max,0.4*max,1.0);
    	fuzzy[2] = new FuzzyFunction(0.3*max,0.4*max,0.7*max,0.8*max,1.2);
    	fuzzy[3] = new FuzzyFunction(0.7*max,0.8*max,0.9*max,max,5);
    	fuzzy[4] = new FuzzyFunction(0.9*max,max,10);
    	fuzzy[4].setDir(false);
    }
    
    public static double classifyCampaign(double duration, long reach, double size){
    	double dens = reach/(duration*size);
    	//Get dens index from dens
    	int densIndex =(int) (dens*10 / 3.5d);
    	//Get factor index from days
    	int dayIndex = (int)duration/5;
    	return CampaignDensity[dayIndex*3+densIndex];
    }
    
    public static double fuzzyAdjust(double densRatio){
    	double max = 1*CampaignDensity[8];
    	if (fuzzy[0] == null)
    		initialFuzzy(max);
    	double fuzzyValue = 0.0d;
    	for (int i =0; i<5; ++i)
    	{
    		fuzzyValue += fuzzy[i].getFuzzyValue(densRatio);
    	}
    	return fuzzyValue;
    }
    
    public static double updateAdjustor(double adjustor, boolean won)
    {
    	if (won){
    		adjustor *= Math.pow(1.15, Wintrend);
    		Wintrend +=1;
    		Losetrend = Math.floor(Losetrend/3);
    	}else{
    		adjustor = Math.pow(0.85, Losetrend);
    		Losetrend += 1;
    		Wintrend = Math.floor(Wintrend/2);
    	}
    	return adjustor;
    }
    
    public static double updateBasic(double basic, double adjustor){
    	basic = basic * adjustor;
    	return basic;
    }
  
    
    public static double getTrends(boolean won)
    {
    	if (won) return Wintrend;
    	else return Losetrend;
    }
}
