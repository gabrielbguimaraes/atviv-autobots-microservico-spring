# AutoBots — Atualização Segurança (ATVIV)

**Disciplina:** Desenvolvimento Web III  
**Professor:** Prof. Dr. Eng. Gerson Penha  
**Atividade Prática:** ATVIV — Autenticação e Autorização Stateless via JSON Web Token (JWT) e Controle de Acesso Baseado em Perfis e Propriedade (RBAC + Ownership)

---

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 2.7.0** (Spring Web, Spring Security, Spring Data JPA)
- **JJWT (`io.jsonwebtoken:jjwt:0.9.1`)** — Assinatura e validação de tokens JWT (`HS512`)
- **BCrypt (`BCryptPasswordEncoder`)** — Hashing seguro de senhas
- **H2 Database** — Banco de dados em memória com carga inicial automática
- **JUnit 5 + Spring MockMvc** — Testes de integração cobrindo 100% da Matriz de Autorização (Tabela 1)

---

## Como Executar e Testar

Os comandos abaixo devem ser executados dentro do diretório `automanager` (utilizando o Maven Wrapper `./mvnw` / `.\mvnw.cmd` ou o `mvn` instalado na máquina):

### 1. Executar a suíte de testes automatizados
```bash
cd automanager
./mvnw clean test
# No Windows PowerShell:
.\mvnw.cmd clean test
# Ou com Maven global:
mvn clean test
```

### 2. Iniciar a aplicação
```bash
cd automanager
./mvnw spring-boot:run
# No Windows PowerShell:
.\mvnw.cmd spring-boot:run
# Ou com Maven global:
mvn spring-boot:run
```
A API iniciará por padrão em `http://localhost:8080`.

---

## Usuários Pré-Cadastrados (Seed Inicial)

Na inicialização (`CargaInicial`), o banco H2 em memória é populado automaticamente de maneira idempotente com os seguintes usuários, mercadorias, serviços e vendas (`V-0001` de `vendedor1` para `cliente1`; `V-0002` de `vendedor2` para `cliente2`):

| Nome | Username (`nomeUsuario`) | Senha (`senha`) | Perfil (`Perfil`) |
| :--- | :--- | :--- | :--- |
| Administrador | `admin` | `123456` | `ROLE_ADMIN` |
| Gerente | `gerente` | `123456` | `ROLE_GERENTE` |
| Vendedor Um | `vendedor1` | `123456` | `ROLE_VENDEDOR` |
| Vendedor Dois | `vendedor2` | `123456` | `ROLE_VENDEDOR` |
| Cliente Um | `cliente1` | `123456` | `ROLE_CLIENTE` |
| Cliente Dois | `cliente2` | `123456` | `ROLE_CLIENTE` |

---

## Matriz de Endpoints e Autorizações (Tabela 1)

| Recurso / Endpoint | `ROLE_ADMIN` | `ROLE_GERENTE` | `ROLE_VENDEDOR` | `ROLE_CLIENTE` |
| :--- | :--- | :--- | :--- | :--- |
| `POST /login` | Público | Público | Público | Público |
| `GET /usuarios/me` | Próprio cadastro | Próprio cadastro | Próprio cadastro | Próprio cadastro |
| `GET /usuarios` | Todos os usuários | Apenas `GERENTE`, `VENDEDOR`, `CLIENTE` | Apenas `CLIENTE` | Apenas o próprio cadastro |
| `GET /usuarios/{id}` | Qualquer usuário | Próprio ou `GERENTE`, `VENDEDOR`, `CLIENTE` | Próprio ou `CLIENTE` | Apenas o próprio ID (`403` nos demais) |
| `POST /usuarios` | Qualquer perfil (incl. `ADMIN`) | Apenas `GERENTE`, `VENDEDOR`, `CLIENTE` | Apenas `CLIENTE` | `403 Forbidden` |
| `PUT /usuarios/{id}` | Qualquer usuário/perfil | Apenas `GERENTE`, `VENDEDOR`, `CLIENTE` | Apenas `CLIENTE` | `403 Forbidden` |
| `DELETE /usuarios/{id}` | Qualquer usuário (incl. `ADMIN`) | Apenas `GERENTE`, `VENDEDOR`, `CLIENTE` | Apenas `CLIENTE` | `403 Forbidden` |
| `GET /mercadorias` e `GET /mercadorias/{id}` | Permitido (`200`) | Permitido (`200`) | Permitido (`200`) | `403 Forbidden` |
| `POST`, `PUT`, `DELETE /mercadorias` | Permitido | Permitido | `403 Forbidden` | `403 Forbidden` |
| `GET /servicos` e `GET /servicos/{id}` | Permitido (`200`) | Permitido (`200`) | Permitido (`200`) | `403 Forbidden` |
| `POST`, `PUT`, `DELETE /servicos` | Permitido | Permitido | `403 Forbidden` | `403 Forbidden` |
| `GET /vendas` | Todas as vendas | Todas as vendas | Apenas vendas feitas por si | Apenas vendas em que é consumidor |
| `GET /vendas/{id}` | Qualquer venda | Qualquer venda | Apenas venda própria (`403` nas demais) | Apenas compra própria (`403` nas demais) |
| `POST /vendas` | Permitido | Permitido | Apenas vendas feitas por si mesmo | `403 Forbidden` |
| `PUT`, `DELETE /vendas/{id}` | Permitido | Permitido | `403 Forbidden` | `403 Forbidden` |

---

## Exemplos de Requisições (`curl`)

### 1. Login e obtenção do token JWT
```bash
curl -i -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"nomeUsuario":"admin","senha":"123456"}'
```
O token JWT é retornado tanto no cabeçalho `Authorization: Bearer <token>` quanto no corpo JSON (`$.token`).

### 2. Consultar o próprio perfil autenticado
```bash
curl -X GET http://localhost:8080/usuarios/me \
  -H "Authorization: Bearer <SEU_TOKEN_JWT>"
```

### 3. Criar um novo cliente (como `ADMIN`, `GERENTE` ou `VENDEDOR`)
```bash
curl -X POST http://localhost:8080/usuarios \
  -H "Authorization: Bearer <SEU_TOKEN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Carlos Silva",
    "nomeUsuario": "carlos.cliente",
    "senha": "123456",
    "perfis": ["ROLE_CLIENTE"]
  }'
```

### 4. Cadastrar mercadoria (como `ADMIN` ou `GERENTE`)
```bash
curl -X POST http://localhost:8080/mercadorias \
  -H "Authorization: Bearer <SEU_TOKEN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Amortecedor Dianteiro",
    "descricao": "Amortecedor pressurizado",
    "quantidade": 20,
    "valor": 299.90
  }'
```

### 5. Registrar uma venda (como `VENDEDOR`, `GERENTE` ou `ADMIN`)
```bash
curl -X POST http://localhost:8080/vendas \
  -H "Authorization: Bearer <SEU_TOKEN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "identificacao": "V-0003",
    "clienteId": 5,
    "mercadoriaIds": [1],
    "servicoIds": [1]
  }'
```
