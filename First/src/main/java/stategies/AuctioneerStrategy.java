package stategies;

import domain.ArtistArtifact;

/**
 * Created by Nick on 11/19/2016.
 */
public interface AuctioneerStrategy {
    int setPrice(ArtistArtifact item, int budget);
    boolean createHighPrice(int budget);
}
