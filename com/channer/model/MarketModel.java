package com.channer.model;


import com.channer.BidBundleUtil;
import com.channer.CampaignData;
import edu.umich.eecs.tac.props.Ad;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.report.adn.AdNetworkKey;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportEntry;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportEntry;

import java.util.*;

/**
 * Created by channerduan on 11/18/15.
 */
public class MarketModel {

    static class Holder {
        final static MarketModel sInstance = new MarketModel();
    }

    /*
        static configuration
     */
    private PublisherModel mPublisherModel;
    private UserModel mUserModel;

    /*
        dynamic game data
     */
    private List<CampaignTrackModel> mCampaignTrackers = new ArrayList<>();

    /*
        The core of Market evaluation
     */
    private SegmentModel[] mCoreData;

    /*
        The core of Query channel evaluation
     */
    public QueryModel[] mQuerySpace;

    public static MarketModel getInstance() {
        return Holder.sInstance;
    }

    private MarketModel() {
        mPublisherModel = new PublisherModel();
        mUserModel = new UserModel();
//        mUserModel.init();    no need to init userModel now
    }

    public void init(Collection<String> publishers) {
        mPublisherModel.init(publishers);
        for (PublisherModel.Publisher publisher : mPublisherModel.mPublisers) {
            System.out.println(publisher.toString());
        }
        for (PublisherModel.PublisherBasis publisher : mPublisherModel.mBasisData) {
            System.out.println(publisher.toString());
        }
        mCampaignTrackers.clear();
        initSegments();
        initQuerySpace();   // init order is important!

        // model check
//        int userNumber = 0;
//        for (int i = 0; i < mCoreData.length; i++) {
//            System.out.println(mCoreData[i].toString());
//            userNumber += mCoreData[i].calcuImpression(1.0d, 1.0d);
//        }
//        System.out.println("total users calculated by core:" + userNumber);
    }

    private static final double QUERY_BASIC_PRICE[] = {0.1, 0.3, 0.3, 0.5};

    private void initQuerySpace() {
        int publisherSize = mPublisherModel.mPublisers.length;
        int totalChannelNum = mCoreData.length * publisherSize * 4;
        mQuerySpace = new QueryModel[totalChannelNum];
        int k = 0;
        for (int i = 0; i < mCoreData.length; i++) {
            Set<MarketSegment> segments = SegmentModel.mapBackMarketSegment(i);
            for (int j = 0; j < publisherSize; j++) {
                double[] populations = mCoreData[i].fragments.get(j).subPopulation;
                mQuerySpace[k++] = new QueryModel(
                        new AdxQuery(mPublisherModel.mPublisers[j].name,
                                segments, Device.mobile, AdType.video),
                        QUERY_BASIC_PRICE[3],
                        populations[0]);
                mQuerySpace[k++] = new QueryModel(
                        new AdxQuery(mPublisherModel.mPublisers[j].name,
                                segments, Device.mobile, AdType.text),
                        QUERY_BASIC_PRICE[3],
                        populations[1]);
                mQuerySpace[k++] = new QueryModel(
                        new AdxQuery(mPublisherModel.mPublisers[j].name,
                                segments, Device.pc, AdType.video),
                        QUERY_BASIC_PRICE[3],
                        populations[2]);
                mQuerySpace[k++] = new QueryModel(
                        new AdxQuery(mPublisherModel.mPublisers[j].name,
                                segments, Device.pc, AdType.text),
                        QUERY_BASIC_PRICE[3],
                        populations[3]);
            }
        }
    }

