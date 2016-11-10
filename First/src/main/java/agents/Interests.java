package agents;

import java.security.SecureRandom;

/**
 * Created by Nick on 11/10/2016.
 */
public enum Interests {
    Chocolate, Paintings, Flowers, Cabbage;

    private static SecureRandom rand = new SecureRandom();
    public static Interests getRandom() {
        switch (rand.nextInt(4)) {
            case 0:
                return Chocolate;
            case 1:
                return Paintings;
            case 2:
                return Flowers;
            case 3:
                return Cabbage;
            default:
                return Cabbage;
        }
    }
}
