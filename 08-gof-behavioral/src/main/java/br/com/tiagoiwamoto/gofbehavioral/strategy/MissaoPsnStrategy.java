package br.com.tiagoiwamoto.gofbehavioral.strategy;

public class MissaoPsnStrategy implements MissaoStrategy{
    @Override
    public void execute(MissaoDto missaoDto) {

    }

    @Override
    public Boolean apply(MissaoDto missaoDto) {
        return missaoDto.missao().equalsIgnoreCase("psn")
                && missaoDto.type().equalsIgnoreCase("valor");
    }
}