    private double efficienceMeasureForOneSegment(double oneSegInBoard[], int length, int segIndex, List<CampaignData> list) {

        int i;
        double mE = 0;
        double vE = 0;
        double mR = mCoreData[segIndex].avgMRatio;
        double vR = mCoreData[segIndex].avgVRatio;
        double impRequire = 0;

        for (i = 0; i < length; i++)
            impRequire += oneSegInBoard[i];
        if (impRequire == 0)
            return 0;
        for (i = 0; i < length; i++)
            if (oneSegInBoard[i] != 0) {
                mE += oneSegInBoard[i] / impRequire * list.get(i).mobileCoef;
                vE += oneSegInBoard[i] / impRequire * list.get(i).videoCoef;
            }

        double sumImpSupply = mCoreData[segIndex].population * SegmentModel.impTransFactor(
                mR, mE, vR, vE);

        double meetR = sumImpSupply / impRequire;

        double cost = mCoreData[segIndex].p1 / (mE * vE) * mR * vR +
                mCoreData[segIndex].p2 / mE * mR * (1d - vR) +
                mCoreData[segIndex].p3 / vE * (1d - mR) * vR +
                mCoreData[segIndex].p4 * (1d - mR) * (1d - vR);


        System.out.println("!!!!!efficience:" + segIndex + "  " + meetR + " " + cost);

        return meetR / cost;
    }

    /*
        Core algorithm
     */
    public AdxBidBundle createBidBundle(List<CampaignData> list, int today) {
        int i, j, k;
        int numCam = list.size();
        System.out.println("!!!!!!!!!!!!!!Task division start!!!!!!!!!!!!!!");
        double board[][] = new double[mCoreData.length + 1][numCam + 1];
        for (i = 0; i <= mCoreData.length; i++)
            for (j = 0; j <= numCam; j++)
                board[i][j] = 0;

        double tmpDouble;
        for (i = 0; i < numCam; i++) {
            tmpDouble = list.get(i).impsTogo() / list.get(i).segments.size();
            board[mCoreData.length][i] = list.get(i).segments.size();
            for (int segMark : list.get(i).segments)
                board[segMark][i] = tmpDouble;
        }
        for (i = 0; i < mCoreData.length; i++)
            board[i][numCam] = efficienceMeasureForOneSegment(board[i], numCam, i, list);
        System.out.println("test!!!!!!!!!!!!!!!!!!!!!!!!!");
        double divide;
        double benefit, resE1, resE2 = 0, oriE1, oriE2;
        int bestIndex = 0;
//        for (i = 0; i < numCam; i++)
//            if (board[mCoreData.length][i] > 1)
//                for (j = 0; j < mCoreData.length - 1; j++)
//                    if (board[j][i] > 0) {
//                        System.out.println("test!!!!!! " + j + "--" + i);
//                        divide = board[j][i] / 2;
//                        board[j][i] -= divide;
//                        oriE1 = board[j][numCam];
//                        resE1 = efficienceMeasureForOneSegment(board[j], numCam, j, list);
//                        benefit = 0;
//                        for (k = j + 1; k < mCoreData.length; k++)
//                            if (board[k][i] > 0) {
//                                oriE2 = board[k][numCam];
//                                board[k][i] += divide;
//                                resE2 = efficienceMeasureForOneSegment(board[k], numCam, k, list);
//                                tmpDouble = resE1 + resE2 - oriE1 - oriE2;
//                                if (tmpDouble > benefit) {
//                                    benefit = tmpDouble;
//                                    bestIndex = k;
//                                }
//                                board[k][i] -= divide;
//                            }
//                        if (benefit > 0) {
//                            board[bestIndex][i] -= divide;
//                            board[j][numCam] = resE1;
//                            board[bestIndex][numCam] = resE2;
//                        } else
//                            board[j][i] += divide;
//                    }

        for (i = 0;i <= numCam ;i++) {
            for (j = 0; j <= mCoreData.length; j++)
                System.out.print("" + board[j][i] + ",");
            System.out.print("\n");
        }
        System.out.println("!!!!!!!!!!!!!!Task division end!!!!!!!!!!!!!!");


        AdxBidBundle bidBundle = new AdxBidBundle();
        int size = mQuerySpace.length;
        for (i = 0; i < size; i++) mQuerySpace[i].externalMark = false;
        for (CampaignData campaignData : list) {
            buildQueryForCampaign(bidBundle, campaignData, today);
        }
        return bidBundle;
    }

