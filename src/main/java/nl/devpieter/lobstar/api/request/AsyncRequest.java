package nl.devpieter.lobstar.api.request;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AsyncRequest<T> extends RequestHelper {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private final List<IRequestCallback<T>> callbacks = new ArrayList<>();
    private CompletableFuture<T> future;

    public AsyncRequest() {
        this(null);
    }

    public AsyncRequest(@Nullable IRequestCallback<T> requestCallback) {
        this.callbacks.add(requestCallback);
    }

    public static void shutdown() {
        EXECUTOR_SERVICE.shutdown();
    }

    private void callCallbacks(@Nullable T result, @Nullable Exception exception) {
        for (IRequestCallback<T> callback : this.callbacks) {
            if (callback != null) callback.onResult(result, exception);
        }
    }

    public void addCallback(@Nullable IRequestCallback<T> callback) {
        if (callback == null) return;
        this.callbacks.add(callback);
    }

    public AsyncRequest<T> execute() {
        if (this.future != null) return this;

        this.future = CompletableFuture.supplyAsync(() -> {
            try {
                T result = requestAsync();
                this.callCallbacks(result, null);
                return result;
            } catch (Exception e) {
                this.callCallbacks(null, e);
                throw new RuntimeException(e);
            }
        }, EXECUTOR_SERVICE);

        return this;
    }

    public void cancel() {
        this.future.cancel(true);
    }

    public boolean isDone() {
        return this.future.isDone();
    }

    public T get() throws ExecutionException, InterruptedException {
        return this.future.get();
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    protected abstract @Nullable T requestAsync() throws Exception;
}