package br.com.tiagoiwamoto.gofbehavioral.observer;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ObserverServiceApp {

    private final ApplicationEventPublisher eventPublisher;

    void notificarMissaoConcluida(Evento evento){
        eventPublisher.publishEvent(evento);
    }

}
