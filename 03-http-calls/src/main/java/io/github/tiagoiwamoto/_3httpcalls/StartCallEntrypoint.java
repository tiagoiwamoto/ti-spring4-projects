package io.github.tiagoiwamoto._3httpcalls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;


@Component
@RequiredArgsConstructor
@Slf4j
public class StartCallEntrypoint {

    private final SwapiPort swapiPort;
    private final PokePort pokePort;

    @EventListener(ApplicationReadyEvent.class)
    public void execute() throws NoSuchAlgorithmException {
        log.info("### STARTING SWAPI CALL PROCESS ###");

        SwapiPersonResponse personResponse = swapiPort.getPersonById(1L);

        log.info("PERSON NAME: {}", personResponse.name());
        log.info("### SWAPI CALL PROCESS FINISHED ###");

        log.info("### STARTING POKEAPI CALL PROCESS ###");
        var pokemonResponse = pokePort.getPokemon();
        log.info("POKEMON NAME: {}", pokemonResponse.get("name"));
        log.info("### POKEAPI CALL PROCESS FINISHED ###");

    }

}
