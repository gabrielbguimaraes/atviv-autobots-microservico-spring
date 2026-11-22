package com.autobots.automanager.excecoes;

@SuppressWarnings("serial")
public class RecursoNaoEncontradoException extends RuntimeException {
	public RecursoNaoEncontradoException(String recurso, Long id) {
		super(String.format("%s com id %d não encontrado(a)", recurso, id));
	}
}
