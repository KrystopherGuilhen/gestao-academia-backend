# Gestão Acadêmica — Backend

API REST para o sistema de gestão de matrículas acadêmicas: cadastro de alunos, cursos, disciplinas e
turmas, e controle do fluxo de matrícula (vagas, confirmação e cancelamento).

**Autor:** Krystopher Francisco Guilhen de Matos
**Contato:** krystopher.guilhen@outlook.com

> Este repositório contém **apenas o backend**. O frontend (Angular) vive em um repositório separado:
> `gestao-academia-frontend`. Veja a seção
> [Uso em conjunto com o frontend](#uso-em-conjunto-com-o-frontend) para rodar os dois juntos.

---

## Sumário

- [Stack tecnológica](#stack-tecnológica)
- [Pré-requisitos e instalação das tecnologias](#pré-requisitos-e-instalação-das-tecnologias)
- [Como executar](#como-executar)
- [Uso em conjunto com o frontend](#uso-em-conjunto-com-o-frontend)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Testes automatizados](#testes-automatizados)
- [Documentação da API (Swagger)](#documentação-da-api-swagger)
- [Decisões técnicas](#decisões-técnicas)
- [Limitações conhecidas](#limitações-conhecidas)
- [Uso de IA](#uso-de-ia)

---

## Stack tecnológica

| Item | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.4 (Web, Data JPA, Security, Validation) |
| Banco de dados | MySQL 8 |
| Migrations | Flyway |
| Autenticação | JWT (biblioteca `jjwt`) |
| Documentação | springdoc-openapi (Swagger / OpenAPI 3) |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc, H2 (perfil de teste) |
| Build | Maven (via Maven Wrapper, `mvnw`) |
| Container | Docker |

---

## Pré-requisitos e instalação das tecnologias

Você não precisa instalar tudo — escolha **uma** das duas rotas abaixo.

### Rota rápida: só Docker

A forma mais simples de rodar este backend é via Docker, que já cuida do Java, do Maven e do MySQL
dentro de containers. Você só precisa instalar:

- **Docker** e **Docker Compose**
  - Windows/Mac: instale o Docker Desktop (https://www.docker.com/products/docker-desktop/), que já
    inclui o Docker Compose.
  - Linux: siga o guia oficial de instalação do Docker Engine
    (https://docs.docker.com/engine/install/) para sua distribuição, e depois instale o plugin
    `docker-compose-plugin` (geralmente já vem junto).
  - Para confirmar que instalou certo, rode no terminal:
    ```bash
    docker --version
    docker compose version
    ```

Se for este o seu caso, pule direto para [Como executar → Com Docker](#com-docker-recomendado).

### Rota completa: rodando sem Docker (desenvolvimento local)

Se você quer rodar o backend diretamente na sua máquina (sem container), para poder depurar/editar o
código com mais facilidade, precisa instalar:

1. **JDK 21** (Java Development Kit)
   - Baixe o Eclipse Temurin 21 (https://adoptium.net/temurin/releases/?version=21) (recomendado,
     gratuito) ou use um gerenciador de versões como o SDKMAN! (https://sdkman.io/):
     ```bash
     sdk install java 21.0.10-tem
     ```
   - Confirme a instalação:
     ```bash
     java -version
     ```
     Deve mostrar algo como `openjdk version "21..."`.

2. **Maven** — na verdade **não precisa instalar**! Este projeto já inclui o *Maven Wrapper*
   (`mvnw` / `mvnw.cmd`), que baixa e usa a versão correta do Maven automaticamente na primeira
   execução. Basta ter o JDK instalado e usar `./mvnw` (Linux/Mac) ou `mvnw.cmd` (Windows) em vez de
   `mvn` diretamente.

3. **MySQL 8** — para não precisar instalar o MySQL na máquina, o jeito mais simples é subir só o
   banco via Docker (mesmo que você não use Docker para o restante):
   ```bash
   docker compose up db
   ```
   Isso sobe um MySQL na porta `3306` já configurado. Alternativamente, instale o MySQL Community
   Server (https://dev.mysql.com/downloads/mysql/) manualmente e crie um banco chamado
   `gestao-academico`.

---

## Como executar

### Com Docker (recomendado)

```bash
git clone <url-deste-repositorio> gestao-academia-backend
cd gestao-academia-backend
cp .env.example .env
docker compose up --build
```

Isso sobe dois containers:

- **db**: MySQL 8 na porta `3306`, com um volume persistente (`mysql_data`).
- **backend**: a API Spring Boot na porta `8080` (ou a porta que você definir em `BACKEND_PORT` no
  `.env`). Ao iniciar, o Flyway aplica automaticamente as migrations (schema + dados de exemplo).

Depois de subir:

- API: **http://localhost:8080/api**
- Documentação (Swagger com tema claro/escuro): **http://localhost:8080/docs**
- Swagger padrão (alternativa): **http://localhost:8080/swagger-ui.html**

Para derrubar tudo (mantendo os dados do banco): `docker compose down`
Para derrubar e limpar o volume do banco: `docker compose down -v`

### Sem Docker (desenvolvimento local)

Com o MySQL acessível (local ou via `docker compose up db`):

```bash
# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Ajuste usuário/senha do banco em `src/main/resources/application-local.properties` se necessário
(por padrão espera `root` / `root123` em `localhost:3306/gestao-academico`, que é exatamente o que o
`docker compose up db` já configura).

A API sobe em `http://localhost:8080`.

### Dados de exemplo e login

A migration `V2__seed_data.sql` já cria um usuário para autenticação, além de alguns cursos,
disciplinas, turmas e alunos de exemplo (uma das turmas tem só 2 vagas de propósito, para facilitar
testar manualmente a regra de limite de vagas):

```
usuário: admin
senha:   admin123
```

---

## Uso em conjunto com o frontend

Este backend foi desenhado para funcionar tanto sozinho (você testa via Swagger/Postman) quanto
junto com o frontend Angular (`gestao-academia-frontend`), que é um repositório e uma imagem Docker
**separados**. Os dois se conectam de três formas possíveis:

### Opção A — Ambos via Docker, na mesma rede (recomendado)

1. Crie a rede Docker compartilhada (só precisa fazer isso uma vez):
   ```bash
   docker network create gestao-academia-network
   ```
2. Suba este backend:
   ```bash
   cp .env.example .env
   docker compose up --build -d
   ```
3. Vá até o repositório do frontend e suba ele também (ele vai se conectar a essa mesma rede e fazer
   proxy de `/api/*` para o serviço `backend` deste repositório — veja o README do frontend):
   ```bash
   cd ../gestao-academia-frontend
   docker compose up --build
   ```
4. Acesse `http://localhost:4200` — o frontend vai falar com este backend transparentemente, sem
   problema de CORS, porque o Nginx do frontend faz o proxy internamente.

### Opção B — Backend via Docker, frontend via `ng serve` (desenvolvimento)

1. Suba só este backend: `docker compose up --build`
2. No repositório do frontend, rode `npm run start:proxy` (usa o `proxy.conf.js` para redirecionar
   `/api/*` para `http://localhost:8080`).

### Opção C — Backend rodando local (sem Docker)

Se você rodou o backend com `./mvnw spring-boot:run` (rota "sem Docker" acima), ele já está em
`http://localhost:8080`, então tanto a Opção A (ajustando `CORS_ALLOWED_ORIGINS`) quanto a Opção B
funcionam normalmente, sem mudanças.

---

## Variáveis de ambiente

Veja o arquivo `.env.example` — todas as variáveis usadas pelo `docker-compose.yml` estão
documentadas ali com comentários. Resumo:

| Variável | Para que serve |
|---|---|
| `MYSQL_DATABASE`, `MYSQL_ROOT_PASSWORD`, `MYSQL_USER`, `MYSQL_PASSWORD` | Credenciais do container MySQL |
| `SPRING_PROFILES_ACTIVE` | Perfil do Spring (`docker` ou `local`) |
| `JWT_SECRET`, `JWT_EXPIRATION` | Chave e validade do token JWT |
| `CORS_ALLOWED_ORIGINS`, `CORS_ALLOWED_METHODS`, `CORS_ALLOWED_HEADERS` | Configuração de CORS |
| `BACKEND_PORT` | Porta publicada no host para acessar a API |

---

## Testes automatizados

```bash
./mvnw test
```

Os testes usam um perfil dedicado (`test`) com banco **H2 em memória** — não é necessário ter MySQL
ou Docker rodando para executar a suíte de testes.

O que está coberto:

- **`MatriculaServiceTest`** (unitário, com Mockito): cobre isoladamente as regras críticas de
  matrícula — turma fechada, matrícula duplicada, confirmação consumindo vaga, rejeição por vagas
  esgotadas, confirmação fora do estado `PENDENTE`, cancelamento liberando vaga, cancelamento
  duplicado.
- **`MatriculaFluxoIntegrationTest`** (integração, com `@SpringBootTest` + `MockMvc`): sobe o
  contexto Spring completo (incluindo segurança) e exercita a API real via HTTP, com login JWT de
  verdade, simulando o cenário completo: cria turma com 1 vaga → matricula dois alunos (ambos
  `PENDENTE`) → confirma o primeiro (consome a vaga) → confirma o segundo (rejeitado, sem vagas) →
  cancela o primeiro (libera a vaga) → confirma o segundo (agora funciona). Também cobre login
  inválido, acesso sem token e matrícula em turma fechada.
- **`GestaoAcademicoApplicationTests`**: smoke test de contexto (garante que toda a configuração e
  as migrations sobem sem erro).

---

## Documentação da API (Swagger)

Com o backend rodando, duas opções:

- **`http://localhost:8080/docs`** — página customizada com Swagger UI e um botão de alternância
  entre tema claro e escuro (🌙/☀️ no topo).
- **`http://localhost:8080/swagger-ui.html`** — Swagger UI padrão do springdoc (só tema claro).

Todos os endpoints exigem um token JWT (obtido em `POST /api/auth/login`), exceto o próprio login,
os caminhos de documentação e `/actuator/health`. Use o botão **Authorize** e informe `Bearer <token>`.

---

## Decisões técnicas

### Arquitetura em camadas

`controller → service → repository`, com DTOs próprios (nunca expondo entidades JPA diretamente na
API) e um envelope de resposta único (`ApiResponse<T>`) para todas as rotas.

### Como a regra de limite de vagas foi protegida

Este é o ponto mais crítico do domínio:

- `Turma` guarda um contador `vagasOcupadas`, que **nunca é enviado pelo cliente** — é somente
  calculado e atualizado pelo `MatriculaService`.
- Uma matrícula nasce com status `PENDENTE` e **não consome vaga**. A vaga só é consumida na
  **confirmação** (`PUT /api/matriculas/{id}/confirmar`), e liberada no **cancelamento** de uma
  matrícula que estivesse `CONFIRMADA`.
- Para proteger contra duas confirmações concorrentes na mesma turma, o `TurmaRepository` expõe uma
  consulta com **lock pessimista** (`@Lock(PESSIMISTIC_WRITE)`, equivalente a `SELECT ... FOR UPDATE`),
  usada exclusivamente dentro da transação de confirmar/cancelar. Isso serializa o acesso à linha da
  turma, evitando overbooking.
- Esse fluxo é exercitado ponta a ponta no teste de integração (turma com 1 vaga, dois alunos
  disputando a confirmação).

### Regras de negócio implementadas

- Aluno só pode se matricular em turma com status `ABERTA`.
- Turma tem limite de vagas (`vagasTotais`); vagas disponíveis = `vagasTotais - vagasOcupadas`.
- Um aluno não pode ter duas matrículas na mesma turma — protegido tanto na regra de negócio quanto
  por uma constraint `UNIQUE (aluno_id, turma_id)` no banco.
- Matrícula tem três estados: `PENDENTE`, `CONFIRMADA`, `CANCELADA`.
- Confirmar consome vaga; cancelar uma matrícula `CONFIRMADA` libera a vaga.
- Consulta de matrículas por aluno (`GET /api/matriculas/aluno/{id}`) e por turma
  (`GET /api/matriculas/turma/{id}`), além de uma listagem geral paginada e buscável.

### Migrations com Flyway

Duas migrations: `V1__create_schema.sql` (schema) e `V2__seed_data.sql` (dados de exemplo). O SQL foi
escrito de forma portável (sem `ENGINE=`, sem crases) para rodar tanto no MySQL 8 quanto no H2 em
modo de compatibilidade MySQL (perfil de testes), sem precisar de dois conjuntos de scripts.

### Tratamento de erros

Um `@RestControllerAdvice` centralizado (`ApiExceptionHandler`) converte cada exceção de domínio num
código HTTP e numa mensagem clara, sempre no mesmo envelope de resposta:

| Exceção | HTTP | Situação |
|---|---|---|
| `ResourceNotFoundException` | 404 | Registro não encontrado |
| `DuplicateException` | 409 | E-mail/CPF/código duplicado, ou matrícula duplicada |
| `RegraNegocioException` | 422 | Turma fechada, vagas esgotadas, matrícula já cancelada, etc. |
| `MethodArgumentNotValidException` | 400 | Falha de validação de campo (Bean Validation) |
| `BadCredentialsException` | 401 | Login inválido |

### Segurança

Autenticação stateless via JWT (Spring Security + `jjwt`), deliberadamente **sem** sistema de perfis
granulares por entidade — o desafio de origem deste projeto não pedia autenticação, então optei por
um login simples (usuário/senha com hash BCrypt) só para não deixar a API totalmente aberta.

### Documentação Swagger com tema claro/escuro

O springdoc-openapi 2.5.0 (compatível com Spring Boot 3.3.4, usado neste projeto) não tem tema
escuro nativo na sua UI padrão (`/swagger-ui.html`). Cheguei a tentar simplesmente atualizar para o
springdoc-openapi 3.0.3 (que embute uma versão do Swagger UI com alternância nativa de tema) — mas
essa versão do springdoc é construída para **Spring Boot 4** (major version nova, com Jackson 3.x e
pacotes internos reorganizados), e misturar as duas quebra a aplicação em runtime
(`NoClassDefFoundError`). Migrar o projeto inteiro para Spring Boot 4 seria uma mudança grande demais
para o escopo deste desafio, então a solução ficou sendo criar uma página HTML customizada (`/docs`,
servida como recurso estático em `static/docs/index.html`) que carrega o Swagger UI via CDN
(`swagger-ui-dist`) apontando para `/v3/api-docs` (o JSON da spec, que o springdoc já gera
automaticamente), com um botão que alterna um filtro CSS de inversão de cores — uma técnica leve e
comum para "escurecer" widgets de terceiros sem reescrever cada seletor manualmente. A preferência de
tema fica salva no `localStorage` do navegador.

---

## Limitações conhecidas

- **Não atualize o springdoc-openapi para a versão 3.x neste projeto**: essa versão é construída para
  Spring Boot 4 (major version nova) e quebra em runtime (`NoClassDefFoundError`) quando misturada com
  o Spring Boot 3.3.4 usado aqui. A versão 2.5.0 é a compatível e testada.
- **Concorrência**: o lock pessimista protege a confirmação/cancelamento contra a condição de corrida
  no contador de vagas, mas assume que o banco suporta `SELECT ... FOR UPDATE` (MySQL e H2 suportam).
- **CPF/e-mail duplicados**: a validação é feita a nível de aplicação mais uma constraint `UNIQUE`
  no banco como defesa em profundidade.
- **Sem edição de campos após confirmação**: não há endpoint para alterar aluno/turma de uma
  matrícula já criada — o fluxo esperado é cancelar e criar uma nova.
- **Sem CI configurado** neste momento.
- **Perfil de autenticação simplificado**: existe apenas um usuário administrativo fixo criado via
  seed; não há endpoint de cadastro de novos usuários.
- **Artefato Maven ainda chamado `academico`**: por herança do nome original do projeto, o
  `artifactId` no `pom.xml` e o pacote Java (`gestao.academico`) ainda usam "academico", enquanto o
  repositório e a imagem Docker já usam "academia". Não afeta o funcionamento — é só uma
  inconsistência estética que pode ser renomeada depois via refactor do pacote, se desejado.

---

## Uso de IA

Este projeto foi desenvolvido com apoio intensivo do **Claude (Anthropic)**, usado como par de
desenvolvimento durante praticamente todo o processo. Registro aqui com transparência onde e como:

- **Leitura e análise de um projeto de referência**: o Claude leu o backend e o frontend de um
  projeto anterior meu (sistema de gestão de treinamentos) para identificar padrões arquiteturais
  reaproveitáveis (camadas, DTOs, envelope de resposta) e o que precisava ficar de fora por não se
  aplicar ao domínio ou por ser complexidade desnecessária.
- **Backend**: toda a estrutura de entidades, DTOs, repositories, services, controllers, segurança
  JWT, exception handling, migrations Flyway e testes foi escrita com apoio do Claude.
- **Revisão manual**: o ambiente de desenvolvimento usado pela IA não tinha acesso ao Maven Central
  para compilar e rodar os testes automaticamente durante a criação. Todo o código foi escrito com
  atenção manual a nomes de métodos/imports corretos das bibliotecas usadas e revisado item a item
  (busca por referências quebradas, verificação de imports). Ainda assim, **recomenda-se rodar
  `./mvnw test` logo após clonar** para pegar qualquer detalhe de sintaxe que a revisão manual não
  tenha capturado.
- **Trechos mais críticos** para revisão adicional: o lock pessimista em
  `TurmaRepository`/`MatriculaService` (coração da regra de vagas), e a página customizada de Swagger
  com toggle de tema (`static/docs/index.html`), que depende de CDN externa (`unpkg.com`) para
  carregar o Swagger UI — se o ambiente de execução não tiver acesso à internet, use o
  `/swagger-ui.html` padrão como alternativa.

Estou à disposição para explicar qualquer decisão, trecho de código específico, ou os trade-offs de
cada simplificação listada acima.
