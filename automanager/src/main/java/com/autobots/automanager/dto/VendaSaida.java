package com.autobots.automanager.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Venda;

public record VendaSaida(Long id, String identificacao, LocalDateTime cadastro, ResumoUsuario cliente,
		ResumoUsuario funcionario, List<Mercadoria> mercadorias, List<Servico> servicos, double valorTotal) {

	public static VendaSaida de(Venda venda) {
		return new VendaSaida(venda.getId(), venda.getIdentificacao(), venda.getCadastro(),
				ResumoUsuario.de(venda.getCliente()), ResumoUsuario.de(venda.getFuncionario()),
				List.copyOf(venda.getMercadorias()), List.copyOf(venda.getServicos()), venda.valorTotal());
	}
}
