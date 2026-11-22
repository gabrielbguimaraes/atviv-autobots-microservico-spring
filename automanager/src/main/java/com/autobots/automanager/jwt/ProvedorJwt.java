package com.autobots.automanager.jwt;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProvedorJwt {
	public static final String CLAIM_PERFIS = "perfis";

	private final String assinatura;
	private final long duracao;

	public ProvedorJwt(@Value("${jwt.secret}") String assinatura, @Value("${jwt.expiration}") long duracao) {
		this.assinatura = assinatura;
		this.duracao = duracao;
	}

	public String proverJwt(String nomeUsuario, Collection<String> perfis) {
		return new GeradorJwt(assinatura, duracao).gerarJwt(nomeUsuario, perfis);
	}

	public boolean validarJwt(String jwt) {
		return new ValidadorJwt().validar(new AnalisadorJwt(assinatura, jwt).obterReivindicacoes());
	}

	public String obterNomeUsuario(String jwt) {
		AnalisadorJwt analisador = new AnalisadorJwt(assinatura, jwt);
		return analisador.obterNomeUsuario(analisador.obterReivindicacoes());
	}

	public long getDuracao() {
		return duracao;
	}
}
