package br.com.tiagoiwamoto.gofbehavioral.observer;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record Evento(

        String type,
        BigDecimal valor,
        LocalDate dataEvento

) {
}
