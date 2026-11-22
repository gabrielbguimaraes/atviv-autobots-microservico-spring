package com.autobots.automanager.dto;

import java.util.List;

public record TokenSaida(String token, String tipo, String nomeUsuario, List<String> perfis, long expiraEmMs) {
}
