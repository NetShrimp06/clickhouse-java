package com.clickhouse.client.api.internal;

import java.util.concurrent.TimeUnit;

public class StopWatch {

    long elapsedNanoTime = 0;
    long startNanoTime;

    public StopWatch() {
        // do nothing
    }

    public StopWatch(long startNanoTime) {
        this.startNanoTime = startNanoTime;
    }

    public void start() {
        startNanoTime = System.nanoTime();
    }

    public void stop() {
        elapsedNanoTime = System.nanoTime() - startNanoTime;
    }

    /**
     * Returns the elapsed time in milliseconds.
     * @return
     */
    public long getElapsedTime() {
        return TimeUnit.NANOSECONDS.toMillis(elapsedNanoTime);
    }
}
