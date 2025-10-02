package nl.devpieter.lobstar.managers;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.models.common.Retry;
import nl.devpieter.lobstar.models.common.RetryHolder;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RetryManager {

    private static RetryManager INSTANCE;

    private final Logger logger = Lobstar.getInstance().getLogger();

    private final List<RetryHolder> retryHolders = new ArrayList<>();

    private RetryManager() {
    }

    public static RetryManager getInstance() {
        if (INSTANCE == null) INSTANCE = new RetryManager();
        return INSTANCE;
    }

    public RetryHolder register(String name, Retry retry, Runnable action) {
        RetryHolder holder = new RetryHolder(name, retry, action);
        this.retryHolders.add(holder);
        return holder;
    }

    public Disposable retry(String name) {
        for (RetryHolder holder : this.retryHolders) {
            if (!holder.identifier().equals(name)) continue;
            return this.retry(holder);
        }

        return null;
    }

    public Disposable retry(RetryHolder holder) {
        Duration nextDelay = holder.retry().getNext();

        this.logger.info("Retrying '{}' in {} seconds", holder.identifier(), nextDelay.toSeconds());

        return Completable.timer(nextDelay.toMillis(), TimeUnit.MILLISECONDS)
                .doOnComplete(holder.onRetry()::run)
                .subscribe();
    }
}
