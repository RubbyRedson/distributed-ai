package agents;

import java.io.Serializable;
import java.util.List;
import domain.ArtistArtifact;
import jade.core.AID;
import stategies.AuctioneerStrategy;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public interface ArtistState {
    ArtistArtifact getArtifact();
    int getBudget();
    int getCurrAuctionPrice();

    void onSoldArtifact();
    List<AID> getAuctioneers();
    void setAuctioneers(List<AID> auctioneers);
    AuctioneerStrategy getAgentStrategy();

    void setAgentStrategy();

    void setWinner(String winner);
    String getContainerName();
    void sendPriceToPrime(int price, String winner);
    //void setCurrAuctionPrice(int newPrice);
    //void setNewBudget(int newBudget);
}
