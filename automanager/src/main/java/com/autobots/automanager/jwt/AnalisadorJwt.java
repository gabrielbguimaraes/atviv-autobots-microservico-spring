package com.autobots.automanager.jwt;

import java.nio.charset.StandardCharsets;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

class AnalisadorJwt {
	private final String assinatura;
	private final String jwt;

	AnalisadorJwt(String assinatura, String jwt) {
		this.assinatura = assinatura;
		this.jwt = jwt;
	}

	Claims obterReivindicacoes() {
		try {
			return Jwts.parser().setSigningKey(assinatura.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(jwt)
					.getBody();
		} catch (JwtException | IllegalArgumentException e) {
			return null;
		}
	}

	String obterNomeUsuario(Claims reivindicacoes) {
		return reivindicacoes == null ? null : reivindicacoes.getSubject();
	}
}
