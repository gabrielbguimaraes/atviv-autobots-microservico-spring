package com.autobots.automanager.servicos;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.excecoes.RecursoNaoEncontradoException;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioVenda;

@Service
@Transactional
public class GerenciadorMercadoria {

	private final RepositorioMercadoria repositorio;
	private final RepositorioVenda repositorioVenda;

	public GerenciadorMercadoria(RepositorioMercadoria repositorio, RepositorioVenda repositorioVenda) {
		this.repositorio = repositorio;
		this.repositorioVenda = repositorioVenda;
	}

	@Transactional(readOnly = true)
	public List<Mercadoria> listar() {
		return repositorio.findAll();
	}

	@Transactional(readOnly = true)
	public Mercadoria obter(Long id) {
		return repositorio.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Mercadoria", id));
	}

	public Mercadoria criar(Mercadoria dados) {
		Mercadoria mercadoria = new Mercadoria();
		aplicar(mercadoria, dados);
		return repositorio.save(mercadoria);
	}

	public Mercadoria atualizar(Long id, Mercadoria dados) {
		Mercadoria mercadoria = obter(id);
		aplicar(mercadoria, dados);
		return repositorio.save(mercadoria);
	}

	public void excluir(Long id) {
		Mercadoria mercadoria = obter(id);
		for (Venda venda : repositorioVenda.findAll()) {
			venda.getMercadorias().removeIf(item -> id.equals(item.getId()));
		}
		repositorio.delete(mercadoria);
	}

	private void aplicar(Mercadoria destino, Mercadoria dados) {
		destino.setNome(Validacao.texto(dados.getNome(), "nome"));
		destino.setDescricao(dados.getDescricao());
		destino.setQuantidade(
				dados.getQuantidade() == null ? 0L : Validacao.naoNegativo(dados.getQuantidade(), "quantidade"));
		destino.setValor(Validacao.naoNegativo(dados.getValor(), "valor"));
	}
}
