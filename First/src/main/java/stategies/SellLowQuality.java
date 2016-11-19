package stategies;

import domain.ArtistArtifact;

/**
 * Created by Nick on 11/19/2016.
 */
public class SellLowQuality implements AuctioneerStrategy {
    private static int RISKY_MULTIPLIER = 3;
    private static double NOT_RISKY_MULTIPLIER = 1.5;
    private static int BUDGET_THRESHOLD = 500;

    @Override
    public int setPrice(ArtistArtifact item, int budget) {
        if (budget < BUDGET_THRESHOLD) { //can not afford to produce new
            return (int) (item.getProductionCost() * NOT_RISKY_MULTIPLIER);
        } else {
            return item.getProductionCost() * RISKY_MULTIPLIER;
        }
    }


    @Override
    public boolean createHighPrice(int budget) {
        return false;
    }
}
