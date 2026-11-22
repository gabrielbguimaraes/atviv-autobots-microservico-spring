package com.autobots.automanager.configuracao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.modelos.Perfil;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVenda;

@Component
public class CargaInicial implements CommandLineRunner {

	private static final String SENHA_PADRAO = "123456";

	private final RepositorioUsuario repositorioUsuario;
	private final RepositorioMercadoria repositorioMercadoria;
	private final RepositorioServico repositorioServico;
	private final RepositorioVenda repositorioVenda;
	private final PasswordEncoder codificador;

	public CargaInicial(RepositorioUsuario repositorioUsuario, RepositorioMercadoria repositorioMercadoria,
			RepositorioServico repositorioServico, RepositorioVenda repositorioVenda, PasswordEncoder codificador) {
		this.repositorioUsuario = repositorioUsuario;
		this.repositorioMercadoria = repositorioMercadoria;
		this.repositorioServico = repositorioServico;
		this.repositorioVenda = repositorioVenda;
		this.codificador = codificador;
	}

	@Override
	@Transactional
	public void run(String... args) {
		garantirUsuario("Administrador", "admin", Perfil.ROLE_ADMIN);
		garantirUsuario("Gerente", "gerente", Perfil.ROLE_GERENTE);
		Usuario vendedor1 = garantirUsuario("Vendedor Um", "vendedor1", Perfil.ROLE_VENDEDOR);
		Usuario vendedor2 = garantirUsuario("Vendedor Dois", "vendedor2", Perfil.ROLE_VENDEDOR);
		Usuario cliente1 = garantirUsuario("Cliente Um", "cliente1", Perfil.ROLE_CLIENTE);
		Usuario cliente2 = garantirUsuario("Cliente Dois", "cliente2", Perfil.ROLE_CLIENTE);

		Mercadoria pneu = garantirMercadoria("Pneu Aro 15", "Pneu radial 195/65 R15", 40L, 389.90);
		Mercadoria oleo = garantirMercadoria("Óleo 5W30", "Óleo sintético 1L", 120L, 49.90);
		garantirMercadoria("Filtro de Ar", "Filtro de ar do motor", 60L, 65.00);

		Servico alinhamento = garantirServico("Alinhamento", "Alinhamento de direção", 120.00);
		Servico trocaOleo = garantirServico("Troca de Óleo", "Troca de óleo e filtro", 80.00);
		garantirServico("Balanceamento", "Balanceamento das quatro rodas", 90.00);

		garantirVenda("V-0001", vendedor1, cliente1, List.of(pneu), List.of(alinhamento));
		garantirVenda("V-0002", vendedor2, cliente2, List.of(oleo), List.of(trocaOleo));
	}

	private Usuario garantirUsuario(String nome, String nomeUsuario, Perfil perfil) {
		return repositorioUsuario.findByCredencialNomeUsuario(nomeUsuario).orElseGet(() -> {
			Credencial credencial = new Credencial();
			credencial.setNomeUsuario(nomeUsuario);
			credencial.setSenha(codificador.encode(SENHA_PADRAO));
			Usuario usuario = new Usuario();
			usuario.setNome(nome);
			usuario.setCredencial(credencial);
			usuario.getPerfis().add(perfil);
			return repositorioUsuario.save(usuario);
		});
	}

	private Mercadoria garantirMercadoria(String nome, String descricao, Long quantidade, Double valor) {
		return repositorioMercadoria.findFirstByNome(nome).orElseGet(() -> {
			Mercadoria mercadoria = new Mercadoria();
			mercadoria.setNome(nome);
			mercadoria.setDescricao(descricao);
			mercadoria.setQuantidade(quantidade);
			mercadoria.setValor(valor);
			return repositorioMercadoria.save(mercadoria);
		});
	}

	private Servico garantirServico(String nome, String descricao, Double valor) {
		return repositorioServico.findFirstByNome(nome).orElseGet(() -> {
			Servico servico = new Servico();
			servico.setNome(nome);
			servico.setDescricao(descricao);
			servico.setValor(valor);
			return repositorioServico.save(servico);
		});
	}

	private void garantirVenda(String identificacao, Usuario funcionario, Usuario cliente,
			List<Mercadoria> mercadorias, List<Servico> servicos) {
		if (repositorioVenda.findFirstByIdentificacao(identificacao).isPresent()) {
			return;
		}
		Venda venda = new Venda();
		venda.setIdentificacao(identificacao);
		venda.setCadastro(LocalDateTime.now());
		venda.setFuncionario(funcionario);
		venda.setCliente(cliente);
		venda.getMercadorias().addAll(mercadorias);
		venda.getServicos().addAll(servicos);
		repositorioVenda.save(venda);
	}
}
