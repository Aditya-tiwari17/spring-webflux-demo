package com.reactivespring.router;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {
    @Bean
    public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler) {
        return route()
//                .GET("/v1/reviews", reviewHandler::getReviews)
//                .POST("/v1/reviews", reviewHandler::addReview)
                // nesting requests with same URL prefix (i.e /v1/reviews)
                .nest(path("/v1/reviews"), builder -> builder
                        .GET("", reviewHandler::getReviews)
                        .POST("", reviewHandler::addReview)
                        .PUT("/{id}", reviewHandler::updateReview)
                        .DELETE("/{id}", reviewHandler::deleteReview))
                .build();
    }
}
