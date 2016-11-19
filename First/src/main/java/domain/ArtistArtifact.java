package domain;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class ArtistArtifact  extends Artifact{

    int productionCost;
    boolean isHighQuality;

    public ArtistArtifact(int productionCost, boolean isHighQuality, Interest type, String name, String author, String date) {
        super(type, name, author, date);
        this.productionCost = productionCost;
        this.isHighQuality = isHighQuality;
    }

    public int getProductionCost() {
        return productionCost;
    }

    public boolean isHighQuality() {
        return isHighQuality;
    }
}
