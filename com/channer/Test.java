package com.channer;


import com.channer.model.MarketModel;
import com.channer.model.PublisherModel;
import com.channer.model.SegmentModel;
import tau.tac.adx.report.adn.MarketSegment;

import java.util.*;


public class Test {
    public static void main(String[] args) {
//        Set<MarketSegment> segments = new HashSet<>();
//        segments.add(MarketSegment.MALE);
//        segments.add(MarketSegment.OLD);
//        segments.add(MarketSegment.HIGH_INCOME);
//
//        List<Integer> list = SegmentModel.mapMarketSegment(segments);
//
//        for (Integer integer : list) System.out.println("!!!:" + integer);
//
//
//        List<String> listName = new ArrayList();
//        listName.add("amazon");
//        listName.add("cnn");
//
//        MarketModel.getInstance().init(listName);
//
//        MarketModel.getInstance().printCore();
//
//        if (1 == 1) return;

//        System.out.println("" + BidBundleUtil.calcuCoefOfMeet(0.22));
//        System.out.println("" + BidBundleUtil.calcuCoefOfMeet(0.29));
//
//        System.out.println("" + BidBundleUtil.calcuCoefOfMeet(1d));
//        System.out.println("" + BidBundleUtil.calcuCoefOfMeet(2d));
//        System.out.println("" + BidBundleUtil.calcuCoefOfMeet(3d));

        System.out.println("Hello channer!");
        try {
            tau.tac.adx.agentware.Main.main(new String[]{"-config", "config/aw-1.conf"});

           // tau.tac.adx.agentware.Main.main(new String[]{"-config", "config/aw-1.conf"});
        } catch (Exception e) {
            System.out.println("IO exception!");
        }
    }
}
