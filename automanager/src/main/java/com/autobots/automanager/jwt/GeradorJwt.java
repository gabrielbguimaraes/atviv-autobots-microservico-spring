package com.autobots.automanager.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

class GeradorJwt {
	private final String assinatura;
	private final long duracao;

	GeradorJwt(String assinatura, long duracao) {
		this.assinatura = assinatura;
		this.duracao = duracao;
	}

	String gerarJwt(String nomeUsuario, Collection<String> perfis) {
		Date agora = new Date();
		return Jwts.builder()
				.setSubject(nomeUsuario)
				.claim(ProvedorJwt.CLAIM_PERFIS, perfis)
				.setIssuedAt(agora)
				.setExpiration(new Date(agora.getTime() + duracao))
				.signWith(SignatureAlgorithm.HS512, assinatura.getBytes(StandardCharsets.UTF_8))
				.compact();
	}
}
