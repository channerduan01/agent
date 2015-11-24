package com.channer.model;

import java.util.*;

/**
 * Created by channerduan on 11/14/15.
 */
public class PublisherModel {

    public static class Publisher {
        public String name;
        public double mRatio;
        public double vRatio;

        public Publisher(String name, double mRatio, double vRatio) {
            this.name = name;
            this.mRatio = mRatio;
            this.vRatio = vRatio;
        }

        @Override
        public String toString() {
            return "publisher-" + name + " m-" + mRatio + " v-" + vRatio;
        }
    }

    public PublisherBasis[] mBasisData;
    public Publisher[] mPublisers;

    private double totalPopulationRatio;

    private static final double DEFAULT_V_RATIO = 0.4d;

    public void init(Collection<String> names) {
//        convertData();

        mBasisData = new PublisherBasis[names.size()];
        mPublisers = new Publisher[names.size()];
        totalPopulationRatio = 0;

        int i = 0;
        for (PublisherBasis basis : PUBLISHERS_BASIS) {
            if (names.contains(basis.name)) {
                mBasisData[i] = basis;
                totalPopulationRatio += basis.populationRatio;
                mPublisers[i] = new Publisher(basis.name, basis.mobileRatio, DEFAULT_V_RATIO);
                i++;
            }
        }
        for(--i;i >=0;i--) {
            mBasisData[i].populationRatio = mBasisData[i].populationRatio / totalPopulationRatio;
        }
    }

    public Publisher getPublisher(String name) {
        for (int i = 0; i < mPublisers.length; i++) {
            if (mPublisers[i].name.equals(name)) {
                return mPublisers[i];
            }
        }
        return null;
    }

    public int getPublisherIndex(String name) {
        for (int i = 0; i < mPublisers.length; i++) {
            if (mPublisers[i].name.equals(name)) {
                return i;
            }
        }
        return 0;
    }

    public void updateVideoRatio(String name, double ratio) {
        for (int i = 0; i < mPublisers.length; i++) {
            if (mPublisers[i].name.equals(name)) {
                mPublisers[i].vRatio = ratio;
                return;
            }
        }
    }

    public static class PublisherBasis {
        public String name;
        public double youngRatio;
        public double oldRatio;
        public double lowIncomeRatio;
        public double highIncomeRatio;
        public double maleRatio;
        public double femaleRatio;
        public double mobileRatio;
        public double pcRatio;

        public double populationRatio;


        public PublisherBasis(String n, double y, double o, double l, double h, double m,
                              double fe, double mo, double pc, double po) {
            name = n;
            youngRatio = y;
            oldRatio = o;
            lowIncomeRatio = l;
            highIncomeRatio = h;
            maleRatio = m;
            femaleRatio = fe;
            mobileRatio = mo;
            pcRatio = pc;
            populationRatio = po;
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer
                    .append(name).append(" ")
                    .append(youngRatio).append(" ")
                    .append(oldRatio).append(" ")
                    .append(lowIncomeRatio).append(" ")
                    .append(highIncomeRatio).append(" ")
                    .append(maleRatio).append(" ")
                    .append(femaleRatio).append(" ")
                    .append(mobileRatio).append(" ")
                    .append(pcRatio).append(" ")
                    .append(populationRatio);
            return buffer.toString();
        }

    }

