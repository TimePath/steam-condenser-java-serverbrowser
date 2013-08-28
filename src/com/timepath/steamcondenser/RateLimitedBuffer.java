package com.timepath.steamcondenser;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author timepath
 */
public abstract class RateLimitedBuffer<E> {

    private final ArrayList<E> buf = new ArrayList<E>();
    private final Timer timer = new Timer();

    /**
     *
     * @param period How frequently to push
     * @param limit How many servers to push
     */
    public RateLimitedBuffer(int period, final int limit) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int count = Math.min(buf.size(), limit);
                ArrayList<E> buffer = new ArrayList(count);
                for (int i = 0; i < count; i++) {
                    buffer.add(buf.get(0));
                    buf.remove(0);
                }
                publish(buffer);
            }
        }, 0, period);
    }

    public void put(final E obj) {
        buf.add(obj);
    }

    abstract void publish(final ArrayList<E> buffer);
}
