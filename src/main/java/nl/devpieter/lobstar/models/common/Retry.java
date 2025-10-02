package nl.devpieter.lobstar.models.common;

import java.time.Duration;

public class Retry {

    private final Duration delay;
    private final Duration maxDelay;
    private final double backoffFactor;

    private int count;

    public Retry(Duration delay, Duration maxDelay, double backoffFactor) {
        this.delay = delay;
        this.maxDelay = maxDelay;
        this.backoffFactor = backoffFactor;
        this.count = 0;
    }

    public Duration getNext() {
        double nextDelay = this.delay.toMillis() * Math.pow(this.backoffFactor, this.count);

        if (nextDelay > this.maxDelay.toMillis()) {
            nextDelay = this.maxDelay.toMillis();
        }

        this.count++;
        return Duration.ofMillis((long) nextDelay);
    }

    public void reset() {
        this.count = 0;
    }
}
