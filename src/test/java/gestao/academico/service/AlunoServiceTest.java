package gestao.academico.service;

import gestao.academico.exception.RegraNegocioException;
import gestao.academico.model.entidades.Aluno;
import gestao.academico.repository.AlunoRepository;
import gestao.academico.repository.MatriculaRepository;
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
 * Cobre a regra: um aluno nao pode ser excluido se tiver qualquer matricula
 * vinculada, mesmo que ela esteja CANCELADA — nesse caso, so a edicao do
 * cadastro e permitida.
 */
@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @InjectMocks
    private AlunoService alunoService;

    private Aluno aluno;

    @BeforeEach
    void setUp() {
        aluno = new Aluno();
        aluno.setId(1L);
        aluno.setNome("Aluno Teste");
    }

    @Test
    void deveRejeitarExclusaoQuandoAlunoPossuiQualquerMatricula() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.existsByAlunoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> alunoService.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Este aluno não pode ser excluído pois está relacionado a uma turma");

        verify(alunoRepository, never()).deleteById(anyLong());
    }

    @Test
    void devePermitirExclusaoQuandoAlunoNaoPossuiNenhumaMatricula() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(matriculaRepository.existsByAlunoId(1L)).thenReturn(false);

        alunoService.excluir(1L);

        verify(alunoRepository).deleteById(1L);
    }
}
