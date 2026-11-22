package com.autobots.automanager.excecoes;

import org.springframework.security.access.AccessDeniedException;

@SuppressWarnings("serial")
public class AcessoNegadoException extends AccessDeniedException {
	public AcessoNegadoException(String mensagem) {
		super(mensagem);
	}
}
