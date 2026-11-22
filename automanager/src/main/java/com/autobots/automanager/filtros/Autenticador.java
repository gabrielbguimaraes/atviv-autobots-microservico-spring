package com.autobots.automanager.filtros;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.autobots.automanager.dto.LoginEntrada;
import com.autobots.automanager.dto.TokenSaida;
import com.autobots.automanager.excecoes.EscritorErro;
import com.autobots.automanager.jwt.ProvedorJwt;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Autenticador extends UsernamePasswordAuthenticationFilter {

	private final ProvedorJwt provedorJwt;
	private final ObjectMapper mapeador;
	private final EscritorErro escritorErro;

	public Autenticador(AuthenticationManager gerenciadorAutenticacao, ProvedorJwt provedorJwt, ObjectMapper mapeador,
			EscritorErro escritorErro) {
		super(gerenciadorAutenticacao);
		this.provedorJwt = provedorJwt;
		this.mapeador = mapeador;
		this.escritorErro = escritorErro;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		LoginEntrada login = lerCredenciais(request);
		return getAuthenticationManager()
				.authenticate(new UsernamePasswordAuthenticationToken(login.nomeUsuario(), login.senha()));
	}

	private LoginEntrada lerCredenciais(HttpServletRequest request) {
		try {
			LoginEntrada login = mapeador.readValue(request.getInputStream(), LoginEntrada.class);
			return login == null ? LoginEntrada.VAZIO : login.normalizado();
		} catch (IOException e) {
			return LoginEntrada.VAZIO;
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication autenticacao) throws IOException {
		UserDetails usuario = (UserDetails) autenticacao.getPrincipal();
		List<String> perfis = usuario.getAuthorities().stream().map(GrantedAuthority::getAuthority).sorted().toList();
		String jwt = provedorJwt.proverJwt(usuario.getUsername(), perfis);
		response.setStatus(HttpStatus.OK.value());
		response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		mapeador.writeValue(response.getOutputStream(),
				new TokenSaida(jwt, "Bearer", usuario.getUsername(), perfis, provedorJwt.getDuracao()));
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException falha) throws IOException {
		SecurityContextHolder.clearContext();
		escritorErro.escrever(request, response, HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos");
	}
}