    public void bidBundleFeedback(AdNetworkReport report, int today) {
        double cost = 0d;
        int numberOfZero = 0;
        int numberOfOne = 0;
        Set<AdNetworkKey> keySet = report.keys();
        AdNetworkReportEntry entry;
        for (AdNetworkKey key : keySet) {
            System.out.println("!!!!!!! report:" + report.getAdNetworkReportEntry(key).toString());
            cost += report.getAdNetworkReportEntry(key).getCost();
            entry = report.getAdNetworkReportEntry(key);

            int segMark = SegmentModel.mapSingleMarketSegment(key.getAge(), key.getIncome(), key.getGender());
            boolean isM = key.getDevice() == Device.mobile;
            boolean isV = key.getAdType() == AdType.video;
            int publisherIndex = mPublisherModel.getPublisherIndex(key.getPublisher());
            int channel;
            if (isM && isV) channel = 0;
            else if (isM) channel = 1;
            else if (isV) channel = 2;
            else channel = 3;
            int publisherChannelSize = mPublisherModel.mPublisers.length * 4;
            int index = segMark * publisherChannelSize + publisherIndex * 4 + channel;

            double winRatio = (double) entry.getWinCount() / (double) entry.getBidCount();
            System.out.println("!!!!" + winRatio + " " + mQuerySpace.toString());

            if (winRatio == 1) {
                mQuerySpace[index].bidPrice /= 1.2d;
                numberOfOne++;
                mQuerySpace[index].numReduce++;
            } else if (winRatio == 0) {
                mQuerySpace[index].bidPrice *= 1.2d;
                numberOfZero++;
                mQuerySpace[index].numAdd++;
            } else {
                mQuerySpace[index].bidPrice =
                        mQuerySpace[index].bidPrice * (1d + 0.2d * (0.8d - winRatio));
            }
        }
        System.out.println("!!!!!!!!!!! total cost !!!!!!!!!!!!  " + cost);

        double ratioOfOne = (double) numberOfOne / (double) keySet.size();
        double ratioOfZero = (double) numberOfZero / (double) keySet.size();

        double redress = 1d;
        if (ratioOfOne > 0.92d) {
            redress = 0.6d;
        } else if (ratioOfZero > 0.92d) {
            redress = 2d;
        }

        if (redress != 1d) {
            System.out.println("!!!!!!!!!!! redress !!!!!!!!!!!!  " + redress);
            int size = mQuerySpace.length;
            for (int i = 0; i < size; i++) {
                mQuerySpace[i].bidPrice *= redress;
                if (redress > 1)
                    mQuerySpace[i].numRedressAdd++;
                else
                    mQuerySpace[i].numRedressReduce++;
                mQuerySpace[i].updateDay = today;
            }
        }
    }

