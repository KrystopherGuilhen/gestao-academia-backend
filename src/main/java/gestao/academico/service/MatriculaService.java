package gestao.academico.service;

import gestao.academico.exception.DuplicateException;
import gestao.academico.exception.RegraNegocioException;
import gestao.academico.exception.ResourceNotFoundException;
import gestao.academico.model.dto.MatriculaRequestDTO;
import gestao.academico.model.dto.MatriculaResponseDTO;
import gestao.academico.model.entidades.Aluno;
import gestao.academico.model.entidades.Matricula;
import gestao.academico.model.entidades.StatusMatricula;
import gestao.academico.model.entidades.StatusTurma;
import gestao.academico.model.entidades.Turma;
import gestao.academico.repository.MatriculaRepository;
import gestao.academico.repository.TurmaRepository;
import gestao.academico.util.PaginaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Concentra toda a regra de negocio do fluxo de matricula. As duas
 * operacoes criticas (confirmar/cancelar) rodam dentro de uma unica
 * transacao que obtem um lock pessimista na turma antes de tocar no
 * contador de vagas - e assim que o limite de vagas e protegido contra
 * confirmacoes concorrentes.
 */
@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final TurmaRepository turmaRepository;
    private final AlunoService alunoService;
    private final TurmaService turmaService;

    @Transactional
    public MatriculaResponseDTO matricular(MatriculaRequestDTO request) {
        Aluno aluno = alunoService.buscarEntidade(request.getAlunoId());
        Turma turma = turmaService.buscarEntidade(request.getTurmaId());

        if (turma.getStatus() != StatusTurma.ABERTA) {
            throw new RegraNegocioException(
                    "A turma " + turma.getCodigo() + " nao esta aberta para novas matriculas (status atual: " + turma.getStatus() + ")"
            );
        }

        if (matriculaRepository.existsByAlunoIdAndTurmaId(aluno.getId(), turma.getId())) {
            throw new DuplicateException(
                    "O aluno " + aluno.getNome() + " ja possui uma matricula na turma " + turma.getCodigo()
            );
        }

        Matricula matricula = new Matricula();
        matricula.setAluno(aluno);
        matricula.setTurma(turma);
        matricula.setStatus(StatusMatricula.PENDENTE);
        matricula.setDataMatricula(LocalDateTime.now());

        return toDTO(matriculaRepository.save(matricula));
    }

    @Transactional
    public MatriculaResponseDTO confirmar(Long matriculaId) {
        Matricula matricula = buscarEntidade(matriculaId);

        if (matricula.getStatus() != StatusMatricula.PENDENTE) {
            throw new RegraNegocioException(
                    "Somente matriculas PENDENTE podem ser confirmadas (status atual: " + matricula.getStatus() + ")"
            );
        }

        // Lock pessimista: bloqueia a linha da turma ate o fim desta transacao,
        // serializando confirmacoes concorrentes na mesma turma.
        Turma turma = turmaRepository.buscarParaAtualizacaoDeVagas(matricula.getTurma().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Turma nao encontrada"));

        if (turma.getVagasOcupadas() >= turma.getVagasTotais()) {
            throw new RegraNegocioException(
                    "Nao ha vagas disponiveis na turma " + turma.getCodigo() + " para confirmar esta matricula"
            );
        }

        turma.setVagasOcupadas(turma.getVagasOcupadas() + 1);
        turmaRepository.save(turma);

        matricula.setStatus(StatusMatricula.CONFIRMADA);
        matricula.setDataConfirmacao(LocalDateTime.now());

        return toDTO(matriculaRepository.save(matricula));
    }

    @Transactional
    public MatriculaResponseDTO cancelar(Long matriculaId) {
        Matricula matricula = buscarEntidade(matriculaId);

        if (matricula.getStatus() == StatusMatricula.CANCELADA) {
            throw new RegraNegocioException("Esta matricula ja esta cancelada");
        }

        boolean liberaVaga = matricula.getStatus() == StatusMatricula.CONFIRMADA;

        if (liberaVaga) {
            Turma turma = turmaRepository.buscarParaAtualizacaoDeVagas(matricula.getTurma().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turma nao encontrada"));
            turma.setVagasOcupadas(Math.max(0, turma.getVagasOcupadas() - 1));
            turmaRepository.save(turma);
        }

        matricula.setStatus(StatusMatricula.CANCELADA);
        matricula.setDataCancelamento(LocalDateTime.now());

        return toDTO(matriculaRepository.save(matricula));
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> consultarPorAluno(Long alunoId) {
        alunoService.buscarEntidade(alunoId);
        return matriculaRepository.findByAlunoId(alunoId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> consultarPorTurma(Long turmaId) {
        turmaService.buscarEntidade(turmaId);
        return matriculaRepository.findByTurmaId(turmaId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public PaginaResponse<MatriculaResponseDTO> consultaPaginada(Pageable pageable, String filtro) {
        return PaginaResponse.from(matriculaRepository.buscarComFiltro(filtro, pageable).map(this::toDTO));
    }

    private Matricula buscarEntidade(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matricula com id " + id + " nao encontrada"));
    }

    private MatriculaResponseDTO toDTO(Matricula matricula) {
        return new MatriculaResponseDTO(
                matricula.getId(),
                matricula.getAluno().getId(),
                matricula.getAluno().getNome(),
                matricula.getTurma().getId(),
                matricula.getTurma().getCodigo(),
                matricula.getTurma().getDisciplina().getNome(),
                matricula.getStatus(),
                matricula.getDataMatricula(),
                matricula.getDataConfirmacao(),
                matricula.getDataCancelamento()
        );
    }
}
