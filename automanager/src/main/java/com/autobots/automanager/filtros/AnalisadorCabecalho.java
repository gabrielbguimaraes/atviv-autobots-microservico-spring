package com.autobots.automanager.filtros;

class AnalisadorCabecalho {
	private static final String PREFIXO = "Bearer ";
	private final String cabecalho;

	AnalisadorCabecalho(String cabecalho) {
		this.cabecalho = cabecalho;
	}

	String obterJwt() {
		return cabecalho.substring(PREFIXO.length()).trim();
	}
}
