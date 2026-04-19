package br.com.tiagoiwamoto.gofbehavioral.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyServiceApp {

    private final List<MissaoStrategy> missaoStrategies;

    public void pontuar(MissaoDto missaoDto){
        var missao = missaoStrategies.stream()
                .filter(missaoStrategy -> missaoStrategy.apply(missaoDto))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Missão selecionada é inválida."));
        log.debug("missao selecionada: {}", missao.getClass().getSimpleName());
        missao.execute(missaoDto);
    }

}
