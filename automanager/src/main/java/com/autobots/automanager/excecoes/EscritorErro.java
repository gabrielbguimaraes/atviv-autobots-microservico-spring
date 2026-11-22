package com.autobots.automanager.excecoes;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.autobots.automanager.dto.ErroResposta;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EscritorErro {
	private final ObjectMapper mapeador;

	public EscritorErro(ObjectMapper mapeador) {
		this.mapeador = mapeador;
	}

	public void escrever(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
			String mensagem) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		mapeador.writeValue(response.getOutputStream(), ErroResposta.de(status, mensagem, request.getRequestURI()));
	}
}
