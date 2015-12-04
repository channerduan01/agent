package com.channer.model;

import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by channerduan on 11/20/15.
 */
public class SegmentModel {

    public static Set<MarketSegment> mapBackMarketSegment(int markNumber) {
        Set<MarketSegment> segments = new HashSet<>();
        if (markNumber % 2 == 1) {
            segments.add(MarketSegment.FEMALE);
        } else {
            segments.add(MarketSegment.MALE);
        }
        if (markNumber > 5 || markNumber == 2 || markNumber == 3) {
            segments.add(MarketSegment.HIGH_INCOME);
        } else {
            segments.add(MarketSegment.LOW_INCOME);
        }
        if (markNumber > 3) {
            segments.add(MarketSegment.OLD);
        } else {
            segments.add(MarketSegment.YOUNG);
        }
        return segments;
    }

    public static int mapSingleMarketSegment(Age age, Income income, Gender gender) {
        int res = 0;
        if (gender == Gender.male) {
            res += 0;
        } else {
            res += 1;
        }
        if (income == Income.low || income == Income.medium) {
            res += 0;
        } else {
            res += 2;
        }
        if (age == Age.Age_18_24 || age == Age.Age_25_34 || age == Age.Age_35_44) {
            res += 0;
        } else {
            res += 4;
        }
        return res;
    }

    public static int mapSingleMarketSegment(MarketSegment age, MarketSegment income, MarketSegment gender) {
        int res = 0;
        if (gender == MarketSegment.MALE) {
            res += 0;
        } else {
            res += 1;
        }
        if (income == MarketSegment.LOW_INCOME) {
            res += 0;
        } else {
            res += 2;
        }
        if (age == MarketSegment.YOUNG) {
            res += 0;
        } else {
            res += 4;
        }
        return res;
    }

    public static List<Integer> mapMarketSegment(Set<MarketSegment> segments) {
        List<Integer> res = new ArrayList<>();
        List<Integer> tmpList = new ArrayList<>();
        int i;
        if (segments.contains(MarketSegment.MALE)) {
            res.add(0);
        } else if (segments.contains(MarketSegment.FEMALE)) {
            res.add(1);
        } else {
            res.add(0);
            res.add(1);
        }
        if (segments.contains(MarketSegment.LOW_INCOME)) {
        } else if (segments.contains(MarketSegment.HIGH_INCOME)) {
            for (i = 0; i < res.size(); i++) res.set(i, res.get(i) + 2);
        } else {
            tmpList.clear();
            for (i = 0; i < res.size(); i++) {
                tmpList.add(res.get(i) + 2);
            }
            res.addAll(tmpList);
        }
        if (segments.contains(MarketSegment.YOUNG)) {
        } else if (segments.contains(MarketSegment.OLD)) {
            for (i = 0; i < res.size(); i++) res.set(i, res.get(i) + 4);
        } else {
            tmpList.clear();
            for (i = 0; i < res.size(); i++) {
                tmpList.add(res.get(i) + 4);
            }
            res.addAll(tmpList);
        }
        return res;
    }


    public int index;
    public List<MarketFragmentModel> fragments = new ArrayList<>();
    public double population;
    public double avgMRatio;
    public double avgVRatio;
    public List<CampaignPressureModel> campaignPressurs = new ArrayList<>();

    public double p1, p2, p3, p4;
    private static final double QUERY_BASIC_PRICE[] = {500, 0.035, 0.035, 0.02};

    public SegmentModel(int index, List<MarketFragmentModel> fragments) {
        campaignPressurs.clear();
        this.index = index;
        this.fragments.clear();
        this.fragments.addAll(fragments);
        updateDataModel();

        p1 = QUERY_BASIC_PRICE[0];
        p2 = QUERY_BASIC_PRICE[1];
        p3 = QUERY_BASIC_PRICE[2];
        p4 = QUERY_BASIC_PRICE[3];
    }

    public void updateDataModel() {
        avgMRatio = 0;
        avgVRatio = 0;
        population = 0;
        for (MarketFragmentModel frag : fragments) {
            population += frag.population;
        }
        for (MarketFragmentModel frag : fragments) {
            avgMRatio += frag.population / population * frag.publisher.mRatio;
            avgVRatio += frag.population / population * frag.publisher.vRatio;
        }
        for (MarketFragmentModel frag : fragments) {
            frag.updateModel();
        }
    }

    public void addCampaignPressure(CampaignPressureModel pressure) {
        campaignPressurs.add(pressure);
    }

    public void deleteCampaignPressure(CampaignPressureModel pressure) {
        campaignPressurs.remove(pressure);
    }

    public static double impTransFactor(double rM, double eM, double rV, double eV) {
        double crossRatio = rM * rV;
        return ((crossRatio * eM * eV)
                + ((rM - crossRatio) * eM)
                + ((rV - crossRatio) * eV)
                + ((1.0d - rM) * (1.0d - rV)));
    }

    public double calcuImpression(double mEffect, double vEffect) {
        double require = population * impTransFactor(avgMRatio, mEffect, avgVRatio, vEffect);
        return require;
    }

    public double calcuAvgPressure(double mEffect, double vEffect) {
        double require = 0d;
        for (CampaignPressureModel pressure : campaignPressurs) {
            require += pressure.impression * pressure.getExtraFactor() / impTransFactor(avgMRatio, mEffect, avgVRatio, vEffect);
        }
        return require;
    }

    public double calcuAllPressure(double mEffect, double vEffect, int today) {
        double require = 0d;
        double population;
        for (CampaignPressureModel pressure : campaignPressurs) {
            population = pressure.impression * (double) (pressure.trackModel.expiredDay - today + 1);
            require += population / impTransFactor(avgMRatio, mEffect, avgVRatio, vEffect);
        }
        return require;
    }

    public double populationToughPressure = 0d;

    public void updatePressurePrediction() {
        double[] factors = calcuAvgEffectFactors();
        double populationPressure = factors[0];
        if (populationPressure != 0) {
            populationPressure /= impTransFactor(avgMRatio, factors[1], avgVRatio, factors[2]);
        }
        double ratio = populationPressure / population;
        if (ratio > 1d) ratio = 1d;
        populationToughPressure = 0.5d * ratio;
    }

    private double[] calcuAvgEffectFactors() {
        double imp = 0;
        for (CampaignPressureModel pressure : campaignPressurs)
            if (pressure.trackModel.property == CampaignTrackModel.WON_OTHERS)
                imp += pressure.impression;
        double[] res = new double[3];
        res[0] = imp;
        res[1] = res[2] = 0;
        for (CampaignPressureModel pressure : campaignPressurs)
            if (pressure.trackModel.property == CampaignTrackModel.WON_OTHERS) {
                res[1] += pressure.impression / imp * pressure.mEffect;
                res[2] += pressure.impression / imp * pressure.vEffect;
            }
        return res;
    }


    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Segment-")
                .append(index)
                .append(" ratio(").append(avgMRatio).append(",").append(avgVRatio).append(")")
                .append(" impressions:").append(calcuImpression(1.0d, 1.0d));

        buffer.append("\n    pressures:").append(campaignPressurs.size()).append(" ");
        for (CampaignPressureModel pressure : campaignPressurs) buffer.append(pressure.toString());
        buffer.append("\n    frag:").append(fragments.size()).append(" ");
        for (MarketFragmentModel fragment : fragments)
            buffer.append(" ").append(fragment.publisher.hashCode()).append(" ").append(fragment.toString());

        return buffer.toString();
    }

}
