package br.com.tiagoiwamoto.gofbehavioral.observer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoNotificarFornecedorXpto {

    @EventListener
    void handle(Evento evento){
        log.debug("Notificando fornecedor Xpto: {}", evento);
    }
}
