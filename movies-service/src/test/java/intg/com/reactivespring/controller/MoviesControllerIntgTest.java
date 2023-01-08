package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl: http://localhost:8084/v1/movies-info",
                "restClient.reviewsUrl: http://localhost:8081/v1/reviews"
        }
)
class MoviesControllerIntgTest {
    @Autowired
    WebTestClient webTestClient;

    @Test
    void getMovieById() {
        String movieId = "abc";

        // Mocking movies-info-service using Wiremock
        stubFor(get(urlEqualTo("/v1/movies-info"+"/"+movieId))
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBodyFile("movieInfo.json")));

        // Mocking movies-review-service using Wiremock
        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBodyFile("reviews.json")));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });
    }

    @Test
    void getMovieById5xx() {
        String movieId = "abc";

        // Mocking movies-info-service using Wiremock
        stubFor(get(urlEqualTo("/v1/movies-info"+"/"+movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MoviesInfoService Unavailable")));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("MoviesInfoService Unavailable");
    }

    @Test
    void getMovieById5xxRetry() {
        String movieId = "abc";

        // Mocking movies-info-service using Wiremock
        stubFor(get(urlEqualTo("/v1/movies-info"+"/"+movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MoviesInfoService Unavailable")));

        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("MoviesInfoService Unavailable");

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movies-info"+"/"+movieId)));
    }
}