    private PublisherBasis[] PUBLISHERS_BASIS = {
            new PublisherBasis("yahoo", 0.46d, 0.54d, 0.8d, 0.2d, 0.5d, 0.5d, 0.26d, 0.74d, 0.16d),
            new PublisherBasis("cnn", 0.43d, 0.57d, 0.75d, 0.25d, 0.49d, 0.51d, 0.24d, 0.76d, 0.02d),
            new PublisherBasis("nyt", 0.41d, 0.59d, 0.73d, 0.27d, 0.48d, 0.52d, 0.23d, 0.77d, 0.03d),
            new PublisherBasis("hfn", 0.43d, 0.57d, 0.74d, 0.26d, 0.47d, 0.53d, 0.22d, 0.78d, 0.08d),
            new PublisherBasis("msn", 0.43d, 0.57d, 0.76d, 0.24d, 0.48d, 0.52d, 0.25d, 0.75d, 0.18d),
            new PublisherBasis("fox", 0.41d, 0.59d, 0.72d, 0.28d, 0.49d, 0.51d, 0.24d, 0.76d, 0.03d),
            new PublisherBasis("amazon", 0.41d, 0.59d, 0.77d, 0.23d, 0.48d, 0.52d, 0.21d, 0.79d, 0.13d),
            new PublisherBasis("ebay", 0.41d, 0.59d, 0.77d, 0.23d, 0.49d, 0.51d, 0.22d, 0.78d, 0.08d),
            new PublisherBasis("wallmart", 0.39d, 0.61d, 0.75d, 0.25d, 0.46d, 0.54d, 0.18d, 0.82d, 0.04d),
            new PublisherBasis("target", 0.44d, 0.56d, 0.72d, 0.28d, 0.46d, 0.54d, 0.19d, 0.81d, 0.02d),
            new PublisherBasis("bestbuy", 0.41d, 0.59d, 0.73d, 0.28d, 0.48d, 0.52d, 0.2d, 0.8d, 0.02d),
            new PublisherBasis("sears", 0.38d, 0.62d, 0.7d, 0.3d, 0.47d, 0.53d, 0.19d, 0.81d, 0.02d),
            new PublisherBasis("webmd", 0.4d, 0.6d, 0.73d, 0.28d, 0.46d, 0.54d, 0.24d, 0.76d, 0.02d),
            new PublisherBasis("ehow", 0.41d, 0.59d, 0.77d, 0.23d, 0.48d, 0.52d, 0.28d, 0.72d, 0.02d),
            new PublisherBasis("ask", 0.39d, 0.61d, 0.78d, 0.22d, 0.49d, 0.51d, 0.28d, 0.72d, 0.05d),
            new PublisherBasis("tripadvisor", 0.42d, 0.58d, 0.73d, 0.28d, 0.47d, 0.53d, 0.3d, 0.7d, 0.02d),
            new PublisherBasis("cnet", 0.43d, 0.57d, 0.74d, 0.26d, 0.51d, 0.49d, 0.27d, 0.73d, 0.02d),
            new PublisherBasis("weather", 0.41d, 0.59d, 0.72d, 0.28d, 0.48d, 0.52d, 0.31d, 0.69d, 0.06d),
    };

    private void convertData() {
        for (Object[][] array : BASIS_DATA) {
            double y = 0, o = 0, l = 0, h = 0, m = 0, fe = 0, mo = 0, pc = 0, po = 0;
            int i;
            for (i = 0; i < 3; i++) y += (Double) array[1][i];
            for (i = 3; i < 6; i++) o += (Double) array[1][i];
            for (i = 0; i < 2; i++) l += (Double) array[2][i];
            for (i = 2; i < 4; i++) h += (Double) array[2][i];
            m = (Double) array[3][0];
            fe = (Double) array[3][1];
            mo = (Double) array[4][0];
            pc = (Double) array[4][1];
            po = (Double) array[5][0];

            java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
            System.out.println("new PublisherBasis(\"" + array[0][0] + "\", "
                            + df.format(y) + "d, "
                            + df.format(o) + "d, "
                            + df.format(l) + "d, "
                            + df.format(h) + "d, "
                            + df.format(m) + "d, "
                            + df.format(fe) + "d, "
                            + df.format(mo) + "d, "
                            + df.format(pc) + "d, "
                            + df.format(po) + "d),"
            );
        }
        System.out.println("PUBLISHERS_BASIS size:" + PUBLISHERS_BASIS.length);
    }

