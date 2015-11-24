package com.channer.model;

/**
 * Created by channerduan on 11/21/15.
 */
public class MarketFragmentModel {
    public PublisherModel.Publisher publisher;
    public double population;

    public double subPopulation[] = new double[4];

    public MarketFragmentModel(PublisherModel.Publisher publisher, double population) {
        this.publisher = publisher;
        this.population = population;
        updateSubPopulation();
    }

    public void updateModel() {
        updateSubPopulation();
    }

    private void updateSubPopulation() {
        double mv = publisher.mRatio * publisher.vRatio;
        double m = publisher.mRatio * (1d - publisher.vRatio);
        double v = (1d - publisher.mRatio) * publisher.vRatio;
        double n = (1d - publisher.mRatio) * (1d - publisher.vRatio);

        subPopulation[0] = mv * population;
        subPopulation[1] = m * population;
        subPopulation[2] = v * population;
        subPopulation[3] = n * population;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer
                .append("(")
                .append(publisher)
                .append(":")
                .append(population)
                .append(")");
        return buffer.toString();
    }
}
