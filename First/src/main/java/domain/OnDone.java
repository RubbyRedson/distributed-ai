package domain;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public interface OnDone<T> {
    void done(T message);
}
