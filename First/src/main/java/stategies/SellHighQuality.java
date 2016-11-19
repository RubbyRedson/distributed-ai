package stategies;

import domain.ArtistArtifact;

/**
 * Created by Nick on 11/19/2016.
 */
public class SellHighQuality implements AuctioneerStrategy {
    private static int RISKY_MULTIPLIER = 4;
    private static int NOT_RISKY_MULTIPLIER = 2;
    private static int BUDGET_THRESHOLD = 2000;

    @Override
    public int setPrice(ArtistArtifact item, int budget) {
        if (budget > BUDGET_THRESHOLD) { //can be risky
            return item.getProductionCost() * RISKY_MULTIPLIER;
        } else {
            return item.getProductionCost() * NOT_RISKY_MULTIPLIER;
        }
    }

    @Override
    public boolean createHighPrice(int budget) {
        return true;
    }
}
