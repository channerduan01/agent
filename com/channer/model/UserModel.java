package com.channer.model;

import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;

import java.util.Set;


/**
 * Created by channerduan on 11/18/15.
 */
public class UserModel {

    public void init() {
//        convertData();
        double num = 0;
        for (UserBasis basis : PUBLISHERS_BASIS) {
            num += basis.population;
        }
        System.out.println("User-total population:" + num);
    }

    public static class UserBasis {
        public MarketSegment age;
        public MarketSegment income;
        public MarketSegment gender;
        public double population;

        public UserBasis(MarketSegment age, MarketSegment income, MarketSegment gender, double population) {
            this.age = age;
            this.income = income;
            this.gender = gender;
            this.population = population;
        }
    }

    public UserBasis[] PUBLISHERS_BASIS = {
            new UserBasis(MarketSegment.YOUNG, MarketSegment.LOW_INCOME, MarketSegment.MALE, 1836),
            new UserBasis(MarketSegment.YOUNG, MarketSegment.HIGH_INCOME, MarketSegment.MALE, 517),
            new UserBasis(MarketSegment.YOUNG, MarketSegment.LOW_INCOME, MarketSegment.FEMALE, 1980),
            new UserBasis(MarketSegment.YOUNG, MarketSegment.HIGH_INCOME, MarketSegment.FEMALE, 256),
            new UserBasis(MarketSegment.OLD, MarketSegment.LOW_INCOME, MarketSegment.MALE, 1795),
            new UserBasis(MarketSegment.OLD, MarketSegment.HIGH_INCOME, MarketSegment.MALE, 808),
            new UserBasis(MarketSegment.OLD, MarketSegment.LOW_INCOME, MarketSegment.FEMALE, 2401),
            new UserBasis(MarketSegment.OLD, MarketSegment.HIGH_INCOME, MarketSegment.FEMALE, 407),
    };

    private void convertData() {
        int i = 0, j = 0;
        for (UserSourceItem item : BASIS_DATA) {
            Set<MarketSegment> set = MarketSegment.extractSegment(item);
            j += item.population;
            if (++i == 6) {
                Object [] array = set.toArray();
                for (int k = 1;k < 3;k++) {
                    if (array[k] == MarketSegment.YOUNG || array[k] == MarketSegment.OLD) {
                        Object tmp = array[0];
                        array[0] = array[k];
                        array[k] = tmp;
                        break;
                    }
                }
                if (array[1] == MarketSegment.FEMALE || array[1] == MarketSegment.MALE) {
                    Object tmp = array[1];
                    array[1] = array[2];
                    array[2] = tmp;
                }

                System.out.println("new UserBasis("
                        + "MarketSegment." + array[0] + ", "
                        + "MarketSegment." + array[1] + ", "
                        + "MarketSegment." + array[2] + ", "
                        + j + "),");
                j = 0;
                i = 0;
            }
        }
    }

    private static class UserSourceItem extends AdxUser {
        protected int population;

        public UserSourceItem(Age age, Gender gender, Income income, int population) {
            super(age, gender, income, 0.0D, 0);
            this.population = population;
        }
    }

    private UserSourceItem[] BASIS_DATA = {
            new UserSourceItem(Age.Age_18_24, Gender.male, Income.low, 526),
            new UserSourceItem(Age.Age_18_24, Gender.male, Income.medium, 71),
            new UserSourceItem(Age.Age_25_34, Gender.male, Income.low, 371),
            new UserSourceItem(Age.Age_25_34, Gender.male, Income.medium, 322),
            new UserSourceItem(Age.Age_35_44, Gender.male, Income.low, 263),
            new UserSourceItem(Age.Age_35_44, Gender.male, Income.medium, 283),
            new UserSourceItem(Age.Age_18_24, Gender.male, Income.high, 11),
            new UserSourceItem(Age.Age_18_24, Gender.male, Income.very_high, 5),
            new UserSourceItem(Age.Age_25_34, Gender.male, Income.high, 140),
            new UserSourceItem(Age.Age_25_34, Gender.male, Income.very_high, 51),
            new UserSourceItem(Age.Age_35_44, Gender.male, Income.high, 185),
            new UserSourceItem(Age.Age_35_44, Gender.male, Income.very_high, 125),
            new UserSourceItem(Age.Age_18_24, Gender.female, Income.low, 546),
            new UserSourceItem(Age.Age_18_24, Gender.female, Income.medium, 52),
            new UserSourceItem(Age.Age_25_34, Gender.female, Income.low, 460),
            new UserSourceItem(Age.Age_25_34, Gender.female, Income.medium, 264),
            new UserSourceItem(Age.Age_35_44, Gender.female, Income.low, 403),
            new UserSourceItem(Age.Age_35_44, Gender.female, Income.medium, 255),
            new UserSourceItem(Age.Age_18_24, Gender.female, Income.high, 6),
            new UserSourceItem(Age.Age_18_24, Gender.female, Income.very_high, 3),
            new UserSourceItem(Age.Age_25_34, Gender.female, Income.high, 75),
            new UserSourceItem(Age.Age_25_34, Gender.female, Income.very_high, 21),
            new UserSourceItem(Age.Age_35_44, Gender.female, Income.high, 104),
            new UserSourceItem(Age.Age_35_44, Gender.female, Income.very_high, 47),
            new UserSourceItem(Age.Age_45_54, Gender.male, Income.low, 290),
            new UserSourceItem(Age.Age_45_54, Gender.male, Income.medium, 280),
            new UserSourceItem(Age.Age_55_64, Gender.male, Income.low, 284),
            new UserSourceItem(Age.Age_55_64, Gender.male, Income.medium, 245),
            new UserSourceItem(Age.Age_65_PLUS, Gender.male, Income.low, 461),
            new UserSourceItem(Age.Age_65_PLUS, Gender.male, Income.medium, 235),
            new UserSourceItem(Age.Age_45_54, Gender.male, Income.high, 197),
            new UserSourceItem(Age.Age_45_54, Gender.male, Income.very_high, 163),
            new UserSourceItem(Age.Age_55_64, Gender.male, Income.high, 157),
            new UserSourceItem(Age.Age_55_64, Gender.male, Income.very_high, 121),
            new UserSourceItem(Age.Age_65_PLUS, Gender.male, Income.high, 103),
            new UserSourceItem(Age.Age_65_PLUS, Gender.male, Income.very_high, 67),
            new UserSourceItem(Age.Age_45_54, Gender.female, Income.low, 457),
            new UserSourceItem(Age.Age_45_54, Gender.female, Income.medium, 275),
            new UserSourceItem(Age.Age_55_64, Gender.female, Income.low, 450),
            new UserSourceItem(Age.Age_55_64, Gender.female, Income.medium, 228),
            new UserSourceItem(Age.Age_65_PLUS, Gender.female, Income.low, 827),
            new UserSourceItem(Age.Age_65_PLUS, Gender.female, Income.medium, 164),
            new UserSourceItem(Age.Age_45_54, Gender.female, Income.high, 122),
            new UserSourceItem(Age.Age_45_54, Gender.female, Income.very_high, 57),
            new UserSourceItem(Age.Age_55_64, Gender.female, Income.high, 109),
            new UserSourceItem(Age.Age_55_64, Gender.female, Income.very_high, 48),
            new UserSourceItem(Age.Age_65_PLUS, Gender.female, Income.high, 53),
            new UserSourceItem(Age.Age_65_PLUS, Gender.female, Income.very_high, 18)};
}
