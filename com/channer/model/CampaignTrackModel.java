package com.channer.model;

import com.channer.CampaignData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by channerduan on 11/20/15.
 */
public class CampaignTrackModel {

    public static final int RANDOM_US = 0;
    public static final int WON_US = 1;
    public static final int WON_OTHERS = 2;

    public int property;
    public CampaignData campaignData;
    public List<Integer> segments;
    public long dayLong;
    public double avgImpressions;
    public double avgCost;
    public long expiredDay;

    public List<CampaignPressureModel> pressureList = new ArrayList<>();

    public CampaignTrackModel(CampaignData cam, int property) {
        this.property = property;
        this.campaignData = cam;
        this.segments = SegmentModel.mapMarketSegment(cam.targetSegment);
        this.expiredDay = cam.dayEnd;
        this.dayLong = (cam.dayEnd-cam.dayStart+1);
        this.avgImpressions = (double)cam.reachImps / (double)dayLong;
        this.avgCost = cam.budget / (double)dayLong;

        pressureList.clear();
        CampaignPressureModel pressure;
        for (Integer integer : segments) {
            pressure = new CampaignPressureModel(this, integer);
            pressureList.add(pressure);
        }
    }

}