    private Object[][][] BASIS_DATA = {
            {{"yahoo"}
                    , {Double.valueOf(0.122D), Double.valueOf(0.171D), Double.valueOf(0.167D), Double.valueOf(0.184D), Double.valueOf(0.164D), Double.valueOf(0.192D)}
                    , {Double.valueOf(0.53D), Double.valueOf(0.27D), Double.valueOf(0.13D), Double.valueOf(0.07000000000000001D)}
                    , {Double.valueOf(0.496D), Double.valueOf(0.504D)}
                    , {Double.valueOf(0.26D), Double.valueOf(0.74D)}
                    , {Double.valueOf(0.16D)}},
            {{"cnn"}, {Double.valueOf(0.102D), Double.valueOf(0.161D), Double.valueOf(0.167D), Double.valueOf(0.194D), Double.valueOf(0.174D), Double.valueOf(0.202D)}, {Double.valueOf(0.48D), Double.valueOf(0.27D), Double.valueOf(0.16D), Double.valueOf(0.09D)}, {Double.valueOf(0.486D), Double.valueOf(0.514D)}, {Double.valueOf(0.24D), Double.valueOf(0.76D)}, {Double.valueOf(0.022D)}},
            {{"nyt"}, {Double.valueOf(0.092D), Double.valueOf(0.151D), Double.valueOf(0.167D), Double.valueOf(0.194D), Double.valueOf(0.174D), Double.valueOf(0.222D)}, {Double.valueOf(0.47D), Double.valueOf(0.26D), Double.valueOf(0.17D), Double.valueOf(0.1D)}, {Double.valueOf(0.476D), Double.valueOf(0.524D)}, {Double.valueOf(0.23D), Double.valueOf(0.77D)}, {Double.valueOf(0.031D)}},
            {{"hfn"}, {Double.valueOf(0.102D), Double.valueOf(0.161D), Double.valueOf(0.167D), Double.valueOf(0.194D), Double.valueOf(0.174D), Double.valueOf(0.202D)}, {Double.valueOf(0.47D), Double.valueOf(0.27D), Double.valueOf(0.17D), Double.valueOf(0.09D)}, {Double.valueOf(0.466D), Double.valueOf(0.534D)}, {Double.valueOf(0.22D), Double.valueOf(0.78D)}, {Double.valueOf(0.081D)}},
            {{"msn"}, {Double.valueOf(0.102D), Double.valueOf(0.161D), Double.valueOf(0.167D), Double.valueOf(0.194D), Double.valueOf(0.174D), Double.valueOf(0.202D)}, {Double.valueOf(0.49D), Double.valueOf(0.27D), Double.valueOf(0.16D), Double.valueOf(0.08D)}, {Double.valueOf(0.476D), Double.valueOf(0.524D)}, {Double.valueOf(0.25D), Double.valueOf(0.75D)}, {Double.valueOf(0.182D)}},
            {{"fox"}, {Double.valueOf(0.092D), Double.valueOf(0.151D), Double.valueOf(0.167D), Double.valueOf(0.194D), Double.valueOf(0.184D), Double.valueOf(0.212D)}, {Double.valueOf(0.46D), Double.valueOf(0.26D), Double.valueOf(0.18D), Double.valueOf(0.1D)}, {Double.valueOf(0.486D), Double.valueOf(0.514D)}, {Double.valueOf(0.24D), Double.valueOf(0.76D)}, {Double.valueOf(0.031D)}},
            {{"amazon"}, {Double.valueOf(0.092D), Double.valueOf(0.151D), Double.valueOf(0.167D), Double.valueOf(0.194D), Double.valueOf(0.184D), Double.valueOf(0.212D)}, {Double.valueOf(0.5D), Double.valueOf(0.27D), Double.valueOf(0.15D), Double.valueOf(0.08D)}, {Double.valueOf(0.476D), Double.valueOf(0.524D)}, {Double.valueOf(0.21D), Double.valueOf(0.79D)}, {Double.valueOf(0.128D)}},
            {{"ebay"}, {Double.valueOf(0.092D), Double.valueOf(0.161D), Double.valueOf(0.157D), Double.valueOf(0.194D), Double.valueOf(0.174D), Double.valueOf(0.222D)}, {Double.valueOf(0.5D), Double.valueOf(0.27D), Double.valueOf(0.15D), Double.valueOf(0.08D)}, {Double.valueOf(0.486D), Double.valueOf(0.514D)}, {Double.valueOf(0.22D), Double.valueOf(0.78D)}, {Double.valueOf(0.08500000000000001D)}},
            {{"wallmart"}, {Double.valueOf(0.072D), Double.valueOf(0.151D), Double.valueOf(0.167D), Double.valueOf(0.204D), Double.valueOf(0.184D), Double.valueOf(0.222D)}, {Double.valueOf(0.47D), Double.valueOf(0.28D), Double.valueOf(0.19D), Double.valueOf(0.06D)}, {Double.valueOf(0.456D), Double.valueOf(0.544D)}, {Double.valueOf(0.18D), Double.valueOf(0.82D)}, {Double.valueOf(0.038D)}},
            {{"target"}, {Double.valueOf(0.092D), Double.valueOf(0.171D), Double.valueOf(0.177D), Double.valueOf(0.184D), Double.valueOf(0.174D), Double.valueOf(0.202D)}, {Double.valueOf(0.45D), Double.valueOf(0.27D), Double.valueOf(0.19D), Double.valueOf(0.09D)}, {Double.valueOf(0.456D), Double.valueOf(0.544D)}, {Double.valueOf(0.19D), Double.valueOf(0.8100000000000001D)}, {Double.valueOf(0.02D)}},
            {{"bestbuy"}, {Double.valueOf(0.102D), Double.valueOf(0.141D), Double.valueOf(0.167D), Double.valueOf(0.204D), Double.valueOf(0.174D), Double.valueOf(0.212D)}, {Double.valueOf(0.465D), Double.valueOf(0.26D), Double.valueOf(0.18D), Double.valueOf(0.095D)}, {Double.valueOf(0.476D), Double.valueOf(0.524D)}, {Double.valueOf(0.2D), Double.valueOf(0.8D)}, {Double.valueOf(0.016D)}},
            {{"sears"}, {Double.valueOf(0.092D), Double.valueOf(0.121D), Double.valueOf(0.167D), Double.valueOf(0.204D), Double.valueOf(0.184D), Double.valueOf(0.232D)}, {Double.valueOf(0.45D), Double.valueOf(0.25D), Double.valueOf(0.2D), Double.valueOf(0.1D)}, {Double.valueOf(0.466D), Double.valueOf(0.534D)}, {Double.valueOf(0.19D), Double.valueOf(0.8100000000000001D)}, {Double.valueOf(0.016D)}},
            {{"webmd"}, {Double.valueOf(0.092D), Double.valueOf(0.151D), Double.valueOf(0.157D), Double.valueOf(0.194D), Double.valueOf(0.184D), Double.valueOf(0.222D)}, {Double.valueOf(0.46D), Double.valueOf(0.265D), Double.valueOf(0.185D), Double.valueOf(0.09D)}, {Double.valueOf(0.456D), Double.valueOf(0.544D)}, {Double.valueOf(0.24D), Double.valueOf(0.76D)}, {Double.valueOf(0.025D)}},
            {{"ehow"}, {Double.valueOf(0.102D), Double.valueOf(0.151D), Double.valueOf(0.157D), Double.valueOf(0.194D), Double.valueOf(0.174D), Double.valueOf(0.222D)}, {Double.valueOf(0.5D), Double.valueOf(0.27D), Double.valueOf(0.15D), Double.valueOf(0.08D)}, {Double.valueOf(0.476D), Double.valueOf(0.524D)}, {Double.valueOf(0.28D), Double.valueOf(0.72D)}, {Double.valueOf(0.025D)}},
            {{"ask"}, {Double.valueOf(0.102D), Double.valueOf(0.131D), Double.valueOf(0.157D), Double.valueOf(0.204D), Double.valueOf(0.184D), Double.valueOf(0.222D)}, {Double.valueOf(0.5D), Double.valueOf(0.28D), Double.valueOf(0.15D), Double.valueOf(0.07000000000000001D)}, {Double.valueOf(0.486D), Double.valueOf(0.514D)}, {Double.valueOf(0.28D), Double.valueOf(0.72D)}, {Double.valueOf(0.05D)}},
            {{"tripadvisor"}, {Double.valueOf(0.082D), Double.valueOf(0.161D), Double.valueOf(0.177D), Double.valueOf(0.204D), Double.valueOf(0.174D), Double.valueOf(0.202D)}, {Double.valueOf(0.465D), Double.valueOf(0.26D), Double.valueOf(0.175D), Double.valueOf(0.1D)}, {Double.valueOf(0.466D), Double.valueOf(0.534D)}, {Double.valueOf(0.3D), Double.valueOf(0.7D)}, {Double.valueOf(0.016D)}},
            {{"cnet"}, {Double.valueOf(0.122D), Double.valueOf(0.151D), Double.valueOf(0.157D), Double.valueOf(0.184D), Double.valueOf(0.174D), Double.valueOf(0.212D)}, {Double.valueOf(0.48D), Double.valueOf(0.265D), Double.valueOf(0.165D), Double.valueOf(0.09D)}, {Double.valueOf(0.506D), Double.valueOf(0.494D)}, {Double.valueOf(0.27D), Double.valueOf(0.73D)}, {Double.valueOf(0.017D)}},
            {{"weather"}, {Double.valueOf(0.092D), Double.valueOf(0.151D), Double.valueOf(0.167D), Double.valueOf(0.204D), Double.valueOf(0.184D), Double.valueOf(0.202D)}, {Double.valueOf(0.455D), Double.valueOf(0.265D), Double.valueOf(0.185D), Double.valueOf(0.095D)}, {Double.valueOf(0.476D), Double.valueOf(0.524D)}, {Double.valueOf(0.31D), Double.valueOf(0.69D)}, {Double.valueOf(0.058D)}}};
}
