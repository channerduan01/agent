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

    public QueryModel(AdxQuery query, double bidPrice, double population) {
        this.adxQuery = query;
        this.bidPrice = bidPrice;
        this.population = population;
        updateDay = 0;
        externalMark = false;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("QueryModel---")
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
            rate = 0.92;
        } else if (d > 4 && d < 8) {
            rate = 0.88;
        } else {
            return;
        }
        bidPrice = bidPrice * rate;
    }

}