    private void buildQueryForCampaign(AdxBidBundle bundle, CampaignData campaign, int today) {
        List<Integer> segMarks = SegmentModel.mapMarketSegment(campaign.targetSegment);
        List<Integer> pickChoices = new ArrayList<>();
        List<Double> prices = new ArrayList<>();
        double m = campaign.mobileCoef;
        double v = campaign.videoCoef;
        int publisherSize = mPublisherModel.mPublisers.length;
        int publisherChannelTotal = publisherSize * 4;
        int baseIndex;
        int tmpInt;

        // evaluate the cost for different channels
        for (Integer integer : segMarks) {
            baseIndex = integer * publisherChannelTotal;
            for (int i = 0; i < publisherChannelTotal; i++) {
                tmpInt = baseIndex + i;
                if (!mQuerySpace[tmpInt].externalMark) {
                    pickChoices.add(tmpInt);
                    switch (tmpInt % 4) {
                        case 1:
                            prices.add(mQuerySpace[tmpInt].population * m * v / mQuerySpace[tmpInt].bidPrice);
                            break;
                        case 2:
                            prices.add(mQuerySpace[tmpInt].population * m / mQuerySpace[tmpInt].bidPrice);
                            break;
                        case 3:
                            prices.add(mQuerySpace[tmpInt].population * v / mQuerySpace[tmpInt].bidPrice);
                            break;
                        default:
                            prices.add(mQuerySpace[tmpInt].population / mQuerySpace[tmpInt].bidPrice);
                            break;
                    }
                }
            }
        }

        // sort them
        double tmpDouble;
        int size = pickChoices.size();
        for (int i = 0; i < size - 1; i++)
            for (int j = i + 1; j < size; j++)
                if (prices.get(i) > prices.get(j)) {
                    tmpDouble = prices.get(i);
                    prices.set(i, prices.get(j));
                    prices.set(j, tmpDouble);
                    tmpInt = pickChoices.get(i);
                    pickChoices.set(i, pickChoices.get(j));
                    pickChoices.set(j, tmpInt);
                }

        // load
        double count = campaign.impsTogo();
        count *= 1.5d;
        for (int i = 0; i < pickChoices.size() && count > 0; i++) {
            count -= mQuerySpace[pickChoices.get(i)].population;
            tmpDouble = mQuerySpace[pickChoices.get(i)].bidPrice *
                    BidBundleUtil.calcuTimePressure(campaign, today - 1);
            mQuerySpace[pickChoices.get(i)].externalMark = true;
            mQuerySpace[pickChoices.get(i)].updateDay = today;
            mQuerySpace[pickChoices.get(i)].bidPrice = tmpDouble;
            // test
//            tmpDouble = m * v * 0.1d;
            bundle.addQuery(mQuerySpace[pickChoices.get(i)].adxQuery,
                    tmpDouble * 1000d,
                    new Ad(null), campaign.id, 1);
        }

        // limitation
        double impressionLimit = campaign.impsTogo();
        double budgetLimit = campaign.budget * 2;
        bundle.setCampaignDailyLimit(campaign.id,
                (int) impressionLimit, budgetLimit);
    }

    public void printCore() {
        System.out.println("MarketModel Core Data");
        for (int i = 0; i < mCoreData.length; i++) {
            System.out.println(mCoreData[i].toString());
        }

        int publisherChannelTotal = mPublisherModel.mPublisers.length * 4;
        for (int i = 0; i < mQuerySpace.length; i++) {
            if (i % publisherChannelTotal == 0) System.out.println("");
            System.out.println("" + i + "-" + mQuerySpace[i].toString());
        }
    }

    private boolean mIsNeedUpdatePublisherData = false;

    public void updateByPublisherReport(AdxPublisherReport report) {
        for (PublisherCatalogEntry publisherKey : report.keys()) {
            AdxPublisherReportEntry entry = report.getEntry(publisherKey);
            double vRatio = (double) entry.getAdTypeOrientation().get(AdType.video) /
                    ((double) entry.getPopularity() + 1.0d);
            mPublisherModel.updateVideoRatio(entry.getPublisherName(), vRatio);
        }
        mIsNeedUpdatePublisherData = true;
    }

    /**
     *
     * @param segments
     * @return total populations
     */
    public double calcuPopulations(Set<MarketSegment> segments) {
        double num = 0d;
        for (Integer integer :SegmentModel.mapMarketSegment(segments)) num += mCoreData[integer].population;
        return num;
    }

    public void addCampaignTracker(CampaignTrackModel tracker) {
        this.mCampaignTrackers.add(tracker);
        for (CampaignPressureModel pressure : tracker.pressureList) {
            mCoreData[pressure.segmentMark].addCampaignPressure(pressure);
        }
    }

    public void updataDayForCampaignTrackers(int day) {
        List<CampaignTrackModel> removeList = new ArrayList<>();
        for (CampaignTrackModel tracker : mCampaignTrackers) {
            if (tracker.expiredDay < day) removeList.add(tracker);
        }
        for (CampaignTrackModel tracker : removeList) {
            for (CampaignPressureModel pressure : tracker.pressureList) {
                mCoreData[pressure.segmentMark].deleteCampaignPressure(pressure);
            }
        }
        mCampaignTrackers.removeAll(removeList);
    }

