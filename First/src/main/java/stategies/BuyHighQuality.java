package stategies;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by Nick on 11/19/2016.
 */
public class BuyHighQuality implements BidderStrategy {
    float highQualityProbability = 0.5f;
    Random random = new SecureRandom();
    private static final int UPPER_LIMIT = 2000;
    private static final int LOWER_LIMIT = 200;
    private static final int LOWEST_PRICE_FOR_HQ = 200;
    private static final int COEFF = 5;
    int ourTrueValue;


    @Override
    public boolean propose(int price, int budget) {
        if (price > budget) return false;
        ourTrueValue = budget > UPPER_LIMIT ? LOWER_LIMIT : budget / COEFF;
        boolean highQuality = determineQuality(price);
        System.out.println("BuyHigh: Our true value is " + ourTrueValue + ", price is " + price +
                ". High quality is " + highQuality);
        return highQuality && price <= budget;
    }

    private boolean determineQuality(int price) {
        if (price < LOWEST_PRICE_FOR_HQ) return false; //probably low quality
        return random.nextFloat() > highQualityProbability; // 50/50 here
    }
}
