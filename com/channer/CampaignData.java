package com.channer;

import com.channer.model.SegmentModel;
import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.InitialCampaignMessage;

import java.util.List;
import java.util.Set;

/**
 * Created by channerduan on 11/20/15.
 */
public class CampaignData {
    /* campaign attributes as set by server */
    public Long reachImps;
    public long dayStart;
    public long dayEnd;
    public Set<MarketSegment> targetSegment;
    public double videoCoef;
    public double mobileCoef;
    public int id;
    public AdxQuery[] campaignQueries;//array of queries relvent for the campaign.

    /* campaign info as reported */
    public CampaignStats stats;
    public double budget;

    /* Special ratio */
    public double profitRatio;
    public double timePressure;

    public double impressionNeedAvg;
    public List<Integer> segments;

    public void updateRatio(int today) {
        timePressure = BidBundleUtil.calcuTimePressure(this, today);
        profitRatio = BidBundleUtil.calcuProfitRatio(this);
        impressionNeedAvg = (double)impsTogo() / (double)(dayEnd-today+1);
    }

    public CampaignData(InitialCampaignMessage icm) {
        reachImps = icm.getReachImps();
        dayStart = icm.getDayStart();
        dayEnd = icm.getDayEnd();
        targetSegment = icm.getTargetSegment();
        segments = SegmentModel.mapMarketSegment(targetSegment);

        videoCoef = icm.getVideoCoef();
        mobileCoef = icm.getMobileCoef();
        id = icm.getId();

        stats = new CampaignStats(0, 0, 0);
        budget = 0.0;
    }

    public void setBudget(double d) {
        budget = d;
    }

    public CampaignData(CampaignOpportunityMessage com) {
        dayStart = com.getDayStart();
        dayEnd = com.getDayEnd();
        id = com.getId();
        reachImps = com.getReachImps();
        targetSegment = com.getTargetSegment();
        segments = SegmentModel.mapMarketSegment(targetSegment);
        mobileCoef = com.getMobileCoef();
        videoCoef = com.getVideoCoef();
        stats = new CampaignStats(0, 0, 0);
        budget = 0.0;
    }

    @Override
    public String toString() {
        return "Campaign ID " + id + ": " + " " + dayStart + "-"
                + dayEnd + " " + targetSegment + ", reach: " + stats.getTargetedImps()+ "/" +reachImps
                + " pressure:" + timePressure + " pRatio:" + profitRatio
                + " coefs: (v=" + videoCoef + ", m=" + mobileCoef + ")";
    }

    public int impsTogo() {
        return (int) Math.max(0, reachImps - stats.getTargetedImps());
    }

    public void setStats(CampaignStats s) {
        stats.setValues(s);
    }

    public AdxQuery[] getCampaignQueries() {
        return campaignQueries;
    }

    public void setCampaignQueries(AdxQuery[] campaignQueries) {
        this.campaignQueries = campaignQueries;
    }

}