    public void updateStateBeforeTheBeginOfNextDay(int today) {
        if (mIsNeedUpdatePublisherData) {
            mIsNeedUpdatePublisherData = false;
            for (SegmentModel model : mCoreData) {
                model.updateDataModel();
            }
        }

        int size = mQuerySpace.length;
        for (int i = 0; i < size; i++) {
            mQuerySpace[i].decay(today);
        }
    }

    /**
     * evaluate the market
     *
     * @return
     */
    public EvaluateModel evaluateMarket(Set<MarketSegment> segments, double requireImp, double mEffect, double vEffect, int today) {
        double pureAvgRequire = 0;
        double basicUserSourceNum = 0;
        double pressure = 0;
        double pressureLength = 0;
        Integer integer;
        int i;
        List<Integer> markList = SegmentModel.mapMarketSegment(segments);
        double p[] = new double[markList.size()];
        double pLength[] = new double[markList.size()];
        double pWidth[] = new double[markList.size()];  // don't use it now
        for (i = 0; i < markList.size(); i++) {
            integer = markList.get(i);
            basicUserSourceNum += mCoreData[integer].population;
            p[i] = mCoreData[integer].calcuAvgPressure(mEffect, vEffect);
            if (p[i] == 0) {
                pLength[i] = 0;
                pWidth[i] = 0;
            } else {
                pLength[i] = mCoreData[integer].calcuAllPressure(mEffect, vEffect, today) / p[i];
            }
            pressure += p[i];
        }
        double avgMRatioForAll = 0d;
        double avgVRatioForAll = 0d;
        for (i = 0; i < markList.size(); i++) {
            integer = markList.get(i);
            avgMRatioForAll = mCoreData[integer].avgMRatio * mCoreData[integer].population / basicUserSourceNum;
            avgVRatioForAll = mCoreData[integer].avgVRatio * mCoreData[integer].population / basicUserSourceNum;
            if (p[i] != 0 && pLength[i] != 0)
                pressureLength += p[i] / pressure * pLength[i];

        }
        pureAvgRequire = requireImp / SegmentModel.impTransFactor(avgMRatioForAll, mEffect, avgVRatioForAll, vEffect);
        EvaluateModel evaluate = new EvaluateModel(
                basicUserSourceNum,
                pureAvgRequire,
                pressure,
                pressureLength,
                today);
        return evaluate;
    }

    private void initSegments() {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");   // for test
        mCoreData = new SegmentModel[mUserModel.PUBLISHERS_BASIS.length];
        for (UserModel.UserBasis userBasis : mUserModel.PUBLISHERS_BASIS) {
            List<MarketFragmentModel> list = new ArrayList<>();
            MarketFragmentModel fragment;
            SegmentModel subModel;
            double tmp;
            double tmpTotalRatioForSpecificMarket = 0;
            for (PublisherModel.PublisherBasis publisherBasis : mPublisherModel.mBasisData) {
                tmp = publisherBasis.populationRatio
                        * (userBasis.age == MarketSegment.YOUNG ? publisherBasis.youngRatio : publisherBasis.oldRatio)
                        * (userBasis.income == MarketSegment.LOW_INCOME ? publisherBasis.lowIncomeRatio : publisherBasis.highIncomeRatio)
                        * (userBasis.gender == MarketSegment.MALE ? publisherBasis.maleRatio : publisherBasis.femaleRatio)
                ;
                tmpTotalRatioForSpecificMarket += tmp;
                fragment = new MarketFragmentModel(mPublisherModel.getPublisher(publisherBasis.name),
                        tmp);
                list.add(fragment);
            }
            for (int i = 0; i < list.size(); i++)
                list.get(i).population = userBasis.population * list.get(i).population / tmpTotalRatioForSpecificMarket;
            subModel = new SegmentModel(SegmentModel.mapSingleMarketSegment(
                    userBasis.age,
                    userBasis.income,
                    userBasis.gender), list);
            mCoreData[subModel.index] = subModel;
        }
    }

}
