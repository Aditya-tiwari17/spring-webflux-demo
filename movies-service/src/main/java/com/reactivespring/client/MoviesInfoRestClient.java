package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.util.RetryUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

@Component
@Log4j2
public class MoviesInfoRestClient {
    private final WebClient webClient;
    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> getMovieInfo(String movieInfoId) {
        String url = moviesInfoUrl.concat("/{id}");
        return webClient.get()
                .uri(url, movieInfoId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("Status code is: {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException("There is no movieInfo available for passed id:"
                                +movieInfoId, clientResponse.statusCode().value()));
                    }
                    return clientResponse.bodyToMono(String.class).flatMap(responseMessage ->
                        Mono.error(new MoviesInfoClientException(responseMessage, clientResponse.statusCode().value())));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.info("Status code is: {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class).flatMap(responseMessage ->
                            Mono.error(new MoviesInfoServerException("Server exception in MoviesInfoService: "
                                    + responseMessage)));
                })
                .bodyToMono(MovieInfo.class)
//                .retry(3)
                .retryWhen(RetryUtil.getRetrySpec())
                .log();

    }
}
