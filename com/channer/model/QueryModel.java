package com.channer.model;

import tau.tac.adx.props.AdxQuery;

/**
 * Created by channerduan on 11/23/15.
 */
public class QueryModel {

    public AdxQuery adxQuery;
    public double bidPrice;     // core
    public int updateDay;
    public double population;

    public boolean externalMark;

    public int numDecay;
    public int numAdd;
    public int numReduce;
    public int numRedressAdd;
    public int numRedressReduce;

    public QueryModel(AdxQuery query, double bidPrice, double population) {
        this.adxQuery = query;
        this.bidPrice = bidPrice;
        this.population = population;
        updateDay = 0;
        externalMark = false;
        numDecay = numAdd = numReduce = numRedressAdd = numRedressReduce = 0;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("QueryModel-").append(numAdd).append(",")
                .append(numReduce).append(",")
                .append(numDecay).append(",")
                .append(numRedressAdd).append(",")
                .append(numRedressReduce).append("--")
                .append("bid-").append(bidPrice)
                .append(" ").append(adxQuery)
                .append(" popu:").append(population)
        ;
        return buffer.toString();
    }

    public void decay(int today) {
        double rate;
        int d = today - updateDay;
        if (d > 2 && d < 4) {
            rate = 0.9;
        } else if (d >= 4 && d < 7) {
            rate = 0.8;
        } else if (d >= 7 && d < 10) {
            rate = 0.7;
        } else {
            return;
        }
        bidPrice = bidPrice * rate;
        numDecay++;
    }

}
