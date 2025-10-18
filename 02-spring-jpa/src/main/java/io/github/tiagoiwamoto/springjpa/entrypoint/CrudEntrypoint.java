package io.github.tiagoiwamoto.springjpa.entrypoint;

import io.github.tiagoiwamoto.springjpa.entity.Jogo;
import io.github.tiagoiwamoto.springjpa.entity.PlataformaEnum;
import io.github.tiagoiwamoto.springjpa.repository.JogoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrudEntrypoint {

    private final JogoRepository repository;

    @EventListener(ApplicationContextEvent.class)
    public void eventListener() {
        final Jogo jogo1 = Jogo.builder()
                .nome("The Witcher 3")
                .preco(79.99)
                .plataforma(PlataformaEnum.PS4)
                .build();
        Thread.ofVirtual().start(() -> {
            repository.save(jogo1);
            log.info("Jogo salvo: {}", jogo1);
        });

        final Jogo jogo2 = Jogo.builder()
                .nome("Elden Ring")
                .preco(199.99)
                .plataforma(PlataformaEnum.PS4)
                .build();
        Thread.ofVirtual().start(() -> {
            repository.save(jogo2);
            log.info("Jogo salvo: {}", jogo2);
        });

        final Jogo jogo3 = Jogo.builder()
                .nome("Horizon Zero Dawn")
                .preco(149.99)
                .plataforma(PlataformaEnum.PS4)
                .build();
        Thread.ofVirtual().start(() -> {
            repository.save(jogo3);
            log.info("Jogo salvo: {}", jogo3);
        });

        final Jogo jogo4 = Jogo.builder()
                .nome("God of War")
                .preco(99.99)
                .plataforma(PlataformaEnum.PS4)
                .build();
        Thread.ofVirtual().start(() -> {
            repository.save(jogo4);
            log.info("Jogo salvo: {}", jogo4);
        });

        log.info("Recuperando todos os jogos...");
        repository.findAll().forEach(j -> log.info("Jogo encontrado: {}", j));
    }

}
