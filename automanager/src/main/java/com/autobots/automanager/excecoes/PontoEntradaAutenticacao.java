package com.autobots.automanager.excecoes;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class PontoEntradaAutenticacao implements AuthenticationEntryPoint {
	private final EscritorErro escritorErro;

	public PontoEntradaAutenticacao(EscritorErro escritorErro) {
		this.escritorErro = escritorErro;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException excecao) throws IOException {
		escritorErro.escrever(request, response, HttpStatus.UNAUTHORIZED,
				"Token JWT ausente, inválido ou expirado");
	}
}
