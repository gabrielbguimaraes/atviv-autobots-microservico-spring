package com.autobots.automanager.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Servico;

public interface RepositorioServico extends JpaRepository<Servico, Long> {
	Optional<Servico> findFirstByNome(String nome);
}
