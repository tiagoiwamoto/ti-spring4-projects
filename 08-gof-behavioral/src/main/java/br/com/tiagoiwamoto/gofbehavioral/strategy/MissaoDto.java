package br.com.tiagoiwamoto.gofbehavioral.strategy;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record MissaoDto(
        String missao,
        String type,
        BigDecimal valor,
        LocalDate dataReferencia

) {
}
