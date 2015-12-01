package com.channer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by channerduan on 11/22/15.
 */
public class BidBundleUtil {

    public static double calcuTimePressure(CampaignData campaign, int today) {
        double camLength = campaign.dayEnd - campaign.dayStart + 1d;
        double camDayLeft = campaign.dayEnd - today;
        double avgMinForWhole = campaign.reachImps / camLength;
        double avgMinForNow = campaign.impsTogo() / camDayLeft;

        double timeFactor = avgMinForNow / avgMinForWhole;

        return timeFactor;
    }

    public static double calcuFinishRatio(CampaignData campaign) {
        return campaign.stats.getTargetedImps() / (double) campaign.reachImps;
    }

    public static double calcuCostRatio(CampaignData campaign) {
        return campaign.stats.getCost() / campaign.budget;
    }

    /**
     * the larger the better
     *
     * @param campaign
     * @return
     */
    public static double calcuProfitRatio(CampaignData campaign) {
        if (calcuCostRatio(campaign) == 0)
            return 1d;
        return calcuFinishRatio(campaign) / calcuCostRatio(campaign);
    }


    /**
     * return the campaigns which we want to give up
     *
     * @param dataList
     * @return
     */
    public static List<CampaignData> costControlFilterForMycampaigns(List<CampaignData> dataList, int today) {
        System.out.println("!!!!!! BidBundle Cost check");
        int length = dataList.size();
        int i, j;
        CampaignData tmp;
        for (i = 0; i < length - 1; i++)
            for (j = i + 1; j < length; j++)
                if (dataList.get(i).dayEnd > dataList.get(j).dayEnd) {
                    tmp = dataList.get(i);
                    dataList.set(i, dataList.get(j));
                    dataList.set(j, tmp);
                }

        List<CampaignData> giveupList = new ArrayList<>();
        List<CampaignData> tmpList = new ArrayList<>();
        double avgPressure = 0;
        double avgProfit = 0;
        for (i = 0; i < length; i++) {
            System.out.println(dataList.get(i).toString());
            if (dataList.get(i).dayStart < today) {
                avgPressure += dataList.get(i).timePressure;
                avgProfit += dataList.get(i).profitRatio;
                tmpList.add(dataList.get(i));
            }
        }
        if (!tmpList.isEmpty()) {
            avgPressure /= tmpList.size();
            avgProfit /= tmpList.size();
            System.out.println("!!!!!! avgPressure:" + avgPressure +
                    " avgProfit:" + avgProfit);
            for (CampaignData campaign : tmpList) {
                if (campaign.profitRatio / avgProfit > 2 &&
                        campaign.timePressure / avgPressure > 2 &&
                        campaign.profitRatio > 2 &&
                        campaign.timePressure > 1.5d &&
                        dataList.indexOf(campaign) != dataList.size() - 1
                        ) {
                    giveupList.add(campaign);
                }
            }
            for (CampaignData campaign : giveupList)
                System.out.println("give up id:" + campaign.id);
        }
        return giveupList;
    }

}
