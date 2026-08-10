package gestao.academico.service;

import gestao.academico.exception.RegraNegocioException;
import gestao.academico.model.entidades.Turma;
import gestao.academico.repository.MatriculaRepository;
import gestao.academico.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Cobre a regra: uma turma nao pode ser excluida se tiver qualquer aluno
 * matriculado nela, mesmo com a matricula CANCELADA.
 */
@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private DisciplinaService disciplinaService;

    @Mock
    private MatriculaRepository matriculaRepository;

    @InjectMocks
    private TurmaService turmaService;

    private Turma turma;

    @BeforeEach
    void setUp() {
        turma = new Turma();
        turma.setId(1L);
        turma.setCodigo("ED-2025-2-A");
    }

    @Test
    void deveRejeitarExclusaoQuandoTurmaPossuiQualquerAlunoMatriculado() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(matriculaRepository.existsByTurmaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> turmaService.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Esta turma não pode ser excluída pois possui alunos matriculados nela");

        verify(turmaRepository, never()).deleteById(anyLong());
    }

    @Test
    void devePermitirExclusaoQuandoTurmaNaoPossuiNenhumAlunoMatriculado() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(matriculaRepository.existsByTurmaId(1L)).thenReturn(false);

        turmaService.excluir(1L);

        verify(turmaRepository).deleteById(1L);
    }
}
