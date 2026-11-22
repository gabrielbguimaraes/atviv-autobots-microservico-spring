package com.autobots.automanager.configuracao;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.autobots.automanager.adaptadores.UserDetailsServiceImpl;
import com.autobots.automanager.excecoes.EscritorErro;
import com.autobots.automanager.excecoes.ManipuladorAcessoNegado;
import com.autobots.automanager.excecoes.PontoEntradaAutenticacao;
import com.autobots.automanager.filtros.Autenticador;
import com.autobots.automanager.filtros.Autorizador;
import com.autobots.automanager.jwt.ProvedorJwt;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class Seguranca {

	private static final String[] ROTAS_PUBLICAS = { "/error" };

	private final UserDetailsServiceImpl servico;
	private final ProvedorJwt provedorJwt;
	private final ObjectMapper mapeador;
	private final EscritorErro escritorErro;
	private final PontoEntradaAutenticacao pontoEntrada;
	private final ManipuladorAcessoNegado manipuladorAcessoNegado;

	public Seguranca(UserDetailsServiceImpl servico, ProvedorJwt provedorJwt, ObjectMapper mapeador,
			EscritorErro escritorErro, PontoEntradaAutenticacao pontoEntrada,
			ManipuladorAcessoNegado manipuladorAcessoNegado) {
		this.servico = servico;
		this.provedorJwt = provedorJwt;
		this.mapeador = mapeador;
		this.escritorErro = escritorErro;
		this.pontoEntrada = pontoEntrada;
		this.manipuladorAcessoNegado = manipuladorAcessoNegado;
	}

	@Bean
	public PasswordEncoder codificadorSenha() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain cadeiaFiltros(HttpSecurity http, AuthenticationConfiguration configuracao)
			throws Exception {
		AuthenticationManager gerenciador = configuracao.getAuthenticationManager();
		http.cors().and().csrf().disable()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.exceptionHandling()
				.authenticationEntryPoint(pontoEntrada)
				.accessDeniedHandler(manipuladorAcessoNegado).and()
				.authorizeHttpRequests(rotas -> rotas
						.antMatchers(HttpMethod.POST, "/login").permitAll()
						.antMatchers(ROTAS_PUBLICAS).permitAll()
						.anyRequest().authenticated())
				.addFilter(new Autenticador(gerenciador, provedorJwt, mapeador, escritorErro))
				.addFilter(new Autorizador(gerenciador, provedorJwt, servico));
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuracao = new CorsConfiguration().applyPermitDefaultValues();
		configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuracao.setExposedHeaders(List.of(HttpHeaders.AUTHORIZATION));
		UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
		fonte.registerCorsConfiguration("/**", configuracao);
		return fonte;
	}
}
