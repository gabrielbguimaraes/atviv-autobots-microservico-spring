package com.autobots.automanager.servicos;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dto.UsuarioEntrada;
import com.autobots.automanager.dto.UsuarioSaida;
import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.excecoes.ConflitoException;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.excecoes.RequisicaoInvalidaException;
import com.autobots.automanager.modelos.Perfil;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVenda;

@Service
@Transactional
public class GerenciadorUsuario {

	private final RepositorioUsuario repositorio;
	private final RepositorioVenda repositorioVenda;
	private final AutorizacaoServico autorizacao;
	private final PasswordEncoder codificador;

	public GerenciadorUsuario(RepositorioUsuario repositorio, RepositorioVenda repositorioVenda,
			AutorizacaoServico autorizacao, PasswordEncoder codificador) {
		this.repositorio = repositorio;
		this.repositorioVenda = repositorioVenda;
		this.autorizacao = autorizacao;
		this.codificador = codificador;
	}

	@Transactional(readOnly = true)
	public List<UsuarioSaida> listar() {
		if (autorizacao.perfisGerenciaveis().isEmpty()) {
			return List.of(obterAtual());
		}
		return repositorio.findAll().stream().filter(autorizacao::podeGerenciar).map(UsuarioSaida::de).toList();
	}

	@Transactional(readOnly = true)
	public UsuarioSaida obterAtual() {
		return UsuarioSaida.de(buscar(autorizacao.usuarioAtual().getId()));
	}

	@Transactional(readOnly = true)
	public UsuarioSaida obter(Long id) {
		Usuario usuario = buscar(id);
		autorizacao.exigirLeitura(usuario);
		return UsuarioSaida.de(usuario);
	}

	public UsuarioSaida criar(UsuarioEntrada dados) {
		Set<Perfil> perfis = perfisValidos(dados.perfis());
		autorizacao.exigirAtribuicao(perfis);
		String nome = Validacao.texto(dados.nome(), "nome");
		String nomeUsuario = Validacao.texto(dados.nomeUsuarioEfetivo(), "nomeUsuario");
		String senha = Validacao.texto(dados.senhaEfetiva(), "senha");
		exigirNomeUsuarioDisponivel(nomeUsuario);

		Credencial credencial = new Credencial();
		credencial.setNomeUsuario(nomeUsuario);
		credencial.setSenha(codificador.encode(senha));
		Usuario usuario = new Usuario();
		usuario.setNome(nome);
		usuario.setCredencial(credencial);
		usuario.getPerfis().addAll(perfis);
		return UsuarioSaida.de(repositorio.save(usuario));
	}

	public UsuarioSaida atualizar(Long id, UsuarioEntrada dados) {
		Usuario usuario = buscar(id);
		autorizacao.exigirGerenciamento(usuario);
		if (dados.perfis() != null) {
			Set<Perfil> perfis = perfisValidos(dados.perfis());
			autorizacao.exigirAtribuicao(perfis);
			usuario.getPerfis().clear();
			usuario.getPerfis().addAll(perfis);
		}
		if (dados.nome() != null) {
			usuario.setNome(Validacao.texto(dados.nome(), "nome"));
		}
		String nomeUsuario = dados.nomeUsuarioEfetivo();
		if (nomeUsuario != null) {
			String normalizado = Validacao.texto(nomeUsuario, "nomeUsuario");
			if (!normalizado.equals(usuario.getNomeUsuario())) {
				exigirNomeUsuarioDisponivel(normalizado);
				usuario.getCredencial().setNomeUsuario(normalizado);
			}
		}
		String senha = dados.senhaEfetiva();
		if (senha != null) {
			usuario.getCredencial().setSenha(codificador.encode(Validacao.texto(senha, "senha")));
		}
		return UsuarioSaida.de(repositorio.save(usuario));
	}

	public void excluir(Long id) {
		Usuario usuario = buscar(id);
		autorizacao.exigirGerenciamento(usuario);
		repositorioVenda.deleteAll(repositorioVenda.findByClienteId(id));
		repositorioVenda.deleteAll(repositorioVenda.findByFuncionarioId(id));
		repositorio.delete(usuario);
	}

	private Usuario buscar(Long id) {
		return repositorio.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
	}

	private Set<Perfil> perfisValidos(Set<Perfil> perfis) {
		if (perfis == null || perfis.isEmpty() || perfis.contains(null)) {
			throw new RequisicaoInvalidaException("Informe ao menos um perfil válido");
		}
		return EnumSet.copyOf(perfis);
	}

	private void exigirNomeUsuarioDisponivel(String nomeUsuario) {
		if (repositorio.existsByCredencialNomeUsuario(nomeUsuario)) {
			throw new ConflitoException("Nome de usuário já cadastrado: " + nomeUsuario);
		}
	}
}
