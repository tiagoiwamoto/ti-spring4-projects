package io.github.tiagoiwamoto._3httpcalls;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface SwapiPort {

    @GetExchange("/people/{id}/")
    SwapiPersonResponse getPersonById(@PathVariable(name = "id") Long id);
}
