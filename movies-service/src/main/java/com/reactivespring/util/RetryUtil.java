package com.reactivespring.util;

import com.reactivespring.exception.MoviesInfoServerException;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;

import java.time.Duration;

public class RetryUtil {
    public static Retry getRetrySpec() {
        return Retry.fixedDelay(3, Duration.ofSeconds(1))
                .filter(MoviesInfoServerException.class::isInstance)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure()));
    }
}
