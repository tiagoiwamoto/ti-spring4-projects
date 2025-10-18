package io.github.tiagoiwamoto.simpleapi.entrypoint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/explore-endpoints")
@RequiredArgsConstructor
@Slf4j
public class ExploreEndpointsRest {

    @GetMapping
    public ResponseEntity recuperarMeusJogos(){
        log.info("Explorando endpoints da aplicação");
        return ResponseEntity.ok("Endpoints explorados com sucesso!");
    }

}
