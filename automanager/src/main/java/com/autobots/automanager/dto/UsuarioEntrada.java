package com.autobots.automanager.dto;

import java.util.Set;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.modelos.Perfil;
import com.fasterxml.jackson.annotation.JsonAlias;

public record UsuarioEntrada(
		String nome,
		String nomeUsuario,
		String senha,
		Credencial credencial,
		@JsonAlias("Perfis") Set<Perfil> perfis) {

	public UsuarioEntrada(String nome, String nomeUsuario, String senha, Set<Perfil> perfis) {
		this(nome, nomeUsuario, senha, null, perfis);
	}

	public String nomeUsuarioEfetivo() {
		if (nomeUsuario != null) {
			return nomeUsuario;
		}
		return credencial != null ? credencial.getNomeUsuario() : null;
	}

	public String senhaEfetiva() {
		if (senha != null) {
			return senha;
		}
		return credencial != null ? credencial.getSenha() : null;
	}
}
