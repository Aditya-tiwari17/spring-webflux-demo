package org.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {
    @Test
    void sink() {
        Sinks.Many<Integer> replaySInk = Sinks.many().replay().all();

        replaySInk.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySInk.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySInk.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux = replaySInk.asFlux();
        integerFlux.subscribe(i-> {
            System.out.println("Subscriber 1:" + i);
        });
    }
}
