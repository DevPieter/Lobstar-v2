package nl.devpieter.lobstar.api.request;

import org.jetbrains.annotations.Nullable;

public interface IRequestCallback<T> {
    void onResult(@Nullable T result, @Nullable Exception exception);
}
