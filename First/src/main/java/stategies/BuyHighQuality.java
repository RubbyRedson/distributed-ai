package stategies;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by Nick on 11/19/2016.
 */
public class BuyHighQuality implements BidderStrategy {
    float highQualityProbability = 0.5f;
    Random random = new SecureRandom();

    int ourTrueValue;


    @Override
    public boolean propose(int price, int budget) {
        if (price > budget) return false;
        ourTrueValue = budget > 2000 ? 200 : budget / 10;
        boolean highQuality = determineQuality(price);
        System.out.println("BuyHigh: Our true value is " + ourTrueValue + ", price is " + price +
                ". High quality is " + highQuality);
        return highQuality && price <= budget;
    }

    private boolean determineQuality(int price) {
        if (price < 200) return false; //probably low quality
        return random.nextFloat() > highQualityProbability; // 50/50 here
    }
}
