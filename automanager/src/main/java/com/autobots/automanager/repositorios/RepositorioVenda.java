package com.autobots.automanager.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Venda;

public interface RepositorioVenda extends JpaRepository<Venda, Long> {
	List<Venda> findByFuncionarioId(Long funcionarioId);

	List<Venda> findByClienteId(Long clienteId);

	Optional<Venda> findFirstByIdentificacao(String identificacao);

	boolean existsByClienteIdOrFuncionarioId(Long clienteId, Long funcionarioId);

	boolean existsByMercadoriasId(Long mercadoriaId);

	boolean existsByServicosId(Long servicoId);
}
