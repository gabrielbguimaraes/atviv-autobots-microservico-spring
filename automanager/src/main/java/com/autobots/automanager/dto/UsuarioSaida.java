package com.autobots.automanager.dto;

import java.util.EnumSet;
import java.util.Set;

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelos.Perfil;

public record UsuarioSaida(Long id, String nome, String nomeUsuario, Set<Perfil> perfis) {
	public static UsuarioSaida de(Usuario usuario) {
		Set<Perfil> perfis = usuario.getPerfis().isEmpty() ? EnumSet.noneOf(Perfil.class)
				: EnumSet.copyOf(usuario.getPerfis());
		return new UsuarioSaida(usuario.getId(), usuario.getNome(), usuario.getNomeUsuario(), perfis);
	}
}
