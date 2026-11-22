package com.autobots.automanager.entidades;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Venda {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String identificacao;

	@Column(nullable = false)
	private LocalDateTime cadastro;

	@ManyToOne(optional = false)
	private Usuario cliente;

	@ManyToOne(optional = false)
	private Usuario funcionario;

	@ManyToMany
	@JoinTable(name = "venda_mercadoria")
	private List<Mercadoria> mercadorias = new ArrayList<>();

	@ManyToMany
	@JoinTable(name = "venda_servico")
	private List<Servico> servicos = new ArrayList<>();

	public double valorTotal() {
		double totalMercadorias = mercadorias.stream().mapToDouble(Mercadoria::getValor).sum();
		double totalServicos = servicos.stream().mapToDouble(Servico::getValor).sum();
		return totalMercadorias + totalServicos;
	}
}
