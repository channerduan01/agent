package com.channer;

import com.channer.model.CampaignTrackModel;
import com.channer.model.EvaluateModel;
import com.channer.model.MarketModel;
import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.publisher.AdxPublisherReport;

import java.util.*;

/**
 * Created by channerduan on 11/20/15.
 */
public class CampaignCore {

    private MarketModel marketModel = MarketModel.getInstance();
    private Map<Integer, CampaignData> myCampaigns;
    private List<Integer> myActiveCampaignIndexs = new ArrayList<>();

    private int today;
    private double quality;
    private double ucsBid;

    public void initialCampaign(CampaignData campaignData) {
        clear();
        myCampaigns.put(campaignData.id, campaignData);
        myActiveCampaignIndexs.add(campaignData.id);
        marketModel.addCampaignTracker(new CampaignTrackModel(campaignData, CampaignTrackModel.RANDOM_US));
    }

    public void initMarket(PublisherCatalog publisherCatalog) {
        List<String> names = new ArrayList<>();
        for (PublisherCatalogEntry pce : publisherCatalog) {
            names.add(pce.getPublisherName());
        }
        marketModel.init(names);
    }

    public void updateDay(int day) {
        today = day;
    }

    private int lastbidId;
    private long lastbidPrice;

    // store all evaluations for campaign
    private List<EvaluateModel> evaluateList = new ArrayList<>();

    private double bidCampaignDefault = 0.4d;
    private boolean isDesire = false;

    public long bidForNewCampaign(CampaignData com) {
        long cmpBidMillis;
        EvaluateModel evaluate = marketModel.evaluateMarket(com.targetSegment,
                (double) com.reachImps / (double) (com.dayEnd - com.dayStart + 1)
                , com.mobileCoef, com.videoCoef, today);
        evaluateList.add(evaluate);
        System.out.println("!!!!!!" + evaluate.showEvaluate());
        long upper = CampaignBidUtil.getCamBidUpperBound(com.reachImps, quality);
        long bottom = CampaignBidUtil.getCamBidBottomBound(com.reachImps, quality);
        double range = upper - bottom;
        double emptyRatio = 1.2d - evaluate.pressurePreRatio - evaluate.requireRatio;
        isDesire = false;
        if (emptyRatio < 0) {
            cmpBidMillis = upper;
        } else {
            emptyRatio /= 1.2d;
            cmpBidMillis = bottom + (long) (range * quality * bidCampaignDefault * (1d - emptyRatio));
            isDesire = true;
        }

        lastbidId = com.id;
        lastbidPrice = cmpBidMillis;
        return cmpBidMillis;
    }

    private double bidUcsDefault = 0.12d;
    private boolean shouldBid = true;

    public double bidForUCS() {
        if (!shouldBid) return 0;
        return bidUcsDefault;
    }

    public AdxBidBundle bidForExchangeX() {
        int dayBiddingFor = today + 1;
        List<CampaignData> campaignList = getActiveCampaign(dayBiddingFor);
        return marketModel.createBidBundle(campaignList, today);
    }

    private List<CampaignData> getActiveCampaign(int day) {
        List<CampaignData> list = new ArrayList<>();
        CampaignData tmp;
        for (Integer integer : myActiveCampaignIndexs) {
            tmp = myCampaigns.get(integer);
            if (tmp.dayStart <= day && tmp.dayEnd >= day && tmp.impsTogo() > 0) {
                list.add(tmp);
            }
        }

        // reset the priority
        int length = list.size();
        for (int i = 0; i < length - 1; i++)
            for (int j = i + 1; j < length; j++) {
                if (list.get(i).targetSegment.size() < list.get(j).targetSegment.size() ||
                        (list.get(i).targetSegment.size() == list.get(j).targetSegment.size() &&
                                list.get(i).dayEnd > list.get(j).dayEnd)) {
                    tmp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, tmp);
                }
            }
        return list;
    }

    public void weWonCampaign(CampaignData campaign, double cost) {
        myCampaigns.put(campaign.id, campaign);

        if (campaign.id == lastbidId && campaign.budget == lastbidPrice)
            marketModel.addCampaignTracker(new CampaignTrackModel(campaign, CampaignTrackModel.RANDOM_US));
        else {
            marketModel.addCampaignTracker(new CampaignTrackModel(campaign, CampaignTrackModel.WON_US));
            if (isDesire) {
                bidCampaignDefault *= 1.3d;
                showBidCampaignFactor("add");
            }
        }

        myActiveCampaignIndexs.add(campaign.id);
    }

    public void otherWonCampaign(CampaignData campaign, String userMark) {
        campaign.budget = lastbidPrice;
        marketModel.addCampaignTracker(new CampaignTrackModel(campaign, CampaignTrackModel.WON_OTHERS));
        if (isDesire) {
            bidCampaignDefault /= 1.4d;
            showBidCampaignFactor("reduce");
        }
    }

    private void showBidCampaignFactor(String mark) {
        System.out.println("bidCampaignDefault " + mark + " " + bidCampaignDefault);
    }

    public void updateAdNetworkReport(AdNetworkReport report) {
        marketModel.bidBundleFeedback(report, today);
    }

    public void campaignStateUpdate(int id, CampaignStats cstats) {
        myCampaigns.get(id).setStats(cstats);
        System.out.println("Day " + today + ": Updating campaign " + id + " stats: "
                + cstats.getTargetedImps() + "/"
                + myCampaigns.get(id).reachImps + " Cost of imps is "
                + cstats.getCost() + " budget:" + myCampaigns.get(id).budget);
        if (myCampaigns.get(id).impsTogo() == 0) {
            myActiveCampaignIndexs.remove(myCampaigns.get(id));
        }
    }

    public void updateUCS(double level, double cost) {
        boolean isNeed = false;
        for (Integer integer : myActiveCampaignIndexs) {
            if (myCampaigns.get(integer).dayStart <= today + 1 &&
                    myCampaigns.get(integer).dayEnd > today + 1) {
                isNeed = true;
                break;
            }
        }
        if (!isNeed && today > 1) {
            shouldBid = false;
            return;
        } else
            shouldBid = true;

        ucsBid = level;
        if (ucsBid < 0.78d) {
            bidUcsDefault = bidUcsDefault * 1.2d;
        } else {
            bidUcsDefault = bidUcsDefault / 1.2d;
        }
    }

    public void updateQuality(double q) {
        quality = q;
    }

    public void timeupToday() {
        marketModel.updataDayForCampaignTrackers(today + 1);
        marketModel.updateStateBeforeTheBeginOfNextDay(today + 1);
        List<Integer> removeList = new ArrayList<>();
        for (Integer integer : myActiveCampaignIndexs) {
            if (myCampaigns.get(integer).dayEnd < today + 1) {
                removeList.add(integer);
            }
        }
        myActiveCampaignIndexs.removeAll(removeList);

        // output all the evaluations finally
        if (today >= 60) {
            for (EvaluateModel evaluate : evaluateList) {
                System.out.println(evaluate.showEvaluate());
            }
        }
    }

    public void clear() {
        shouldBid = true;
        ucsBid = 0.2;
        quality = 1.0d;
        today = 0;
        myCampaigns = new HashMap<>();
        myActiveCampaignIndexs.clear();
    }

    public void updatePublsers(AdxPublisherReport report) {
        marketModel.updateByPublisherReport(report);
//        marketModel.printCore();
    }
}
