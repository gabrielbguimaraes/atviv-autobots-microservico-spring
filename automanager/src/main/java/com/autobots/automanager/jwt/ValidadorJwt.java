package com.autobots.automanager.jwt;

import java.util.Date;

import io.jsonwebtoken.Claims;

class ValidadorJwt {
	boolean validar(Claims reivindicacoes) {
		if (reivindicacoes == null) {
			return false;
		}
		Date expiracao = reivindicacoes.getExpiration();
		return reivindicacoes.getSubject() != null && expiracao != null && new Date().before(expiracao);
	}
}
