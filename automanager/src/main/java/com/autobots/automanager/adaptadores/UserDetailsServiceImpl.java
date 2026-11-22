package com.autobots.automanager.adaptadores;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.repositorios.RepositorioUsuario;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final RepositorioUsuario repositorio;

	public UserDetailsServiceImpl(RepositorioUsuario repositorio) {
		this.repositorio = repositorio;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return repositorio.findByCredencialNomeUsuario(username).map(UserDetailsImpl::new)
				.orElseThrow(() -> new UsernameNotFoundException(username));
	}
}
