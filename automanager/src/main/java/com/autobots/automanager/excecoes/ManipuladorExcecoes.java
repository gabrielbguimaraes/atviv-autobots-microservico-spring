package com.autobots.automanager.excecoes;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.autobots.automanager.dto.ErroResposta;

@RestControllerAdvice
public class ManipuladorExcecoes {

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest request) {
		return resposta(HttpStatus.NOT_FOUND, e.getMessage(), request);
	}

	@ExceptionHandler(RequisicaoInvalidaException.class)
	public ResponseEntity<ErroResposta> invalida(RequisicaoInvalidaException e, HttpServletRequest request) {
		return resposta(HttpStatus.BAD_REQUEST, e.getMessage(), request);
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ErroResposta> malformada(Exception e, HttpServletRequest request) {
		return resposta(HttpStatus.BAD_REQUEST, "Requisição malformada ou com tipos inválidos", request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErroResposta> acessoNegado(AccessDeniedException e, HttpServletRequest request) {
		String mensagem = e instanceof AcessoNegadoException ? e.getMessage() : "Acesso negado para o perfil autenticado";
		return resposta(HttpStatus.FORBIDDEN, mensagem, request);
	}

	@ExceptionHandler(ConflitoException.class)
	public ResponseEntity<ErroResposta> conflito(ConflitoException e, HttpServletRequest request) {
		return resposta(HttpStatus.CONFLICT, e.getMessage(), request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException e, HttpServletRequest request) {
		return resposta(HttpStatus.CONFLICT, "Operação viola a integridade dos dados", request);
	}

	private ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem, HttpServletRequest request) {
		return ResponseEntity.status(status).body(ErroResposta.de(status, mensagem, request.getRequestURI()));
	}
}
