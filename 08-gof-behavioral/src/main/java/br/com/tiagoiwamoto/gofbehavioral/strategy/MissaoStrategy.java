package br.com.tiagoiwamoto.gofbehavioral.strategy;

public interface MissaoStrategy {

    void execute(MissaoDto missaoDto);
    Boolean apply(MissaoDto missaoDto);

}
