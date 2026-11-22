package com.autobots.automanager.dto;

import java.time.Instant;

import org.springframework.http.HttpStatus;

public record ErroResposta(String timestamp, int status, String erro, String mensagem, String caminho) {
	public static ErroResposta de(HttpStatus status, String mensagem, String caminho) {
		return new ErroResposta(Instant.now().toString(), status.value(), status.getReasonPhrase(), mensagem, caminho);
	}
}
