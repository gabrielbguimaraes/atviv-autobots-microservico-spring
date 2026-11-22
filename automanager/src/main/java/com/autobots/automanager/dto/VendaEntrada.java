package com.autobots.automanager.dto;

import java.util.List;
import java.util.Objects;

public record VendaEntrada(
		String identificacao,
		Long clienteId,
		Long funcionarioId,
		List<Long> mercadoriaIds,
		List<Long> servicoIds,
		ReferenciaId cliente,
		ReferenciaId funcionario,
		List<ReferenciaId> mercadorias,
		List<ReferenciaId> servicos) {

	public record ReferenciaId(Long id) {
	}

	public VendaEntrada(String identificacao, Long clienteId, Long funcionarioId, List<Long> mercadoriaIds,
			List<Long> servicoIds) {
		this(identificacao, clienteId, funcionarioId, mercadoriaIds, servicoIds, null, null, null, null);
	}

	public Long clienteIdEfetivo() {
		return clienteId != null ? clienteId : (cliente != null ? cliente.id() : null);
	}

	public Long funcionarioIdEfetivo() {
		return funcionarioId != null ? funcionarioId : (funcionario != null ? funcionario.id() : null);
	}

	public List<Long> mercadoriaIdsEfetivos() {
		if (mercadoriaIds != null) {
			return mercadoriaIds;
		}
		if (mercadorias != null) {
			return mercadorias.stream().filter(Objects::nonNull).map(ReferenciaId::id).toList();
		}
		return null;
	}

	public List<Long> servicoIdsEfetivos() {
		if (servicoIds != null) {
			return servicoIds;
		}
		if (servicos != null) {
			return servicos.stream().filter(Objects::nonNull).map(ReferenciaId::id).toList();
		}
		return null;
	}
}
