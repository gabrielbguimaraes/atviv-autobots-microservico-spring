package com.autobots.automanager.servicos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dto.VendaEntrada;
import com.autobots.automanager.dto.VendaSaida;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.excecoes.RequisicaoInvalidaException;
import com.autobots.automanager.modelos.Perfil;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVenda;

@Service
@Transactional
public class GerenciadorVenda {

	private final RepositorioVenda repositorio;
	private final RepositorioUsuario repositorioUsuario;
	private final RepositorioMercadoria repositorioMercadoria;
	private final RepositorioServico repositorioServico;
	private final AutorizacaoServico autorizacao;

	public GerenciadorVenda(RepositorioVenda repositorio, RepositorioUsuario repositorioUsuario,
			RepositorioMercadoria repositorioMercadoria, RepositorioServico repositorioServico,
			AutorizacaoServico autorizacao) {
		this.repositorio = repositorio;
		this.repositorioUsuario = repositorioUsuario;
		this.repositorioMercadoria = repositorioMercadoria;
		this.repositorioServico = repositorioServico;
		this.autorizacao = autorizacao;
	}

	@Transactional(readOnly = true)
	public List<VendaSaida> listar() {
		if (autorizacao.acessoTotalVendas()) {
			return repositorio.findAll().stream().map(VendaSaida::de).toList();
		}
		Long atual = autorizacao.usuarioAtual().getId();
		Set<Venda> vendas = new LinkedHashSet<>();
		if (autorizacao.possui(Perfil.ROLE_VENDEDOR)) {
			vendas.addAll(repositorio.findByFuncionarioId(atual));
		}
		if (autorizacao.possui(Perfil.ROLE_CLIENTE)) {
			vendas.addAll(repositorio.findByClienteId(atual));
		}
		return vendas.stream().map(VendaSaida::de).toList();
	}

	@Transactional(readOnly = true)
	public VendaSaida obter(Long id) {
		Venda venda = buscar(id);
		autorizacao.exigirLeitura(venda);
		return VendaSaida.de(venda);
	}

	public VendaSaida criar(VendaEntrada dados) {
		Long funcionarioId = autorizacao.resolverFuncionarioDaVenda(dados.funcionarioIdEfetivo());
		Venda venda = new Venda();
		venda.setCadastro(LocalDateTime.now());
		aplicar(venda, dados, funcionarioId);
		return VendaSaida.de(repositorio.save(venda));
	}

	public VendaSaida atualizar(Long id, VendaEntrada dados) {
		Venda venda = buscar(id);
		aplicar(venda, dados, dados.funcionarioIdEfetivo());
		return VendaSaida.de(repositorio.save(venda));
	}

	public void excluir(Long id) {
		repositorio.delete(buscar(id));
	}

	private Venda buscar(Long id) {
		return repositorio.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Venda", id));
	}

	private void aplicar(Venda venda, VendaEntrada dados, Long funcionarioId) {
		Long clienteId = dados.clienteIdEfetivo();
		if (clienteId != null) {
			venda.setCliente(cliente(clienteId));
		}
		if (funcionarioId != null) {
			venda.setFuncionario(funcionario(funcionarioId));
		}
		List<Long> mercadoriaIds = dados.mercadoriaIdsEfetivos();
		if (mercadoriaIds != null) {
			venda.setMercadorias(new ArrayList<>(buscarTodos(repositorioMercadoria, mercadoriaIds, "mercadoria")));
		}
		List<Long> servicoIds = dados.servicoIdsEfetivos();
		if (servicoIds != null) {
			venda.setServicos(new ArrayList<>(buscarTodos(repositorioServico, servicoIds, "serviço")));
		}
		if (dados.identificacao() != null) {
			venda.setIdentificacao(Validacao.texto(dados.identificacao(), "identificacao"));
		}
		if (venda.getCliente() == null) {
			throw new RequisicaoInvalidaException("O campo 'clienteId' é obrigatório");
		}
		if (venda.getMercadorias().isEmpty() && venda.getServicos().isEmpty()) {
			throw new RequisicaoInvalidaException("A venda deve conter ao menos uma mercadoria ou serviço");
		}
		if (venda.getIdentificacao() == null) {
			venda.setIdentificacao("VENDA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		}
	}

	private Usuario cliente(Long id) {
		Usuario usuario = repositorioUsuario.findById(id)
				.orElseThrow(() -> new RequisicaoInvalidaException("Cliente informado não existe: " + id));
		if (!usuario.possui(Perfil.ROLE_CLIENTE)) {
			throw new RequisicaoInvalidaException("O usuário informado não possui perfil de cliente");
		}
		return usuario;
	}

	private Usuario funcionario(Long id) {
		Usuario usuario = repositorioUsuario.findById(id)
				.orElseThrow(() -> new RequisicaoInvalidaException("Funcionário informado não existe: " + id));
		if (!autorizacao.ehFuncionario(usuario)) {
			throw new RequisicaoInvalidaException("O usuário informado não possui perfil de funcionário");
		}
		return usuario;
	}

	private <T> List<T> buscarTodos(JpaRepository<T, Long> repositorio, List<Long> ids, String recurso) {
		List<Long> distintos = ids.stream().distinct().toList();
		if (distintos.contains(null)) {
			throw new RequisicaoInvalidaException("Identificador de " + recurso + " inválido");
		}
		List<T> encontrados = repositorio.findAllById(distintos);
		if (encontrados.size() != distintos.size()) {
			throw new RequisicaoInvalidaException("Há " + recurso + "(s) inexistente(s) na venda");
		}
		return encontrados;
	}
}
