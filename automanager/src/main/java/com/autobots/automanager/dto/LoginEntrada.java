package com.autobots.automanager.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LoginEntrada(
		@JsonAlias("username") String nomeUsuario,
		@JsonAlias("password") String senha) {
	public static final LoginEntrada VAZIO = new LoginEntrada("", "");

	public LoginEntrada normalizado() {
		return new LoginEntrada(nomeUsuario == null ? "" : nomeUsuario, senha == null ? "" : senha);
	}
}
