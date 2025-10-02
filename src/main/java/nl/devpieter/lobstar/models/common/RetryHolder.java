package nl.devpieter.lobstar.models.common;

public record RetryHolder(String identifier, Retry retry, Runnable onRetry) {

}
