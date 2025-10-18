package io.github.tiagoiwamoto.springjpa.repository;

import io.github.tiagoiwamoto.springjpa.entity.Jogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JogoRepository extends JpaRepository<Jogo, Long> {
}
