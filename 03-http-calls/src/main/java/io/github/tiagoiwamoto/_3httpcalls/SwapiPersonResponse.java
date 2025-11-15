package io.github.tiagoiwamoto._3httpcalls;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SwapiPersonResponse(
    String name,
    String height,
    String mass,
    String hairColor,
    String skinColor,
    String eyeColor,
    String birthYear,
    String gender,
    String homeworld,
    List<String> films,
    List<String> species,
    List<String> vehicles,
    List<String> starships,
    LocalDateTime created,
    LocalDateTime edited,
    String url
) {
}
