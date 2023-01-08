package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.MovieReviewRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
class ReviewsUnitTest {
    @MockBean
    private MovieReviewRepository movieReviewRepository;

    @Autowired
    private WebTestClient webTestClient;

    private final String MOVIES_REVIEW_URI = "/v1/reviews";

    @Test
    void getReviews() {
        Review review1 = Review.builder()
                .reviewId("abc")
                .movieInfoId(1L)
                .comment("Great Movie")
                .rating(9.0)
                .build();

        Review review2 = Review.builder()
                .reviewId("def")
                .movieInfoId(2L)
                .comment("Awesome graphics, best experience")
                .rating(9.5)
                .build();

        Review review3 = Review.builder()
                .reviewId("ghi")
                .movieInfoId(2L)
                .comment("Not as good as the previous parts")
                .rating(6.0)
                .build();

        List<Review> movieReviews = List.of(review1, review2, review3);

        when(movieReviewRepository.findAll()).thenReturn(Flux.fromIterable(movieReviews));

        webTestClient
                .get()
                .uri(MOVIES_REVIEW_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void getReviewsByMovieInfoId() {
        URI uri = UriComponentsBuilder.fromUriString(MOVIES_REVIEW_URI).queryParam("movieInfoId", 2L)
                .buildAndExpand().toUri();

        Review review1 = Review.builder()
                .reviewId("def")
                .movieInfoId(2L)
                .comment("Awesome graphics, best experience")
                .rating(9.5)
                .build();

        Review review2 = Review.builder()
                .reviewId("ghi")
                .movieInfoId(2L)
                .comment("Not as good as the previous parts")
                .rating(6.0)
                .build();

        List<Review> movieReviews = List.of(review1, review2);

        when(movieReviewRepository.findByMovieInfoId(isA(Long.class))).thenReturn(Flux.fromIterable(movieReviews));

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void addReview() {
        Review review = Review.builder()
                .reviewId("abc")
                .movieInfoId(1L)
                .comment("Great Movie")
                .rating(9.0)
                .build();

        when(movieReviewRepository.save(isA(Review.class))).thenReturn(Mono.just(review));

        webTestClient
                .post()
                .uri(MOVIES_REVIEW_URI)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review savedMovieReview = reviewEntityExchangeResult.getResponseBody();
                    assert savedMovieReview != null;
                    assertNotNull(savedMovieReview.getReviewId());
                    assertEquals("abc", savedMovieReview.getReviewId());
                });
    }

    @Test
    void addReviewValidation() {
        Review review = Review.builder()
                .reviewId("abc")
                .movieInfoId(null)
                .comment("Great Movie")
                .rating(-9.0)
                .build();

        when(movieReviewRepository.save(isA(Review.class))).thenReturn(Mono.just(review));

        webTestClient
                .post()
                .uri(MOVIES_REVIEW_URI)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("movieInfoId must not be null,rating: please pass a non-negative value");
    }

    @Test
    void updateReview() {
        String reviewId = "abc";
        Review review = Review.builder()
                .reviewId("abc")
                .movieInfoId(1L)
                .comment("Updated Comment")
                .rating(9.6)
                .build();

        when(movieReviewRepository.findById("abc")).thenReturn(Mono.just(review));
        when(movieReviewRepository.save(isA(Review.class))).thenReturn(Mono.just(review));


        webTestClient
                .put()
                .uri(MOVIES_REVIEW_URI+"/{id}", reviewId)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review updatedMovieReview = reviewEntityExchangeResult.getResponseBody();
                    assert updatedMovieReview != null;
                    assertNotNull(updatedMovieReview.getReviewId());
                    assertEquals(9.6, updatedMovieReview.getRating());
                    assertEquals("Updated Comment", updatedMovieReview.getComment());
                });
    }

    @Test
    void deleteMovieReviewById() {
        String movieInfoId = "abc";
        Review review = Review.builder()
                .reviewId("abc")
                .movieInfoId(1L)
                .comment("Great Movie")
                .rating(9.0)
                .build();
        when(movieReviewRepository.findById("abc")).thenReturn(Mono.just(review));
        when(movieReviewRepository.deleteById(isA(String.class))).thenReturn(Mono.empty());
        webTestClient
                .delete()
                .uri(MOVIES_REVIEW_URI+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
