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

import com.autobots.automanager.dto.VendaEntrada;
import com.autobots.automanager.dto.VendaSaida;
import com.autobots.automanager.servicos.GerenciadorVenda;

@RestController
@RequestMapping("/vendas")
public class ControleVenda {

	private final GerenciadorVenda gerenciador;

	public ControleVenda(GerenciadorVenda gerenciador) {
		this.gerenciador = gerenciador;
	}

	@GetMapping
	public ResponseEntity<List<VendaSaida>> listar() {
		return ResponseEntity.ok(gerenciador.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<VendaSaida> obter(@PathVariable Long id) {
		return ResponseEntity.ok(gerenciador.obter(id));
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@PostMapping
	public ResponseEntity<VendaSaida> criar(@RequestBody VendaEntrada dados) {
		return ResponseEntity.status(HttpStatus.CREATED).body(gerenciador.criar(dados));
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
	@PutMapping("/{id}")
	public ResponseEntity<VendaSaida> atualizar(@PathVariable Long id, @RequestBody VendaEntrada dados) {
		return ResponseEntity.ok(gerenciador.atualizar(id, dados));
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		gerenciador.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
