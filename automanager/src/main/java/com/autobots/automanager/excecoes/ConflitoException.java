package com.autobots.automanager.excecoes;

@SuppressWarnings("serial")
public class ConflitoException extends RuntimeException {
	public ConflitoException(String mensagem) {
		super(mensagem);
	}
}
