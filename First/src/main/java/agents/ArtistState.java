package agents;

import java.util.List;
import domain.ArtistArtifact;
import jade.core.AID;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public interface ArtistState {
    ArtistArtifact getArtifact();
    int getBudget();
    int getCurrAuctionPrice();

    List<AID> getAuctioneers();
    void setAuctioneers(List<AID> auctioneers);
    //void setCurrAuctionPrice(int newPrice);
    //void setNewBudget(int newBudget);
}
