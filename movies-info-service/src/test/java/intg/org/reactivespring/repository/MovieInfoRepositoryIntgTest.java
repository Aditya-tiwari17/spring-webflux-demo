package org.reactivespring.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivespring.entity.MovieInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntgTest {
    @Autowired
    MovieInfoRepository movieInfoRepository;

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
    void findAllTest() {
        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findByIdTest() {
        Mono<MovieInfo> moviesInfoMono = movieInfoRepository.findById("abc").log();

        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("Batman Dark Knight", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void findByYearTest() {
        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findByYear(2008).log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByNameTest() {
        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findByName("Batman Begins").log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void saveMovieInfoTest() {
        MovieInfo batman1 = MovieInfo.builder()
                .name("Batman Begins")
                .year(2005)
                .cast(List.of("Christian Bale", "Cillian Murphy", "Katie Holmes"))
                .releaseDate(LocalDate.parse("2005-06-17"))
                .build();

        Mono<MovieInfo> moviesInfoMono = movieInfoRepository.save(batman1).log();

        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("Batman Begins", movieInfo.getName());
                })
                .verifyComplete();

        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void updateMovieInfoTest() {
        MovieInfo moviesInfo = movieInfoRepository.findById("abc").log().block();
        assert moviesInfo != null;
        moviesInfo.setYear(2022);

        Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(moviesInfo).log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals(2022, movieInfo.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfoTest() {
        movieInfoRepository.deleteById("abc").log().block();

        Flux<MovieInfo> movieInfoList = movieInfoRepository.findAll().log();

        StepVerifier.create(movieInfoList)
                .expectNextCount(2)
                .verifyComplete();
    }
}
