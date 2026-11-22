package com.autobots.automanager.servicos;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioVenda;

@Service
@Transactional
public class GerenciadorServico {

	private final RepositorioServico repositorio;
	private final RepositorioVenda repositorioVenda;

	public GerenciadorServico(RepositorioServico repositorio, RepositorioVenda repositorioVenda) {
		this.repositorio = repositorio;
		this.repositorioVenda = repositorioVenda;
	}

	@Transactional(readOnly = true)
	public List<Servico> listar() {
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Servico obter(Long id) {
		return repositorio.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", id));
	}

	public Servico criar(Servico dados) {
		Servico servico = new Servico();
		aplicar(servico, dados);
		return repositorio.save(servico);
	}

	public Servico atualizar(Long id, Servico dados) {
		Servico servico = obter(id);
		aplicar(servico, dados);
		return repositorio.save(servico);
	}

	public void excluir(Long id) {
		Servico servico = obter(id);
		for (Venda venda : repositorioVenda.findAll()) {
			venda.getServicos().removeIf(item -> id.equals(item.getId()));
		}
		repositorio.delete(servico);
	}

	private void aplicar(Servico destino, Servico dados) {
		destino.setNome(Validacao.texto(dados.getNome(), "nome"));
		destino.setDescricao(dados.getDescricao());
		destino.setValor(Validacao.naoNegativo(dados.getValor(), "valor"));
	}
}
