package com.reactivespring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
@Builder
public class Review {

    @Id
    private String reviewId;
    @NotNull(message = "movieInfoId must not be null")
    private Long movieInfoId;
    private String comment;
    @Min(value = 0L, message = "rating: please pass a non-negative value")
    private Double rating;
}
