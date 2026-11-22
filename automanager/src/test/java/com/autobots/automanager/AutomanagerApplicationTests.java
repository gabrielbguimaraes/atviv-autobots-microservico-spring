package com.autobots.automanager;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.autobots.automanager.dto.LoginEntrada;
import com.autobots.automanager.dto.UsuarioEntrada;
import com.autobots.automanager.dto.VendaEntrada;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.modelos.Perfil;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVenda;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AutomanagerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Autowired
	private RepositorioMercadoria repositorioMercadoria;

	@Autowired
	private RepositorioServico repositorioServico;

	@Autowired
	private RepositorioVenda repositorioVenda;

	@Value("${app.easteregg}")
	private String easterEgg;

	private Long adminId;
	private Long gerenteId;
	private Long vendedor1Id;
	private Long vendedor2Id;
	private Long cliente1Id;
	private Long cliente2Id;
	private Long mercadoriaId;
	private Long servicoId;
	private Long venda1Id;
	private Long venda2Id;

	@BeforeEach
	void configurarReferencias() {
		adminId = repositorioUsuario.findByCredencialNomeUsuario("admin").orElseThrow().getId();
		gerenteId = repositorioUsuario.findByCredencialNomeUsuario("gerente").orElseThrow().getId();
		vendedor1Id = repositorioUsuario.findByCredencialNomeUsuario("vendedor1").orElseThrow().getId();
		vendedor2Id = repositorioUsuario.findByCredencialNomeUsuario("vendedor2").orElseThrow().getId();
		cliente1Id = repositorioUsuario.findByCredencialNomeUsuario("cliente1").orElseThrow().getId();
		cliente2Id = repositorioUsuario.findByCredencialNomeUsuario("cliente2").orElseThrow().getId();
		mercadoriaId = repositorioMercadoria.findFirstByNome("Pneu Aro 15").orElseThrow().getId();
		servicoId = repositorioServico.findFirstByNome("Alinhamento").orElseThrow().getId();
		venda1Id = repositorioVenda.findFirstByIdentificacao("V-0001").orElseThrow().getId();
		venda2Id = repositorioVenda.findFirstByIdentificacao("V-0002").orElseThrow().getId();
	}

	private String autenticar(String nomeUsuario, String senha) throws Exception {
		String payload = objectMapper.writeValueAsString(new LoginEntrada(nomeUsuario, senha));
		MvcResult resultado = mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
				.andExpect(jsonPath("$.token", notNullValue()))
				.andExpect(jsonPath("$.nomeUsuario", is(nomeUsuario)))
				.andReturn();
		return resultado.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
	}

	@Test
	void deveAutenticarComSucessoERejeitarCredenciaisOuTokensInvalidos() throws Exception {
		String bearer = autenticar("admin", "123456");

		mockMvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nomeUsuario", is("admin")));

		String loginInvalido = objectMapper.writeValueAsString(new LoginEntrada("admin", "senha-errada"));
		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(loginInvalido))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/usuarios"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/usuarios")
				.header(HttpHeaders.AUTHORIZATION, bearer + "adulterado"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void administradorDevePossuirCrudCompletoEmTodasAsEntidadesInclusiveAdmin() throws Exception {
		String bearer = autenticar("admin", "123456");

		UsuarioEntrada novoAdmin = new UsuarioEntrada("Segundo Admin", "admin2", "123456", Set.of(Perfil.ROLE_ADMIN));
		MvcResult criacaoAdmin = mockMvc.perform(post("/usuarios")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(novoAdmin)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nomeUsuario", is("admin2")))
				.andExpect(jsonPath("$.perfis", hasItem("ROLE_ADMIN")))
				.andReturn();
		Long novoAdminId = extrairId(criacaoAdmin);

		UsuarioEntrada edicaoAdmin = new UsuarioEntrada("Admin Editado", "admin2", "654321", Set.of(Perfil.ROLE_ADMIN));
		mockMvc.perform(put("/usuarios/{id}", novoAdminId)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicaoAdmin)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome", is("Admin Editado")));

		mockMvc.perform(get("/usuarios")
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(7))));

		mockMvc.perform(delete("/usuarios/{id}", novoAdminId)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNoContent());

		Mercadoria novaMercadoria = new Mercadoria();
		novaMercadoria.setNome("Bateria 60Ah");
		novaMercadoria.setDescricao("Bateria automotiva selada");
		novaMercadoria.setQuantidade(15L);
		novaMercadoria.setValor(450.0);
		MvcResult criacaoMercadoria = mockMvc.perform(post("/mercadorias")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(novaMercadoria)))
				.andExpect(status().isCreated())
				.andReturn();
		Long novaMercadoriaId = extrairId(criacaoMercadoria);

		novaMercadoria.setValor(470.0);
		mockMvc.perform(put("/mercadorias/{id}", novaMercadoriaId)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(novaMercadoria)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valor", is(470.0)));

		mockMvc.perform(delete("/mercadorias/{id}", novaMercadoriaId)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNoContent());

		Servico novoServico = new Servico();
		novoServico.setNome("Revisão Completa");
		novoServico.setDescricao("Revisão de 20.000 km");
		novoServico.setValor(350.0);
		MvcResult criacaoServico = mockMvc.perform(post("/servicos")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(novoServico)))
				.andExpect(status().isCreated())
				.andReturn();
		Long novoServicoId = extrairId(criacaoServico);

		novoServico.setValor(380.0);
		mockMvc.perform(put("/servicos/{id}", novoServicoId)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(novoServico)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valor", is(380.0)));

		mockMvc.perform(delete("/servicos/{id}", novoServicoId)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNoContent());

		VendaEntrada novaVenda = new VendaEntrada("V-ADMIN", cliente1Id, vendedor1Id, List.of(mercadoriaId),
				List.of(servicoId));
		MvcResult criacaoVenda = mockMvc.perform(post("/vendas")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(novaVenda)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.identificacao", is("V-ADMIN")))
				.andReturn();
		Long novaVendaId = extrairId(criacaoVenda);

		VendaEntrada edicaoVenda = new VendaEntrada("V-ADMIN-EDITADA", cliente2Id, vendedor2Id, List.of(mercadoriaId),
				List.of(servicoId));
		mockMvc.perform(put("/vendas/{id}", novaVendaId)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(edicaoVenda)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.identificacao", is("V-ADMIN-EDITADA")));

		mockMvc.perform(delete("/vendas/{id}", novaVendaId)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNoContent());
	}

	@Test
	void gerenteDeveGerenciarNaoAdminsMercadoriasServicosEVendasEBloquearOperacoesEmAdmin() throws Exception {
		String bearer = autenticar("gerente", "123456");

		for (Perfil perfilPermitido : List.of(Perfil.ROLE_GERENTE, Perfil.ROLE_VENDEDOR, Perfil.ROLE_CLIENTE)) {
			String username = "novo_" + perfilPermitido.name().toLowerCase();
			UsuarioEntrada entrada = new UsuarioEntrada("Usuário " + username, username, "123456",
					Set.of(perfilPermitido));
			MvcResult criado = mockMvc.perform(post("/usuarios")
					.header(HttpHeaders.AUTHORIZATION, bearer)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(entrada)))
					.andExpect(status().isCreated())
					.andReturn();
			Long idCriado = extrairId(criado);

			UsuarioEntrada atualizado = new UsuarioEntrada("Editado " + username, username, "123456",
					Set.of(perfilPermitido));
			mockMvc.perform(put("/usuarios/{id}", idCriado)
					.header(HttpHeaders.AUTHORIZATION, bearer)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(atualizado)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.nome", is("Editado " + username)));

			mockMvc.perform(delete("/usuarios/{id}", idCriado)
					.header(HttpHeaders.AUTHORIZATION, bearer))
					.andExpect(status().isNoContent());
		}

		UsuarioEntrada tentativaCriarAdmin = new UsuarioEntrada("Hacker Admin", "hacker_admin", "123456",
				Set.of(Perfil.ROLE_ADMIN));
		mockMvc.perform(post("/usuarios")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tentativaCriarAdmin)))
				.andExpect(status().isForbidden());

		UsuarioEntrada tentativaPromoverParaAdmin = new UsuarioEntrada("Promovido", "vendedor1", "123456",
				Set.of(Perfil.ROLE_ADMIN));
		mockMvc.perform(put("/usuarios/{id}", vendedor1Id)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tentativaPromoverParaAdmin)))
				.andExpect(status().isForbidden());

		UsuarioEntrada tentativaEditarAdmin = new UsuarioEntrada("Admin Alterado", "admin", "123456",
				Set.of(Perfil.ROLE_GERENTE));
		mockMvc.perform(put("/usuarios/{id}", adminId)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tentativaEditarAdmin)))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/usuarios/{id}", adminId)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/usuarios/{id}", adminId)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/usuarios")
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].nomeUsuario", not(hasItem("admin"))));

		Mercadoria mercadoria = new Mercadoria();
		mercadoria.setNome("Pastilha de Freio");
		mercadoria.setDescricao("Jogo dianteiro");
		mercadoria.setQuantidade(30L);
		mercadoria.setValor(140.0);
		MvcResult mercadoriaCriada = mockMvc.perform(post("/mercadorias")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mercadoria)))
				.andExpect(status().isCreated())
				.andReturn();
		Long idMercadoria = extrairId(mercadoriaCriada);

		mercadoria.setValor(155.0);
		mockMvc.perform(put("/mercadorias/{id}", idMercadoria)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mercadoria)))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/mercadorias/{id}", idMercadoria)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNoContent());

		Servico servico = new Servico();
		servico.setNome("Higienização A/C");
		servico.setDescricao("Limpeza do ar-condicionado");
		servico.setValor(110.0);
		MvcResult servicoCriado = mockMvc.perform(post("/servicos")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(servico)))
				.andExpect(status().isCreated())
				.andReturn();
		Long idServico = extrairId(servicoCriado);

		servico.setValor(125.0);
		mockMvc.perform(put("/servicos/{id}", idServico)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(servico)))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/servicos/{id}", idServico)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNoContent());

		VendaEntrada venda = new VendaEntrada("V-GERENTE", cliente1Id, vendedor1Id, List.of(mercadoriaId),
				List.of(servicoId));
		MvcResult vendaCriada = mockMvc.perform(post("/vendas")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(venda)))
				.andExpect(status().isCreated())
				.andReturn();
		Long idVenda = extrairId(vendaCriada);

		VendaEntrada vendaAtualizada = new VendaEntrada("V-GERENTE-2", cliente2Id, vendedor2Id, List.of(mercadoriaId),
				List.of(servicoId));
		mockMvc.perform(put("/vendas/{id}", idVenda)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(vendaAtualizada)))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/vendas/{id}", idVenda)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNoContent());
	}

	@Test
	void vendedorDeveGerenciarApenasClientesLerCatalogoECriarOuLerApenasPropriasVendas() throws Exception {
		String bearer = autenticar("vendedor1", "123456");

		UsuarioEntrada novoCliente = new UsuarioEntrada("Cliente Novo", "cliente_novo", "123456",
				Set.of(Perfil.ROLE_CLIENTE));
		MvcResult clienteCriado = mockMvc.perform(post("/usuarios")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(novoCliente)))
				.andExpect(status().isCreated())
				.andReturn();
		Long novoClienteId = extrairId(clienteCriado);

		UsuarioEntrada clienteEditado = new UsuarioEntrada("Cliente Editado", "cliente_novo", "123456",
				Set.of(Perfil.ROLE_CLIENTE));
		mockMvc.perform(put("/usuarios/{id}", novoClienteId)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(clienteEditado)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome", is("Cliente Editado")));

		mockMvc.perform(delete("/usuarios/{id}", novoClienteId)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNoContent());

		for (Perfil perfilProibido : List.of(Perfil.ROLE_ADMIN, Perfil.ROLE_GERENTE, Perfil.ROLE_VENDEDOR)) {
			UsuarioEntrada tentativa = new UsuarioEntrada("Proibido", "proibido_" + perfilProibido.name(), "123456",
					Set.of(perfilProibido));
			mockMvc.perform(post("/usuarios")
					.header(HttpHeaders.AUTHORIZATION, bearer)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(tentativa)))
					.andExpect(status().isForbidden());
		}

		for (Long alvoProibido : List.of(adminId, gerenteId, vendedor2Id)) {
			mockMvc.perform(get("/usuarios/{id}", alvoProibido)
					.header(HttpHeaders.AUTHORIZATION, bearer))
					.andExpect(status().isForbidden());

			mockMvc.perform(put("/usuarios/{id}", alvoProibido)
					.header(HttpHeaders.AUTHORIZATION, bearer)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(novoCliente)))
					.andExpect(status().isForbidden());

			mockMvc.perform(delete("/usuarios/{id}", alvoProibido)
					.header(HttpHeaders.AUTHORIZATION, bearer))
					.andExpect(status().isForbidden());
		}

		mockMvc.perform(get("/usuarios")
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].perfis[*]", everyItem(is("ROLE_CLIENTE"))));

		mockMvc.perform(get("/mercadorias").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk());
		mockMvc.perform(get("/mercadorias/{id}", mercadoriaId).header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk());

		Mercadoria mercadoria = new Mercadoria();
		mercadoria.setNome("Item Proibido");
		mercadoria.setQuantidade(1L);
		mercadoria.setValor(10.0);
		mockMvc.perform(post("/mercadorias")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mercadoria)))
				.andExpect(status().isForbidden());
		mockMvc.perform(put("/mercadorias/{id}", mercadoriaId)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mercadoria)))
				.andExpect(status().isForbidden());
		mockMvc.perform(delete("/mercadorias/{id}", mercadoriaId)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/servicos").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk());
		mockMvc.perform(get("/servicos/{id}", servicoId).header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk());

		Servico servico = new Servico();
		servico.setNome("Serviço Proibido");
		servico.setValor(10.0);
		mockMvc.perform(post("/servicos")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(servico)))
				.andExpect(status().isForbidden());
		mockMvc.perform(put("/servicos/{id}", servicoId)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(servico)))
				.andExpect(status().isForbidden());
		mockMvc.perform(delete("/servicos/{id}", servicoId)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());

		VendaEntrada vendaPropria = new VendaEntrada("V-VEND1-NOVA", cliente1Id, null, List.of(mercadoriaId),
				List.of(servicoId));
		mockMvc.perform(post("/vendas")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(vendaPropria)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.funcionario.id", is(vendedor1Id.intValue())));

		VendaEntrada vendaEmNomeDeOutro = new VendaEntrada("V-FRAUDE", cliente1Id, vendedor2Id, List.of(mercadoriaId),
				List.of(servicoId));
		mockMvc.perform(post("/vendas")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(vendaEmNomeDeOutro)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/vendas").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].funcionario.id", everyItem(is(vendedor1Id.intValue()))));

		mockMvc.perform(get("/vendas/{id}", venda1Id).header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk());

		mockMvc.perform(get("/vendas/{id}", venda2Id).header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());

		mockMvc.perform(put("/vendas/{id}", venda1Id)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(vendaPropria)))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/vendas/{id}", venda1Id)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());
	}

	@Test
	void clienteDeveLerApenasProprioCadastroEPropriasComprasComDemaisOperacoesBloqueadas() throws Exception {
		String bearer = autenticar("cliente1", "123456");

		mockMvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(cliente1Id.intValue())))
				.andExpect(jsonPath("$.nomeUsuario", is("cliente1")));

		mockMvc.perform(get("/usuarios/{id}", cliente1Id).header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(cliente1Id.intValue())));

		mockMvc.perform(get("/usuarios").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(cliente1Id.intValue())));

		mockMvc.perform(get("/usuarios/{id}", cliente2Id).header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());

		UsuarioEntrada tentativaUsuario = new UsuarioEntrada("Outro", "outro", "123456", Set.of(Perfil.ROLE_CLIENTE));
		mockMvc.perform(post("/usuarios")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tentativaUsuario)))
				.andExpect(status().isForbidden());

		mockMvc.perform(put("/usuarios/{id}", cliente1Id)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tentativaUsuario)))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/usuarios/{id}", cliente1Id)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/vendas").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].cliente.id", is(cliente1Id.intValue())));

		mockMvc.perform(get("/vendas/{id}", venda1Id).header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(venda1Id.intValue())));

		mockMvc.perform(get("/vendas/{id}", venda2Id).header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());

		VendaEntrada tentativaVenda = new VendaEntrada("V-CLI", cliente1Id, vendedor1Id, List.of(mercadoriaId),
				List.of(servicoId));
		mockMvc.perform(post("/vendas")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tentativaVenda)))
				.andExpect(status().isForbidden());

		mockMvc.perform(put("/vendas/{id}", venda1Id)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tentativaVenda)))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/vendas/{id}", venda1Id)
				.header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/mercadorias").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/servicos").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isForbidden());
	}

	@Test
	void deveRetornar404ParaRecursosInexistentes400ParaPayloadsInvalidosEConterEasterEgg() throws Exception {
		assertEquals("p_P = c^5 / (hG^2)", easterEgg);

		String bearer = autenticar("admin", "123456");

		mockMvc.perform(get("/usuarios/99999").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/mercadorias/99999").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/servicos/99999").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/vendas/99999").header(HttpHeaders.AUTHORIZATION, bearer))
				.andExpect(status().isNotFound());

		UsuarioEntrada usuarioSemSenha = new UsuarioEntrada("Invalido", "invalido", "", Set.of(Perfil.ROLE_CLIENTE));
		mockMvc.perform(post("/usuarios")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(usuarioSemSenha)))
				.andExpect(status().isBadRequest());

		Mercadoria mercadoriaNegativa = new Mercadoria();
		mercadoriaNegativa.setNome("Peça");
		mercadoriaNegativa.setQuantidade(1L);
		mercadoriaNegativa.setValor(-10.0);
		mockMvc.perform(post("/mercadorias")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mercadoriaNegativa)))
				.andExpect(status().isBadRequest());

		VendaEntrada vendaVazia = new VendaEntrada("V-VAZIA", cliente1Id, vendedor1Id, List.of(), List.of());
		mockMvc.perform(post("/vendas")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(vendaVazia)))
				.andExpect(status().isBadRequest());
	}

	private Long extrairId(MvcResult resultado) throws Exception {
		JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
		return json.get("id").asLong();
	}
}
