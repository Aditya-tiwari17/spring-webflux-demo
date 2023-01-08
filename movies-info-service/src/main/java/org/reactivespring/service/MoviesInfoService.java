package org.reactivespring.service;

import org.reactivespring.entity.MovieInfo;
import org.reactivespring.repository.MovieInfoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MoviesInfoService {

    private MovieInfoRepository movieInfoRepository;

    public MoviesInfoService(MovieInfoRepository movieInfoRepository) {
        this.movieInfoRepository = movieInfoRepository;
    }

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return movieInfoRepository.save(movieInfo);
    }

    public Flux<MovieInfo> getAllMovieInfo() {
        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> getMovieInfoById(String id) {
        return movieInfoRepository.findById(id);
    }

    public Mono<MovieInfo> updateMovieInfo(String id, MovieInfo movieInfo) {
        return movieInfoRepository.findById(id)
                .flatMap(movieInfo1 -> {
                    movieInfo1.setName(movieInfo.getName());
                    movieInfo1.setYear(movieInfo.getYear());
                    movieInfo1.setCast(movieInfo.getCast());
                    movieInfo1.setReleaseDate(movieInfo.getReleaseDate());
                    return movieInfoRepository.save(movieInfo1);
                });
    }

    public Mono<Void> deleteMovieInfoById(String id) {
        return movieInfoRepository.deleteById(id);
    }

    public Flux<MovieInfo> getMovieInfoByYear(Integer year) {
        return movieInfoRepository.findByYear(year).log();
    }
}
