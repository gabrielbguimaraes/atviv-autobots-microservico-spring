package com.autobots.automanager.servicos;

import com.autobots.automanager.excecoes.RequisicaoInvalidaException;

final class Validacao {

	private Validacao() {
	}

	static String texto(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new RequisicaoInvalidaException("O campo '" + campo + "' é obrigatório");
		}
		return valor.trim();
	}

	static <T extends Number> T naoNegativo(T valor, String campo) {
		if (valor == null || valor.doubleValue() < 0) {
			throw new RequisicaoInvalidaException("O campo '" + campo + "' é obrigatório e não pode ser negativo");
		}
		return valor;
	}
}
