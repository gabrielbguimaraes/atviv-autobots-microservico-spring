package com.autobots.automanager.dto;

import com.autobots.automanager.entidades.Usuario;

public record ResumoUsuario(Long id, String nome, String nomeUsuario) {
	public static ResumoUsuario de(Usuario usuario) {
		return new ResumoUsuario(usuario.getId(), usuario.getNome(), usuario.getNomeUsuario());
	}
}
