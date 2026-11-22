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

import com.autobots.automanager.dto.UsuarioEntrada;
import com.autobots.automanager.dto.UsuarioSaida;
import com.autobots.automanager.servicos.GerenciadorUsuario;

@RestController
@RequestMapping("/usuarios")
public class ControleUsuario {

	private final GerenciadorUsuario gerenciador;

	public ControleUsuario(GerenciadorUsuario gerenciador) {
		this.gerenciador = gerenciador;
	}

	@GetMapping
	public ResponseEntity<List<UsuarioSaida>> listar() {
		return ResponseEntity.ok(gerenciador.listar());
	}

	@GetMapping("/me")
	public ResponseEntity<UsuarioSaida> obterAtual() {
		return ResponseEntity.ok(gerenciador.obterAtual());
	}

	@GetMapping("/{id}")
	public ResponseEntity<UsuarioSaida> obter(@PathVariable Long id) {
		return ResponseEntity.ok(gerenciador.obter(id));
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@PostMapping
	public ResponseEntity<UsuarioSaida> criar(@RequestBody UsuarioEntrada dados) {
		return ResponseEntity.status(HttpStatus.CREATED).body(gerenciador.criar(dados));
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@PutMapping("/{id}")
	public ResponseEntity<UsuarioSaida> atualizar(@PathVariable Long id, @RequestBody UsuarioEntrada dados) {
		return ResponseEntity.ok(gerenciador.atualizar(id, dados));
	}

	@PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		gerenciador.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
