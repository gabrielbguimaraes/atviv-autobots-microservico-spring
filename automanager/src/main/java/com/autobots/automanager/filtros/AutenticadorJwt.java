package com.autobots.automanager.filtros;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.autobots.automanager.jwt.ProvedorJwt;

class AutenticadorJwt {
	private final String jwt;
	private final ProvedorJwt provedorJwt;
	private final UserDetailsService servico;

	AutenticadorJwt(String jwt, ProvedorJwt provedorJwt, UserDetailsService servico) {
		this.jwt = jwt;
		this.provedorJwt = provedorJwt;
		this.servico = servico;
	}

	UsernamePasswordAuthenticationToken obterAutenticacao() {
		if (!provedorJwt.validarJwt(jwt)) {
			return null;
		}
		try {
			UserDetails usuario = servico.loadUserByUsername(provedorJwt.obterNomeUsuario(jwt));
			return new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
		} catch (UsernameNotFoundException e) {
			return null;
		}
	}
}
