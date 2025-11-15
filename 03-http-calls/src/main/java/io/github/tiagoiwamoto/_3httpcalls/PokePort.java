package io.github.tiagoiwamoto._3httpcalls;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Map;

@HttpExchange
public interface PokePort {

    @GetExchange("/pokemon/ditto")
    Map<String, Object> getPokemon();
}
