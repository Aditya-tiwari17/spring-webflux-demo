package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.util.RetryUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.net.URI;

@Component
public class MovieReviewsRestClient {
    private final WebClient webClient;

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    public MovieReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> getReviews(String movieInfoId) {
        URI url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieInfoId)
                .buildAndExpand()
                .toUri();

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .retryWhen(RetryUtil.getRetrySpec())
                .log();
    }
}
