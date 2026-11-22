package com.autobots.automanager.excecoes;

@SuppressWarnings("serial")
public class RequisicaoInvalidaException extends RuntimeException {
	public RequisicaoInvalidaException(String mensagem) {
		super(mensagem);
	}
}
