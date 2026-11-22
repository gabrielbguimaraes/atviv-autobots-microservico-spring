package com.autobots.automanager.entidades;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.autobots.automanager.modelos.Perfil;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Usuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nome;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
	private Credencial credencial;

	@ElementCollection(fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	private Set<Perfil> perfis = new HashSet<>();

	public boolean possui(Perfil perfil) {
		return perfis.contains(perfil);
	}

	public String getNomeUsuario() {
		return credencial == null ? null : credencial.getNomeUsuario();
	}
}
