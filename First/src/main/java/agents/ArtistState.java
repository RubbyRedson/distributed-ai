package agents;

import domain.ArtistArtifact;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public interface ArtistState {
    ArtistArtifact getArtifact();
    int getBudget();
    int getCurrAuctionPrice();

    //void setCurrAuctionPrice(int newPrice);
    //void setNewBudget(int newBudget);
}
