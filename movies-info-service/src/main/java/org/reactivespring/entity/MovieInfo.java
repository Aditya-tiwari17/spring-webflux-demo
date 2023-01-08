package org.reactivespring.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class MovieInfo {
    @Id
    private String movieInfoId;
    @NotBlank(message = "name should not be blank")
    private String name;
    @NotNull
    @Positive(message = "year should be a positive number")
    private Integer year;
    private List<@NotBlank(message = "cast should not be blank") String> cast;
    private LocalDate releaseDate;
}
