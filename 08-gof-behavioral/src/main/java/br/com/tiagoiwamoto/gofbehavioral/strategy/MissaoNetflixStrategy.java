package br.com.tiagoiwamoto.gofbehavioral.strategy;

public class MissaoNetflixStrategy implements MissaoStrategy{
    @Override
    public void execute(MissaoDto missaoDto) {

    }

    @Override
    public Boolean apply(MissaoDto missaoDto) {
        return missaoDto.missao().equalsIgnoreCase("netflix")
                && missaoDto.type().equalsIgnoreCase("valor");
    }
}
