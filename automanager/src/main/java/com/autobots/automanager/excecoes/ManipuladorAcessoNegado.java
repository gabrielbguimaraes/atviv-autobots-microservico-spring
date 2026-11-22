package com.autobots.automanager.excecoes;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ManipuladorAcessoNegado implements AccessDeniedHandler {
	private final EscritorErro escritorErro;

	public ManipuladorAcessoNegado(EscritorErro escritorErro) {
		this.escritorErro = escritorErro;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException excecao)
			throws IOException {
		escritorErro.escrever(request, response, HttpStatus.FORBIDDEN, "Acesso negado para o perfil autenticado");
	}
}
