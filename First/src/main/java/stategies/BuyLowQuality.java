package stategies;

/**
 * Created by Nick on 11/19/2016.
 */
public class BuyLowQuality implements BidderStrategy {
    int ourTrueValue;

    @Override
    public boolean propose(int price, int budget) {
        if (price > budget) return false;
        boolean lowQuality = determineQuality(price);
        ourTrueValue = budget / 10;
        System.out.println("BuyLow: Our true value is " + ourTrueValue + ", price is " + price +
                ". Low quality is " + lowQuality);
        return lowQuality && price <= ourTrueValue;
    }

    private boolean determineQuality(int price) {
        if (price > 200) return false; //probably high quality
        return true; //buy when price is less than 200
    }
}
