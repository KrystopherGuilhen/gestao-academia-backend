package gestao.academico.service;

import gestao.academico.exception.DuplicateException;
import gestao.academico.exception.RegraNegocioException;
import gestao.academico.exception.ResourceNotFoundException;
import gestao.academico.model.dto.MatriculaRequestDTO;
import gestao.academico.model.dto.MatriculaResponseDTO;
import gestao.academico.model.entidades.*;
import gestao.academico.repository.MatriculaRepository;
import gestao.academico.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Cobre as regras criticas do fluxo de matricula, que sao o principal
 * ponto de avaliacao do desafio:
 *  - aluno so pode se matricular em turma aberta
 *  - nao pode haver matricula duplicada (mesmo aluno + mesma turma)
 *  - confirmar consome vaga e respeita o limite da turma
 *  - cancelar libera a vaga quando a matricula estava confirmada
 */
@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private AlunoService alunoService;

    @Mock
    private TurmaService turmaService;

    @InjectMocks
    private MatriculaService matriculaService;

    private Aluno aluno;
    private Turma turma;
    private Disciplina disciplina;

    @BeforeEach
    void setUp() {
        disciplina = new Disciplina();
        disciplina.setId(1L);
        disciplina.setNome("Estrutura de Dados");

        aluno = new Aluno();
        aluno.setId(10L);
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@teste.com");

        turma = new Turma();
        turma.setId(100L);
        turma.setCodigo("ED-2025-2-A");
        turma.setDisciplina(disciplina);
        turma.setVagasTotais(2);
        turma.setVagasOcupadas(0);
        turma.setStatus(StatusTurma.ABERTA);
        turma.setDataInicio(LocalDate.now());
        turma.setDataFim(LocalDate.now().plusMonths(4));
    }

    @Nested
    @DisplayName("matricular()")
    class Matricular {

        @Test
        @DisplayName("cria matricula PENDENTE quando turma esta aberta e nao ha duplicidade")
        void deveCriarMatriculaPendente() {
            MatriculaRequestDTO request = new MatriculaRequestDTO();
            request.setAlunoId(aluno.getId());
            request.setTurmaId(turma.getId());

            when(alunoService.buscarEntidade(aluno.getId())).thenReturn(aluno);
            when(turmaService.buscarEntidade(turma.getId())).thenReturn(turma);
            when(matriculaRepository.existsByAlunoIdAndTurmaId(aluno.getId(), turma.getId())).thenReturn(false);
            when(matriculaRepository.save(any(Matricula.class))).thenAnswer(invocacao -> {
                Matricula m = invocacao.getArgument(0);
                m.setId(999L);
                return m;
            });

            MatriculaResponseDTO resultado = matriculaService.matricular(request);

            assertThat(resultado.getStatus()).isEqualTo(StatusMatricula.PENDENTE);
            assertThat(resultado.getAlunoId()).isEqualTo(aluno.getId());
            assertThat(resultado.getTurmaId()).isEqualTo(turma.getId());

            // A vaga NAO deve ser consumida na criacao, somente na confirmacao
            verify(turmaRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejeita matricula quando a turma nao esta aberta")
        void deveRejeitarQuandoTurmaFechada() {
            turma.setStatus(StatusTurma.FECHADA);
            MatriculaRequestDTO request = new MatriculaRequestDTO();
            request.setAlunoId(aluno.getId());
            request.setTurmaId(turma.getId());

            when(alunoService.buscarEntidade(aluno.getId())).thenReturn(aluno);
            when(turmaService.buscarEntidade(turma.getId())).thenReturn(turma);

            assertThatThrownBy(() -> matriculaService.matricular(request))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("nao esta aberta");

            verify(matriculaRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejeita matricula duplicada do mesmo aluno na mesma turma")
        void deveRejeitarMatriculaDuplicada() {
            MatriculaRequestDTO request = new MatriculaRequestDTO();
            request.setAlunoId(aluno.getId());
            request.setTurmaId(turma.getId());

            when(alunoService.buscarEntidade(aluno.getId())).thenReturn(aluno);
            when(turmaService.buscarEntidade(turma.getId())).thenReturn(turma);
            when(matriculaRepository.existsByAlunoIdAndTurmaId(aluno.getId(), turma.getId())).thenReturn(true);

            assertThatThrownBy(() -> matriculaService.matricular(request))
                    .isInstanceOf(DuplicateException.class)
                    .hasMessageContaining("ja possui uma matricula");

            verify(matriculaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("confirmar()")
    class Confirmar {

        @Test
        @DisplayName("confirma matricula PENDENTE e consome uma vaga da turma")
        void deveConfirmarEConsumirVaga() {
            Matricula matricula = matriculaPendente();

            when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));
            when(turmaRepository.buscarParaAtualizacaoDeVagas(turma.getId())).thenReturn(Optional.of(turma));
            when(turmaRepository.save(any(Turma.class))).thenAnswer(inv -> inv.getArgument(0));
            when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));

            MatriculaResponseDTO resultado = matriculaService.confirmar(1L);

            assertThat(resultado.getStatus()).isEqualTo(StatusMatricula.CONFIRMADA);
            assertThat(resultado.getDataConfirmacao()).isNotNull();

            ArgumentCaptor<Turma> turmaCaptor = ArgumentCaptor.forClass(Turma.class);
            verify(turmaRepository).save(turmaCaptor.capture());
            assertThat(turmaCaptor.getValue().getVagasOcupadas()).isEqualTo(1);
        }

        @Test
        @DisplayName("nao confirma quando a turma nao tem mais vagas disponiveis")
        void deveRejeitarQuandoVagasEsgotadas() {
            turma.setVagasOcupadas(2); // vagasTotais = 2, ja esta no limite
            Matricula matricula = matriculaPendente();

            when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));
            when(turmaRepository.buscarParaAtualizacaoDeVagas(turma.getId())).thenReturn(Optional.of(turma));

            assertThatThrownBy(() -> matriculaService.confirmar(1L))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("Nao ha vagas disponiveis");

            verify(turmaRepository, never()).save(any());
            verify(matriculaRepository, never()).save(any());
        }

        @Test
        @DisplayName("nao permite confirmar uma matricula que ja foi confirmada ou cancelada")
        void deveRejeitarConfirmacaoForaDoEstadoPendente() {
            Matricula matricula = matriculaPendente();
            matricula.setStatus(StatusMatricula.CANCELADA);

            when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));

            assertThatThrownBy(() -> matriculaService.confirmar(1L))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("Somente matriculas PENDENTE");

            verify(turmaRepository, never()).buscarParaAtualizacaoDeVagas(anyLong());
        }

        @Test
        @DisplayName("lanca ResourceNotFoundException quando a matricula nao existe")
        void deveLancarNotFoundQuandoMatriculaNaoExiste() {
            when(matriculaRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> matriculaService.confirmar(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("cancelar()")
    class Cancelar {

        @Test
        @DisplayName("libera a vaga da turma ao cancelar uma matricula CONFIRMADA")
        void deveLiberarVagaAoCancelarConfirmada() {
            turma.setVagasOcupadas(1);
            Matricula matricula = matriculaPendente();
            matricula.setStatus(StatusMatricula.CONFIRMADA);

            when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));
            when(turmaRepository.buscarParaAtualizacaoDeVagas(turma.getId())).thenReturn(Optional.of(turma));
            when(turmaRepository.save(any(Turma.class))).thenAnswer(inv -> inv.getArgument(0));
            when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));

            MatriculaResponseDTO resultado = matriculaService.cancelar(1L);

            assertThat(resultado.getStatus()).isEqualTo(StatusMatricula.CANCELADA);

            ArgumentCaptor<Turma> turmaCaptor = ArgumentCaptor.forClass(Turma.class);
            verify(turmaRepository).save(turmaCaptor.capture());
            assertThat(turmaCaptor.getValue().getVagasOcupadas()).isEqualTo(0);
        }

        @Test
        @DisplayName("cancelar uma matricula PENDENTE nao altera o contador de vagas")
        void naoDeveAlterarVagaAoCancelarPendente() {
            Matricula matricula = matriculaPendente(); // status PENDENTE

            when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));
            when(matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));

            MatriculaResponseDTO resultado = matriculaService.cancelar(1L);

            assertThat(resultado.getStatus()).isEqualTo(StatusMatricula.CANCELADA);
            verify(turmaRepository, never()).buscarParaAtualizacaoDeVagas(anyLong());
            verify(turmaRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejeita cancelar uma matricula que ja esta cancelada")
        void deveRejeitarCancelamentoDuplicado() {
            Matricula matricula = matriculaPendente();
            matricula.setStatus(StatusMatricula.CANCELADA);

            when(matriculaRepository.findById(1L)).thenReturn(Optional.of(matricula));

            assertThatThrownBy(() -> matriculaService.cancelar(1L))
                    .isInstanceOf(RegraNegocioException.class)
                    .hasMessageContaining("ja esta cancelada");

            verify(matriculaRepository, never()).save(any());
        }
    }

    private Matricula matriculaPendente() {
        Matricula matricula = new Matricula();
        matricula.setId(1L);
        matricula.setAluno(aluno);
        matricula.setTurma(turma);
        matricula.setStatus(StatusMatricula.PENDENTE);
        return matricula;
    }
}
