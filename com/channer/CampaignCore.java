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

	private Map<Integer, CampaignData> otherCampaigns;
	private List<Integer> otherActiveCampaignIndexs = new ArrayList<>();

	private int today;
	private double quality;
	private double ucsLevel;

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

	private double bidCampaignDefault = 0.5d;
	private boolean isDesire = false;

	public long bidForNewCampaign(CampaignData com) {
		long cmpBidMillis;
		EvaluateModel evaluate = marketModel.evaluateMarket(com.targetSegment,
				(double) com.reachImps / (double) (com.dayEnd - com.dayStart + 1), com.mobileCoef, com.videoCoef,
				today);
		evaluateList.add(evaluate);
		System.out.println("!!!!!!" + evaluate.showEvaluate());
		long upper = CampaignBidUtil.getCamBidUpperBound(com.reachImps, quality);
		long bottom = CampaignBidUtil.getCamBidBottomBound(com.reachImps, quality);
		double range = upper - bottom;

		double emptyRatio = 1.0d - evaluate.pressurePreRatio - evaluate.requireRatio;
		isDesire = false;
		if (emptyRatio < 0) {
			cmpBidMillis = upper;
		} else {
			emptyRatio /= 1.0d;
			cmpBidMillis = bottom + (long) (range * quality * bidCampaignDefault * (1d - emptyRatio));
			isDesire = true;
		}

		lastbidId = com.id;
		lastbidPrice = cmpBidMillis;
		return cmpBidMillis;
	}

	public AdxBidBundle bidForExchangeX() {
		List<CampaignData> list = getNextDayActiveCampaign();

		List<CampaignData> removelist = BidBundleUtil.costControlFilterForMycampaigns(list, today);
		for (CampaignData campaign : removelist) {
			list.remove(campaign);
			myActiveCampaignIndexs.remove((Integer) campaign.id);
		}
		int length = list.size();
		int i, j;
		CampaignData tmp;
		// reset the priority
		for (i = 0; i < length - 1; i++)
			for (j = i + 1; j < length; j++) {
				if (list.get(i).targetSegment.size() < list.get(j).targetSegment.size()
						|| (list.get(i).targetSegment.size() == list.get(j).targetSegment.size()
								&& list.get(i).dayEnd > list.get(j).dayEnd)) {
					tmp = list.get(i);
					list.set(i, list.get(j));
					list.set(j, tmp);
				}
			}
		return marketModel.createBidBundle(list, today + 1);
	}

	public void updateStatus() {
		for (Integer integer : myActiveCampaignIndexs) {
			// use today to calculate metrics ratios
			myCampaigns.get(integer).updateRatio(today);
		}
	}

	private List<Integer> getNextDayActiveCampaignIndex() {
		int day = today + 1;
		List<Integer> removeList = new ArrayList<>();
		CampaignData tmp;
		for (Integer integer : myActiveCampaignIndexs) {
			tmp = myCampaigns.get(integer);
			// dayEnd <= tomorrow or impsTogo == 0
			if (tmp.dayEnd <= day || tmp.impsTogo() == 0) {
				removeList.add(integer);
			}
		}
		// remove all the inactive campaigns from the myActiveCampaignIndexs
		myActiveCampaignIndexs.removeAll(removeList);
		return myActiveCampaignIndexs;
	}
	
	private List<CampaignData> getNextDayActiveCampaign() {
		int day = today + 1;
		List<CampaignData> list = new ArrayList<>();
		CampaignData tmp;
		for (Integer integer : myActiveCampaignIndexs) {
			tmp = myCampaigns.get(integer);
			if (tmp.dayStart <= day && tmp.dayEnd >= day && tmp.impsTogo() > 0) {
				list.add(tmp);
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
		
		saveOtherWonCampaign(campaign);

	}

	private void saveOtherWonCampaign(CampaignData campaign) {
		Integer id = campaign.id;
		otherCampaigns.put(id, campaign);
		otherActiveCampaignIndexs.add(id);
		
	}

	private void updateOtherWonCampaign() {	
		if (otherActiveCampaignIndexs.isEmpty()){
			System.out.println("no other campaign exists!!!");
			return;
		}
		
		// removeList to avoid exception
		List<Integer> removeList = new ArrayList<Integer>();
		int tomorrow = today + 1;
		for (Integer integer : otherActiveCampaignIndexs) {
			if (tomorrow >= otherCampaigns.get(integer).dayEnd) {			
				removeList.add(integer); 
				
			}
		}	
		otherActiveCampaignIndexs.removeAll(removeList);
	}

	private void showBidCampaignFactor(String mark) {
		System.out.println("bidCampaignDefault " + mark + " " + bidCampaignDefault);
	}

	public void updateAdNetworkReport(AdNetworkReport report) {
		marketModel.bidBundleFeedback(report, today);
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

		// output all the data finally
		if (today >= 60) {
			System.out.println("\n\n!!!!!!!!!! EvaluateModel");
			for (EvaluateModel evaluate : evaluateList) {
				System.out.println(evaluate.showEvaluate());
			}
			System.out.println("\n\n!!!!!!!!!! MyCampaigns");
			
			Iterator<Map.Entry<Integer, CampaignData>> it = myCampaigns.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, CampaignData> entry = it.next();
				System.out.println("" + entry.getKey() + " " + entry.getValue());
			}
			System.out.println("\n\n!!!!!!!!!! Core");
			marketModel.printCore();
			
			System.out.println("\n\n!!!!!!!!!! Ucs: Level,Cost");
			for (UcsLevelCost i : ulc){
				System.out.println(i.toString());
			}
		}
		
	}

	public void clear() {
		shouldBid = true;
		ucsLevel = 0.2;
		quality = 1.0d;
		today = 0;
		myCampaigns = new HashMap<>();
		myActiveCampaignIndexs.clear();
		otherCampaigns = new HashMap<>();
		otherActiveCampaignIndexs.clear();
		
		levelUpCostMargin = BidForUcsUtil.calcuMarginUcsLevelUp();
	}

	public void updatePublsers(AdxPublisherReport report) {
		marketModel.updateByPublisherReport(report);
		// marketModel.printCore();
	}
	
	/**
	 * @author Lewis
	 *
	 */

	private double bidUcsDefault = 0.12d;
	private boolean shouldBid = false;
	private double aveImpPredictCost = 3.065619492584309E-4d;
	private double pastTrustRate = 0.4d;
	private List<UcsLevelCost> ulc = new ArrayList<UcsLevelCost> ();
	private Map<Integer, Double> levelUpCostMargin = new LinkedHashMap<Integer, Double>();
	
	public double bidForUCS() {
		if (!shouldBid)
			return 0;
		return bidUcsDefault;
	}
	
	public void updateUcs(double level, double cost) {
		
		ulc.add(new UcsLevelCost(level, cost));
		
		updateOtherWonCampaign();
		
		// get my tomorrows active campaign index  
		myActiveCampaignIndexs = getNextDayActiveCampaignIndex();
		
		// check if should bid for tomorrows ucs
		if (myActiveCampaignIndexs.isEmpty() || today < 1d){
			shouldBid = false;
			System.out.println("No need bid for tomorrow's UCS.");
			return;
		} else {
			System.out.println("The number of my tomorrow's active Campaigns is: "
					+ String.valueOf(myActiveCampaignIndexs.size()));
			shouldBid = true;
		}
		
		// if yes, check ucs level. 
		// if > 0.9 then bid reduce.
		double alpha = 1.4d;
		double reducedReate = 1.3d;
		if (level > 0.9d){
			bidUcsDefault /=  reducedReate;
		}
		
		// if < 0.78, then bid increase but no more than upper bound
		double[] aveImpCost;
		double[] aveImpTogo;
		
		aveImpCost = calcuAveImpCost(pastTrustRate);
		aveImpTogo = calcuAveImpTogo();
		
		double timePressure = calcuTimePressureSum();
		if ((level < 0.78d && isUcsUpWorthy(aveImpCost, aveImpTogo, level)) 
							|| (level < 0.78d && timePressure > 1d)){
			bidUcsDefault = bidUcsDefault * alpha * (0.4d + 0.6d * timePressure); 
		}
		
	}

	public boolean isUcsUpWorthy(double[] aveImpCost, double[] aveImpTogo, double level) {
		double relativeLost;
		int adjustedLevel = adjustLevel(level);
		
		if (level == 1.0d){
			return false;
		}
		
		switch(myActiveCampaignIndexs.size()){
			case 1:
				relativeLost = (3d / 20d) * aveImpCost[0] * aveImpTogo[0];
				//test
				System.out.println("!!!!!!!!!!myactivecampaigns:" + String.valueOf(myActiveCampaignIndexs.size()) + "!!!!!relativeLost: " + String.valueOf(relativeLost));
				if (relativeLost > levelUpCostMargin.get(adjustedLevel)) return true;
				else return false;
			case 2: 
				double higherCost = Math.max(aveImpCost[0], aveImpCost[1]);
				relativeLost = (3d / 10d) * higherCost * (Math.pow(aveImpTogo[0], 2) + Math.pow(aveImpTogo[1], 2))
						/ (aveImpTogo[0] + aveImpTogo[1]);
				//test
				System.out.println("!!!!!!!!!!myactivecampaigns:" + String.valueOf(myActiveCampaignIndexs.size()) + "!!!!!relativeLost: " + String.valueOf(relativeLost));
				if (relativeLost > levelUpCostMargin.get(adjustedLevel)) return true;
				else return false;
			default:
				higherCost = Math.max(aveImpCost[0], aveImpCost[1]);
				higherCost = Math.max(higherCost, aveImpCost[2]);
				relativeLost = (9d / 20d) * higherCost
						* (Math.pow(aveImpTogo[0], 2) + Math.pow(aveImpTogo[1], 2) + Math.pow(aveImpTogo[2], 2))
						/ ((aveImpTogo[0] + aveImpTogo[1]) + aveImpTogo[2]);
				//test
				System.out.println("!!!!!!!!!!myactivecampaigns:" + String.valueOf(myActiveCampaignIndexs.size()) + "!!!!!relativeLost: " + String.valueOf(relativeLost));
				if (relativeLost > levelUpCostMargin.get(adjustedLevel)) return true;
				else return false;
		}
	}

	private int adjustLevel(double level) {
		int temp = (int) Math.round(level * 100d);	
		return temp;
	}

	private double[] calcuAveImpCost(double pastTrustRate) {
		double[] aveImpCost = new double[myActiveCampaignIndexs.size()];
		int count = 0;
		double aveImpPastCost;
		for (Integer integer : myActiveCampaignIndexs){
			if (checkPastExist(integer)){
				aveImpPastCost = myCampaigns.get(integer).stats.getCost() / myCampaigns.get(integer).stats.getTargetedImps();
				aveImpCost[count] = (1-pastTrustRate) * aveImpPredictCost + pastTrustRate * aveImpPastCost;
			} else {
				aveImpCost[count] = aveImpPredictCost;
			}
			System.out.println("!!!!!!!!!!!! Campaign id:" + String.valueOf(integer) + ", AveImpCost: "
					+ String.valueOf(aveImpCost[count]));
			count++;	
		}
		return aveImpCost;
	}

	private boolean checkPastExist(Integer integer) {
		if (myCampaigns.get(integer).stats.getCost() == 0d){
			System.out.println("!!!!!!!!!!!! Stats.getCost: "
					+ String.valueOf(myCampaigns.get(integer).stats.getCost()));
			return false;
		} else {
			System.out.println("!!!!!!!!!!!! Stats.getCost: "
					+ String.valueOf(myCampaigns.get(integer).stats.getCost()));
			return true;
		}
			
	}
	private double[] calcuAveImpTogo() {
		double[] aveImpTogo = new double[myActiveCampaignIndexs.size()];
		double contractLength;
		int count = 0;
		for (Integer integer : myActiveCampaignIndexs){
			contractLength = myCampaigns.get(integer).dayEnd - myCampaigns.get(integer).dayStart + 1d;
			aveImpTogo[count] = myCampaigns.get(integer).reachImps /contractLength;
			System.out.println("!!!!!!!!!!!! Campaign id:" + String.valueOf(integer) + ", AveImpTogo: "
					+ String.valueOf(aveImpTogo[count]));
			count++;
		}
		
		return aveImpTogo;
	}

	private double calcuTimePressureSum() {
		double timePressureSum = 0;
		for (Integer integer : myActiveCampaignIndexs){
			timePressureSum += myCampaigns.get(integer).timePressure;
		}
		return timePressureSum;
	}
	
	
	public void campaignStateUpdate(int id, CampaignStats cstats) {
		myCampaigns.get(id).setStats(cstats);
		System.out.println("Day " + today + ": Updating campaign " + id + " stats: " + cstats.getTargetedImps() + "/"
				+ myCampaigns.get(id).reachImps + " Cost of imps is " + cstats.getCost() + " budget:"
				+ myCampaigns.get(id).budget);

		if (myCampaigns.get(id).impsTogo() == 0) {
			myActiveCampaignIndexs.remove(myCampaigns.get(id));
		}

	}
}
