package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Flux<String> splitString(String s) {
        return Flux.just(s.split(""));
    }

    private Mono<List<String>> splitStringMono(String s) {
        String[] charArray = s.split("");
        List<String> charList = List.of(charArray);

        return Mono.just(charList);
    }

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("Aditya", "Yogesh", "Sonveer"));
    }

    public Flux<String> namesFluxUpper() {
        return Flux.fromIterable(List.of("Aditya", "Yogesh", "Sonveer"))
                .map(String::toUpperCase);
    }

    public Flux<String> namesFluxFlatMap() {
        return Flux.fromIterable(List.of("Aditya", "Yogesh", "Sonveer"))
                .map(String::toUpperCase)
                .flatMap(this::splitString);
    }

    public Flux<String> namesFluxTransform(int length) {
//        Flux<String> and Flux<String> in Function below are input and output types for the Function functional interface
        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > length);
        return Flux.fromIterable(List.of("Adi", "Yogesh", "Sonveer"))
                .transform(filterMap)
                .defaultIfEmpty("default");
    }

    public Flux<String> namesFluxTransformSwitchIfEmpty(int length) {
//        Flux<String> and Flux<String> in Function below are input and output types for the Function functional interface
        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > length);

        Flux<String> defaultValue = Flux.just("default value").transform(filterMap);

        return Flux.fromIterable(List.of("Adi", "Yogesh", "Sonveer"))
                .transform(filterMap)
                .switchIfEmpty(defaultValue)
                .log();
    }

    public Flux<String> fluxConcat() {
        Flux<String> fluxAbc = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        Flux<String> fluxDef = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(100));
        // Will be subscribed sequentially
        return Flux.concat(fluxAbc, fluxDef);
    }

    public Flux<String> fluxMerge() {
        Flux<String> fluxAbc = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        Flux<String> fluxDef = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));
        // Will be subscribed in parallel
        return Flux.merge(fluxAbc, fluxDef);
    }

    public Mono<String> nameMono() {
        return Mono.just("Aditya");
    }

    public Mono<String> nameMonoMapFilter(int stringLength) {
        return Mono.just("aditya")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength);
    }

    public Mono<List<String>> nameMonoFlatMap(int stringLength) {
        return Mono.just("aditya")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitStringMono)
                .log();
    }

    public Flux<String> nameMonoFlatMapMany(int stringLength) {
        return Mono.just("aditya")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMapMany(this::splitString)
                .log();
    }

    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

        fluxAndMonoGeneratorService.namesFlux().subscribe(name -> System.out.println("Flux->"+name));

        fluxAndMonoGeneratorService.nameMono().subscribe(name -> System.out.println("Mono->"+name));

        fluxAndMonoGeneratorService.namesFluxUpper().subscribe(name -> System.out.println("Flux upper->"+name));
    }
}
