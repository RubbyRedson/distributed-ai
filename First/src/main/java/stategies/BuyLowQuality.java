package stategies;

import java.io.Serializable;

/**
 * Created by Nick on 11/19/2016.
 */
public class BuyLowQuality implements BidderStrategy, Serializable {
    int ourTrueValue;
    private static final int COEFF = 10;
    private static final int LOWEST_PRICE_FOR_HQ = 200;

    @Override
    public boolean propose(int price, int budget) {
        if (price > budget) return false;
        boolean lowQuality = determineQuality(price);
        ourTrueValue = budget;
        System.out.println("BuyLow: Our true value is " + ourTrueValue + ", price is " + price +
                ". Low quality is " + lowQuality);
        return lowQuality && price < ourTrueValue;
    }

    private boolean determineQuality(int price) {
        if (price > LOWEST_PRICE_FOR_HQ) return false; //probably high quality
        return true; //buy when price is less than 200
    }
}
