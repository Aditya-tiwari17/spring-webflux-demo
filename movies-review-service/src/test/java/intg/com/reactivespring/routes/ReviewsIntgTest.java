package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.MovieReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ReviewsIntgTest {
    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MovieReviewRepository movieReviewRepository;

    private final String MOVIES_REVIEW_URI = "/v1/reviews";

    @BeforeEach
    void setUp() {
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
        movieReviewRepository.saveAll(movieReviews)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieReviewRepository.deleteAll().block();
    }

    @Test
    void getReviews() {
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
                .movieInfoId(1L)
                .comment("Great Movie")
                .rating(9.0)
                .build();

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
                });
    }

    @Test
    void updateReview() {
        String reviewId = "abc";
        Review review = Review.builder()
                .movieInfoId(1L)
                .comment("Awesome")
                .rating(9.6)
                .build();

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
                    assertEquals("Awesome", updatedMovieReview.getComment());
                });
    }

    @Test
    void updateReviewNotFound() {
        String reviewId = "xyz";
        Review review = Review.builder()
                .movieInfoId(1L)
                .comment("Awesome")
                .rating(9.6)
                .build();

        webTestClient
                .put()
                .uri(MOVIES_REVIEW_URI+"/{id}", reviewId)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isNotFound();
//                .expectBody(String.class)
//                .isEqualTo("Review not found for given reviewId:xyz");
    }

    @Test
    void deleteReview() {
        String reviewId = "abc";

        webTestClient
                .delete()
                .uri(MOVIES_REVIEW_URI+"/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}
