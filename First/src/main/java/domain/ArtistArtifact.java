package domain;

import java.io.Serializable;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class ArtistArtifact extends Artifact implements Serializable{

    int productionCost;
    boolean isHighQuality;
    public static final int HIGH_QUALITY_COST = 1000;
    public static final int LOW_QUALITY_COST = 100;

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
