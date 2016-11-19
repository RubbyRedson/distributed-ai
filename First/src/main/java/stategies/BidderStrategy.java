package stategies;

/**
 * Created by Nick on 11/19/2016.
 */
public interface BidderStrategy {
    boolean propose(int price, int budget);
}
