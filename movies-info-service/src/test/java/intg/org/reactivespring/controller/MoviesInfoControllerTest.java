package org.reactivespring.controller;

import io.netty.handler.codec.http.HttpScheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivespring.entity.MovieInfo;
import org.reactivespring.repository.MovieInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//Below mentioned profile should be different from all profiles mentioned in application.yaml
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerTest {
    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    private final String MOVIES_INFO_URI = "/v1/movies-info";

    @BeforeEach
    void setUp() {
        MovieInfo batman1 = MovieInfo.builder()
                .name("Batman Begins")
                .year(2005)
                .cast(List.of("Christian Bale", "Cillian Murphy", "Katie Holmes"))
                .releaseDate(LocalDate.parse("2005-06-17"))
                .build();

        MovieInfo batman2 = MovieInfo.builder()
                .movieInfoId("abc")
                .name("Batman Dark Knight")
                .year(2008)
                .cast(List.of("Christian Bale", "Heath Ledger", "Gary Oldman"))
                .releaseDate(LocalDate.parse("2008-07-18"))
                .build();

        MovieInfo batman3 = MovieInfo.builder()
                .name("Batman Dark Knight Rises")
                .year(2012)
                .cast(List.of("Christian Bale", "Joseph Gorden Levitt", "Anne Hathaway"))
                .releaseDate(LocalDate.parse("2012-07-20"))
                .build();

        List<MovieInfo> moviesInfo = List.of(batman1, batman2, batman3);
        movieInfoRepository.saveAll(moviesInfo)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {
        MovieInfo batman = MovieInfo.builder()
                .name("Batman Begins")
                .year(2005)
                .cast(List.of("Christian Bale", "Cillian Murphy", "Katie Holmes"))
                .releaseDate(LocalDate.parse("2005-06-17"))
                .build();
        webTestClient
                .post()
                .uri(MOVIES_INFO_URI)
                .bodyValue(batman)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                });
    }

    @Test
    void getAllMoviesInfo() {
        webTestClient
                .get()
                .uri(MOVIES_INFO_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoByYear() {
        // Build URL with query parameter
        URI uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_URI).queryParam("year", 2005)
                        .buildAndExpand().toUri();
        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getMovieInfoById() {
        String movieInfoId = "abc";
        webTestClient
                .get()
                .uri(MOVIES_INFO_URI+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                // Allows to fetch a value from response json of API(name in our case)
                .jsonPath("$.name").isEqualTo("Batman Dark Knight");
//                .expectBody(MovieInfo.class)
//                .consumeWith(movieInfoEntityExchangeResult -> {
//                    MovieInfo movieInfo = movieInfoEntityExchangeResult.getResponseBody();
//                    assert movieInfo != null;
//                    assertEquals("Batman Dark Knight", movieInfo.getName());
//
//                });
    }

    @Test
    void getMovieInfoByIdNotFound() {
        String movieInfoId = "def";
        webTestClient
                .get()
                .uri(MOVIES_INFO_URI+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void updateMovieInfo() {
        String movieInfoId = "abc";
        MovieInfo batman = MovieInfo.builder()
                .name("Batman Begins Updated")
                .year(2005)
                .cast(List.of("Christian Bale", "Cillian Murphy", "Katie Holmes"))
                .releaseDate(LocalDate.parse("2005-06-18"))
                .build();
        webTestClient
                .put()
                .uri(MOVIES_INFO_URI+"/{id}", movieInfoId)
                .bodyValue(batman)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                // Allows to fetch a value from response json of API(name in our case)
                .jsonPath("$.name").isEqualTo("Batman Begins Updated")
                .jsonPath("$.releaseDate").isEqualTo("2005-06-18");
    }

    @Test
    void updateMovieInfoNotFound() {
        String movieInfoId = "def";
        MovieInfo batman = MovieInfo.builder()
                .name("Batman Begins Updated")
                .year(2005)
                .cast(List.of("Christian Bale", "Cillian Murphy", "Katie Holmes"))
                .releaseDate(LocalDate.parse("2005-06-18"))
                .build();
        webTestClient
                .put()
                .uri(MOVIES_INFO_URI+"/{id}", movieInfoId)
                .bodyValue(batman)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteMovieInfoById() {
        String movieInfoId = "abc";
        webTestClient
                .delete()
                .uri(MOVIES_INFO_URI+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}