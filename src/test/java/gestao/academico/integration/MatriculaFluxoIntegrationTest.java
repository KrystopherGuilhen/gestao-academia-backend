package gestao.academico.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import gestao.academico.model.dto.*;
import gestao.academico.model.entidades.StatusTurma;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de integracao ponta a ponta: sobe o contexto Spring completo
 * (seguranca, JPA/H2, Flyway) e exercita a API real via MockMvc,
 * incluindo login JWT, para validar o fluxo critico de matricula:
 * criacao (PENDENTE), confirmacao (consome vaga), limite de vagas
 * respeitado e cancelamento (libera vaga).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MatriculaFluxoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginComCredenciaisValidasDeveRetornarToken() throws Exception {
        LoginRequestDTO login = new LoginRequestDTO();
        login.setUsername("admin");
        login.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.token").isNotEmpty());
    }

    @Test
    void loginComCredenciaisInvalidasDeveRetornar401() throws Exception {
        LoginRequestDTO login = new LoginRequestDTO();
        login.setUsername("admin");
        login.setPassword("senha-errada");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acessarRotaProtegidaSemTokenDeveRetornar401ou403() throws Exception {
        mockMvc.perform(get("/api/alunos/todos"))
                .andExpect(status().is(status4xxSemToken()));
    }

    @Test
    void fluxoCompletoDeMatriculaRespeitaLimiteDeVagas() throws Exception {
        String token = obterToken();

        Long alunoId1 = criarAluno(token, "Fluxo Aluno Um", "fluxo.um@email.com", "10101010101");
        Long alunoId2 = criarAluno(token, "Fluxo Aluno Dois", "fluxo.dois@email.com", "20202020202");
        Long cursoId = criarCurso(token, "Curso Fluxo Teste", "FLX-001");
        Long disciplinaId = criarDisciplina(token, "Disciplina Fluxo", "FLX-DISC-001", cursoId);
        // Turma com apenas 1 vaga, para forcar o cenario de limite esgotado
        Long turmaId = criarTurma(token, "FLX-TURMA-001", disciplinaId, 1);

        // 1) matricula o aluno 1 -> PENDENTE, nao consome vaga ainda
        Long matriculaId1 = matricular(token, alunoId1, turmaId);
        mockMvc.perform(get("/api/turmas/" + turmaId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.dados.vagasOcupadas", is(0)));

        // 2) confirma a matricula do aluno 1 -> consome a unica vaga
        mockMvc.perform(put("/api/matriculas/" + matriculaId1 + "/confirmar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.status", is("CONFIRMADA")));

        mockMvc.perform(get("/api/turmas/" + turmaId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.dados.vagasOcupadas", is(1)))
                .andExpect(jsonPath("$.dados.vagasDisponiveis", is(0)));

        // 3) aluno 1 nao pode se matricular de novo na mesma turma
        mockMvc.perform(post("/api/matriculas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMatricula(alunoId1, turmaId))))
                .andExpect(status().isConflict());

        // 4) aluno 2 consegue entrar na fila (PENDENTE), pois so a confirmacao consome vaga
        Long matriculaId2 = matricular(token, alunoId2, turmaId);

        // 5) mas confirmar a matricula do aluno 2 deve falhar: nao ha vagas disponiveis
        mockMvc.perform(put("/api/matriculas/" + matriculaId2 + "/confirmar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("vagas")));

        // 6) cancelar a matricula CONFIRMADA do aluno 1 libera a vaga
        mockMvc.perform(put("/api/matriculas/" + matriculaId1 + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.status", is("CANCELADA")));

        mockMvc.perform(get("/api/turmas/" + turmaId).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.dados.vagasOcupadas", is(0)));

        // 7) agora a matricula do aluno 2, antes rejeitada, pode ser confirmada
        mockMvc.perform(put("/api/matriculas/" + matriculaId2 + "/confirmar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.status", is("CONFIRMADA")));
    }

    @Test
    void naoDevePermitirMatricularEmTurmaFechada() throws Exception {
        String token = obterToken();

        Long alunoId = criarAluno(token, "Aluno Turma Fechada", "turma.fechada@email.com", "30303030303");
        Long cursoId = criarCurso(token, "Curso Fechado Teste", "FEC-001");
        Long disciplinaId = criarDisciplina(token, "Disciplina Fechada", "FEC-DISC-001", cursoId);
        Long turmaId = criarTurma(token, "FEC-TURMA-001", disciplinaId, 10);

        // Fecha a turma
        TurmaDTO atualizacao = new TurmaDTO();
        atualizacao.setCodigo("FEC-TURMA-001");
        atualizacao.setDisciplinaId(disciplinaId);
        atualizacao.setPeriodo("2025.2");
        atualizacao.setVagasTotais(10);
        atualizacao.setDataInicio(LocalDate.now());
        atualizacao.setDataFim(LocalDate.now().plusMonths(4));
        atualizacao.setStatus(StatusTurma.FECHADA);

        mockMvc.perform(put("/api/turmas/" + turmaId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/matriculas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMatricula(alunoId, turmaId))))
                .andExpect(status().isUnprocessableEntity());
    }

    // -------------------- helpers --------------------

    private int status4xxSemToken() {
        // Sem token, o Spring Security devolve 403 (acesso negado) por padrao
        // quando nao ha AuthenticationEntryPoint customizado.
        return 403;
    }

    private String obterToken() throws Exception {
        LoginRequestDTO login = new LoginRequestDTO();
        login.setUsername("admin");
        login.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return JsonPath.read(body, "$.dados.token");
    }

    private Long criarAluno(String token, String nome, String email, String cpf) throws Exception {
        AlunoDTO dto = new AlunoDTO();
        dto.setNome(nome);
        dto.setEmail(email);
        dto.setCpf(cpf);
        dto.setDataNascimento(LocalDate.of(2000, 1, 1));

        MvcResult result = mockMvc.perform(post("/api/alunos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        return extrairId(result);
    }

    private Long criarCurso(String token, String nome, String codigo) throws Exception {
        CursoDTO dto = new CursoDTO();
        dto.setNome(nome);
        dto.setCodigo(codigo);
        dto.setCargaHorariaTotal(2400);

        MvcResult result = mockMvc.perform(post("/api/cursos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        return extrairId(result);
    }

    private Long criarDisciplina(String token, String nome, String codigo, Long cursoId) throws Exception {
        DisciplinaDTO dto = new DisciplinaDTO();
        dto.setNome(nome);
        dto.setCodigo(codigo);
        dto.setCargaHoraria(60);
        dto.setCursoId(cursoId);

        MvcResult result = mockMvc.perform(post("/api/disciplinas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        return extrairId(result);
    }

    private Long criarTurma(String token, String codigo, Long disciplinaId, int vagasTotais) throws Exception {
        TurmaDTO dto = new TurmaDTO();
        dto.setCodigo(codigo);
        dto.setDisciplinaId(disciplinaId);
        dto.setPeriodo("2025.2");
        dto.setVagasTotais(vagasTotais);
        dto.setDataInicio(LocalDate.now());
        dto.setDataFim(LocalDate.now().plusMonths(4));

        MvcResult result = mockMvc.perform(post("/api/turmas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        return extrairId(result);
    }

    private Long matricular(String token, Long alunoId, Long turmaId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/matriculas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMatricula(alunoId, turmaId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.status", is("PENDENTE")))
                .andReturn();

        return extrairId(result);
    }

    private MatriculaRequestDTO requestMatricula(Long alunoId, Long turmaId) {
        MatriculaRequestDTO dto = new MatriculaRequestDTO();
        dto.setAlunoId(alunoId);
        dto.setTurmaId(turmaId);
        return dto;
    }

    private Long extrairId(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.dados.id");
        return id.longValue();
    }
}
