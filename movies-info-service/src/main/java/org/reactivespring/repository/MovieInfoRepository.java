package org.reactivespring.repository;

import org.reactivespring.entity.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface MovieInfoRepository extends ReactiveMongoRepository<MovieInfo, String> {
    Flux<MovieInfo> findByYear(Integer year);

    Flux<MovieInfo> findByName(String name);
}
