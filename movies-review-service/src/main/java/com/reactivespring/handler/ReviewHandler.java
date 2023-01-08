package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.repository.MovieReviewRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;

@Component
@Log4j2
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private final MovieReviewRepository movieReviewRepository;

    public ReviewHandler(MovieReviewRepository movieReviewRepository) {
        this.movieReviewRepository = movieReviewRepository;
    }

    private void validate(Review review) {
        var constraintViolations = validator.validate(review);
        log.info("Constraint Violations: {}", constraintViolations);
        if (!constraintViolations.isEmpty()) {
            String errorMessage = constraintViolations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(movieReviewRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue)
                .log();
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        var movieInfoId = request.queryParam("movieInfoId");
        Flux<Review> movieReviews;
        if (movieInfoId.isPresent()) {
            movieReviews = movieReviewRepository.findByMovieInfoId(Long.valueOf(movieInfoId.get()));
        }
        else {
            movieReviews = movieReviewRepository.findAll();
        }
        return ServerResponse.ok().body(movieReviews, Review.class)
                .log();
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        String reviewId = request.pathVariable("id");
        return movieReviewRepository.findById(reviewId)
                .flatMap(review -> request.bodyToMono(Review.class)
                .map(requestReview -> {
                    review.setComment(requestReview.getComment());
                    review.setMovieInfoId(requestReview.getMovieInfoId());
                    review.setRating(requestReview.getRating());
                    return review;
                })
                        .flatMap(movieReviewRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview)))
//                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for given reviewId:" + reviewId)));
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        String reviewId = request.pathVariable("id");
        return movieReviewRepository.findById(reviewId)
                .flatMap(review -> movieReviewRepository.deleteById(reviewId))
                .then(ServerResponse.noContent().build());
    }
}
