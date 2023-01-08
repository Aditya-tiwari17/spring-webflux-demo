package com.reactivespring.controller;

import com.reactivespring.client.MovieReviewsRestClient;
import com.reactivespring.client.MoviesInfoRestClient;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.Review;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/v1/movies")
public class MoviesController {

    private final MoviesInfoRestClient moviesInfoRestClient;
    private final MovieReviewsRestClient movieReviewsRestClient;

    public MoviesController(MoviesInfoRestClient moviesInfoRestClient, MovieReviewsRestClient movieReviewsRestClient) {
        this.moviesInfoRestClient = moviesInfoRestClient;
        this.movieReviewsRestClient = movieReviewsRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> getMovieById(@PathVariable("id") String movieId) {
        return moviesInfoRestClient.getMovieInfo(movieId)
                .flatMap(movieInfo -> {
                    Mono<List<Review>> reviewsList = movieReviewsRestClient.getReviews(movieId)
                            .collectList();

                    return reviewsList.map(reviews -> new Movie(movieInfo, reviews));
                })
                .switchIfEmpty(Mono.just(new Movie()));
    }
}
