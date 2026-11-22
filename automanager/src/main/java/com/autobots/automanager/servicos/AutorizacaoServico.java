package com.autobots.automanager.servicos;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.autobots.automanager.adaptadores.UserDetailsImpl;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.excecoes.AcessoNegadoException;
import com.autobots.automanager.modelos.Perfil;

@Service
public class AutorizacaoServico {

	private static final Map<Perfil, Set<Perfil>> PERFIS_GERENCIAVEIS = Map.of(
			Perfil.ROLE_ADMIN, EnumSet.allOf(Perfil.class),
			Perfil.ROLE_GERENTE, EnumSet.of(Perfil.ROLE_GERENTE, Perfil.ROLE_VENDEDOR, Perfil.ROLE_CLIENTE),
			Perfil.ROLE_VENDEDOR, EnumSet.of(Perfil.ROLE_CLIENTE),
			Perfil.ROLE_CLIENTE, EnumSet.noneOf(Perfil.class));

	private static final Set<Perfil> PERFIS_FUNCIONARIO = EnumSet.of(Perfil.ROLE_ADMIN, Perfil.ROLE_GERENTE,
			Perfil.ROLE_VENDEDOR);

	public Usuario usuarioAtual() {
		Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacao == null || !(autenticacao.getPrincipal() instanceof UserDetailsImpl detalhes)) {
			throw new AcessoNegadoException("Usuário não autenticado");
		}
		return detalhes.getUsuario();
	}

	public boolean possui(Perfil perfil) {
		return usuarioAtual().possui(perfil);
	}

	public boolean ehFuncionario(Usuario usuario) {
		return usuario.getPerfis().stream().anyMatch(PERFIS_FUNCIONARIO::contains);
	}

	public Set<Perfil> perfisGerenciaveis() {
		Set<Perfil> gerenciaveis = EnumSet.noneOf(Perfil.class);
		usuarioAtual().getPerfis().forEach(perfil -> gerenciaveis.addAll(PERFIS_GERENCIAVEIS.get(perfil)));
		return gerenciaveis;
	}

	public boolean podeGerenciar(Usuario alvo) {
		Set<Perfil> gerenciaveis = perfisGerenciaveis();
		return !gerenciaveis.isEmpty() && gerenciaveis.containsAll(alvo.getPerfis());
	}

	public boolean ehProprio(Usuario alvo) {
		return alvo.getId() != null && alvo.getId().equals(usuarioAtual().getId());
	}

	public void exigirGerenciamento(Usuario alvo) {
		if (!podeGerenciar(alvo)) {
			throw new AcessoNegadoException("Seu perfil não pode gerenciar este usuário");
		}
	}

	public void exigirAtribuicao(Set<Perfil> perfis) {
		Set<Perfil> gerenciaveis = perfisGerenciaveis();
		if (gerenciaveis.isEmpty() || !gerenciaveis.containsAll(perfis)) {
			throw new AcessoNegadoException("Seu perfil não pode atribuir os perfis " + perfis);
		}
	}

	public void exigirLeitura(Usuario alvo) {
		if (!ehProprio(alvo) && !podeGerenciar(alvo)) {
			throw new AcessoNegadoException("Seu perfil não pode visualizar este usuário");
		}
	}

	public boolean acessoTotalVendas() {
		return possui(Perfil.ROLE_ADMIN) || possui(Perfil.ROLE_GERENTE);
	}

	public void exigirLeitura(Venda venda) {
		if (acessoTotalVendas()) {
			return;
		}
		Long atual = usuarioAtual().getId();
		boolean vendaPropria = possui(Perfil.ROLE_VENDEDOR) && venda.getFuncionario().getId().equals(atual);
		boolean compraPropria = possui(Perfil.ROLE_CLIENTE) && venda.getCliente().getId().equals(atual);
		if (!vendaPropria && !compraPropria) {
			throw new AcessoNegadoException("Seu perfil não pode visualizar esta venda");
		}
	}

	public Long resolverFuncionarioDaVenda(Long funcionarioInformado) {
		Long atual = usuarioAtual().getId();
		if (acessoTotalVendas()) {
			return funcionarioInformado != null ? funcionarioInformado : atual;
		}
		if (possui(Perfil.ROLE_VENDEDOR)) {
			if (funcionarioInformado != null && !funcionarioInformado.equals(atual)) {
				throw new AcessoNegadoException("O vendedor só pode registrar vendas em seu próprio nome");
			}
			return atual;
		}
		throw new AcessoNegadoException("Seu perfil não pode registrar vendas");
	}
}
