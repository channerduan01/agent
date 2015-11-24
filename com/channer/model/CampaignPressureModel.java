package com.channer.model;

/**
 * Created by channerduan on 11/21/15.
 */
public class CampaignPressureModel {
    public CampaignTrackModel trackModel;
    public int segmentMark;
    public double impression;
    public double mEffect;
    public double vEffect;
    public int reliefBreadth;

    public CampaignPressureModel(CampaignTrackModel tracker, int segmentMark) {
        this.trackModel = tracker;
        this.segmentMark = segmentMark;
        this.reliefBreadth = tracker.segments.size();
        this.mEffect = tracker.campaignData.mobileCoef;
        this.vEffect = tracker.campaignData.videoCoef;
        this.impression = tracker.avgImpressions / reliefBreadth;
    }

    public double getExtraFactor() {
        if (trackModel.property != CampaignTrackModel.WON_OTHERS) {
            return 1.3d;
        } else {
            return 1d;
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer
                .append(" Pre-").append(trackModel.campaignData.id).append(":")
                .append(" ").append(segmentMark)
                .append(" ").append(reliefBreadth)
                .append(" ").append(impression)
                .append(" ").append(mEffect)
                .append(" ").append(vEffect)
                .append(";");
        return buffer.toString();
    }
}
