package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FluxAndMonoGeneratorServiceTest {
    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void namesFluxUpperTest() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxUpper())
                .expectNext("ADITYA", "YOGESH", "SONVEER")
                .verifyComplete();
    }

    @Test
    void namesFluxTest() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFlux())
                .expectNext("Aditya", "Yogesh", "Sonveer")
                .verifyComplete();
    }

    @Test
    void namesFluxFlatMap() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxFlatMap())
                .expectNext("A", "D", "I", "T", "Y", "A", "Y", "O", "G", "E", "S", "H", "S", "O", "N", "V", "E", "E", "R")
//                .assertNext("ADITYA")
                .verifyComplete();
    }

    @Test
    void nameMonoMapFilter() {
        StepVerifier.create(fluxAndMonoGeneratorService.nameMonoMapFilter(5))
                .expectNext("ADITYA")
                .verifyComplete();
    }

    @Test
    void nameMonoFlatMap() {
        StepVerifier.create(fluxAndMonoGeneratorService.nameMonoFlatMap(5))
                .expectNext(List.of("A", "D", "I", "T", "Y", "A"))
                .verifyComplete();
    }

    @Test
    void nameMonoFlatMapMany() {
        StepVerifier.create(fluxAndMonoGeneratorService.nameMonoFlatMapMany(5))
                .expectNext("A", "D", "I", "T", "Y", "A")
                .verifyComplete();
    }

    @Test
    void namesFluxTransform() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxTransform(4))
                .expectNext("YOGESH", "SONVEER")
                .verifyComplete();
    }

    @Test
    void namesFluxTransformDefaultIfEmpty() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxTransform(7))
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    void namesFluxTransformSwitchIfEmpty() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxTransformSwitchIfEmpty(7))
                .expectNext("DEFAULT VALUE")
                .verifyComplete();
    }

    @Test
    void fluxConcat() {
        StepVerifier.create(fluxAndMonoGeneratorService.fluxConcat())
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void fluxMerge() {
        StepVerifier.create(fluxAndMonoGeneratorService.fluxMerge())
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }
}