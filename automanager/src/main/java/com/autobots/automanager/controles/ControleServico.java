package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.servicos.GerenciadorServico;

@RestController
@RequestMapping("/servicos")
public class ControleServico {

	private final GerenciadorServico gerenciador;

	public ControleServico(GerenciadorServico gerenciador) {
		this.gerenciador = gerenciador;
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@GetMapping
	public ResponseEntity<List<Servico>> listar() {
		return ResponseEntity.ok(gerenciador.listar());
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Servico> obter(@PathVariable Long id) {
		return ResponseEntity.ok(gerenciador.obter(id));
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
	@PostMapping
	public ResponseEntity<Servico> criar(@RequestBody Servico dados) {
		return ResponseEntity.status(HttpStatus.CREATED).body(gerenciador.criar(dados));
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
	@PutMapping("/{id}")
	public ResponseEntity<Servico> atualizar(@PathVariable Long id, @RequestBody Servico dados) {
		return ResponseEntity.ok(gerenciador.atualizar(id, dados));
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		gerenciador.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
