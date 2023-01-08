package org.reactivespring.controller;

import org.reactivespring.entity.MovieInfo;
import org.reactivespring.service.MoviesInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class MoviesInfoController {
    private final MoviesInfoService moviesInfoService;

    public MoviesInfoController(MoviesInfoService moviesInfoService) {
        this.moviesInfoService = moviesInfoService;
    }

    @GetMapping("/movies-info")
    public Flux<MovieInfo> getAllMovieInfo(@RequestParam(value = "year", required = false) Integer year) {
        if (year != null) {
            return moviesInfoService.getMovieInfoByYear(year);
        }
        return moviesInfoService.getAllMovieInfo().log();
    }

    @GetMapping("/movies-info/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id) {
        return moviesInfoService.getMovieInfoById(id)
                        .map(ResponseEntity.ok()::body)
                                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())).log();
    }

    @PostMapping("/movies-info")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return moviesInfoService.addMovieInfo(movieInfo).log();
    }

    @PutMapping("/movies-info/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@PathVariable String id, @RequestBody @Valid MovieInfo movieInfo) {
        return moviesInfoService.updateMovieInfo(id, movieInfo)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())).log();
    }

    @DeleteMapping("/movies-info/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfoById(@PathVariable String id) {
        return moviesInfoService.deleteMovieInfoById(id).log();
    }
}
