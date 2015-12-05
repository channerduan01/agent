package com.channer.model;

import com.channer.CampaignData;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.BidBundle;
import tau.tac.adx.props.AdxBidBundle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by channerduan on 12/4/15.
 */
public class BidRecordModel {

    public static class BidItem {
        public double basePrice;
        public double factor;
        public double aimRatio;
        public double realRatio;

        public BidItem(double basePrice, double factor, double aimRatio) {
            this.basePrice = basePrice;
            this.factor = factor;
            this.aimRatio = aimRatio;
        }

        @Override
        public String toString() {
            return "(" + basePrice + "," + factor + "," + aimRatio + "," + realRatio + ")";
        }
    }

    public int bidday;
    public int pNum;

    public Map<Integer, BidItem[]> data = new HashMap();

    public BidRecordModel(int bidday, List<CampaignData> campaignList, int segSize, int pNum) {
        this.bidday = bidday;
        this.pNum = pNum;
        BidItem[] items;
        for (CampaignData campaign : campaignList) {
            items = new BidItem[segSize * pNum];
            data.put(campaign.id, items);
        }
    }

    public void add(CampaignData campaign, int sigment, double meetFactor, double expectRatio,
                    double eM, double eV, SegmentModel model) {
        BidItem[] items = data.get(campaign.id);
        items[sigment * pNum + 0] = new BidItem(meetFactor * model.p1 * eM * eV
                , campaign.timePressure, expectRatio);
        items[sigment * pNum + 1] = new BidItem(meetFactor * model.p2 * eM
                , campaign.timePressure, expectRatio);
        items[sigment * pNum + 2] = new BidItem(meetFactor * model.p3 * eV
                , campaign.timePressure, expectRatio);
        items[sigment * pNum + 3] = new BidItem(meetFactor * model.p4
                , campaign.timePressure, expectRatio);
    }

    public void print() {
        BidItem[] items;
        for (Map.Entry<Integer, BidItem[]> entry : data.entrySet()) {
            System.out.print("BidRecord[" + entry.getKey() + "] ");
            items = entry.getValue();
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null)
                    System.out.print("" + i + "," + items[i].toString());
            }
            System.out.print("\n");
        }
    }

    public AdxBidBundle createBundle(List<CampaignData> list, QueryModel[] queryModels,
                                     int publisherNum, double board[][]) {
        AdxBidBundle bundle = new AdxBidBundle();
        BidItem[] items;
        CampaignData campaign;
        for (int ii = 0; ii < list.size(); ii++) {
            campaign = list.get(ii);
            items = data.get(campaign.id);
            if (items == null) continue;
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    for (int j = 0; j < publisherNum; j++) {
                        bundle.addQuery(queryModels[(i / 4) * publisherNum * 4 + j * 4 + i % 4].adxQuery,
                                items[i].basePrice * items[i].factor * 1000d,
                                new Ad(null), campaign.id, (int) (board[i / 4][ii] + 0.5d));
                    }
                }
            }
            // limitation
            double impressionLimit = campaign.impsTogo() / (double) (campaign.dayEnd - bidday + 1) * campaign.timePressure;
            double budgetLimit = campaign.budget * 2;
            bundle.setCampaignDailyLimit(campaign.id,
                    (int) impressionLimit, budgetLimit);
        }
        return bundle;
    }

}
