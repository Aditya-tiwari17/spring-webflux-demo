package org.reactivespring.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reactivespring.entity.MovieInfo;
import org.reactivespring.service.MoviesInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoServiceMock;

    private final String MOVIES_INFO_URI = "/v1/movies-info";

    MoviesInfoControllerUnitTest(MoviesInfoService moviesInfoServiceMock) {
        this.moviesInfoServiceMock = moviesInfoServiceMock;
    }

    @Test
    void getAllMoviesInfo() {
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

        when(moviesInfoServiceMock.getAllMovieInfo()).thenReturn(Flux.fromIterable(moviesInfo));

        webTestClient
                .get()
                .uri(MOVIES_INFO_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                // Expect list in body in response
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        MovieInfo batman = MovieInfo.builder()
                .movieInfoId("abc")
                .name("Batman Dark Knight")
                .year(2008)
                .cast(List.of("Christian Bale", "Heath Ledger", "Gary Oldman"))
                .releaseDate(LocalDate.parse("2008-07-18"))
                .build();

        String movieInfoId = "abc";

        when(moviesInfoServiceMock.getMovieInfoById(movieInfoId)).thenReturn(Mono.just(batman));

        webTestClient.get()
                .uri(MOVIES_INFO_URI+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                // Expect body in response
                .expectBody()
                // Allows to fetch a value from response json of API(name in our case)
                .jsonPath("$.name").isEqualTo("Batman Dark Knight");
    }

    @Test
    void addMovieInfo() {
        MovieInfo batman = MovieInfo.builder()
                .movieInfoId("mockId")
                .name("Batman Begins")
                .year(2005)
                .cast(List.of("Christian Bale", "Cillian Murphy", "Katie Holmes"))
                .releaseDate(LocalDate.parse("2005-06-17"))
                .build();

        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(batman));
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
                    assertEquals("Batman Begins", savedMovieInfo.getName());
                });
    }

    @Test
    void updateMovieInfo() {
        String movieInfoId = "abc";
        MovieInfo batman = MovieInfo.builder()
                .movieInfoId(movieInfoId)
                .name("Batman Begins Updated")
                .year(2005)
                .cast(List.of("Christian Bale", "Cillian Murphy", "Katie Holmes"))
                .releaseDate(LocalDate.parse("2005-06-18"))
                .build();

        when(moviesInfoServiceMock.updateMovieInfo(isA(String.class), isA(MovieInfo.class))).thenReturn(Mono.just(batman));

        webTestClient
                .put()
                .uri(MOVIES_INFO_URI+"/{id}", movieInfoId)
                .bodyValue(batman)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                // Expect body in response
                .expectBody()
                // Allows to fetch a value from response json of API(name in our case)
                .jsonPath("$.name").isEqualTo("Batman Begins Updated")
                .jsonPath("$.releaseDate").isEqualTo("2005-06-18");
    }

    @Test
    void deleteMovieInfoById() {
        String movieInfoId = "abc";
        when(moviesInfoServiceMock.deleteMovieInfoById(isA(String.class))).thenReturn(Mono.empty());
        webTestClient
                .delete()
                .uri(MOVIES_INFO_URI+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    // To test bean validations on entity MovieInfo like in name and year
    @DisplayName(value = "Bean validations test")
    void addMovieInfoValidation() {
        MovieInfo batman = MovieInfo.builder()
                .name("")
                .year(-2005)
                .cast(List.of("Christian Bale", "", "Katie Holmes"))
                .releaseDate(LocalDate.parse("2005-06-17"))
                .build();
        webTestClient
                .post()
                .uri(MOVIES_INFO_URI)
                .bodyValue(batman)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    String response = stringEntityExchangeResult.getResponseBody();
                    assertEquals("cast should not be blank,name should not be blank,year should be a positive number", response);
                });
    }
}
