package gestao.academico.service;

import gestao.academico.exception.DuplicateException;
import gestao.academico.exception.RegraNegocioException;
import gestao.academico.exception.ResourceNotFoundException;
import gestao.academico.model.dto.TurmaDTO;
import gestao.academico.model.entidades.Disciplina;
import gestao.academico.model.entidades.StatusTurma;
import gestao.academico.model.entidades.Turma;
import gestao.academico.repository.MatriculaRepository;
import gestao.academico.repository.TurmaRepository;
import gestao.academico.util.PaginaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final DisciplinaService disciplinaService;
    private final MatriculaRepository matriculaRepository;

    @Transactional(readOnly = true)
    public PaginaResponse<TurmaDTO> consultaPaginada(Pageable pageable, String filtro) {
        return PaginaResponse.from(turmaRepository.buscarComFiltro(filtro, pageable).map(this::toDTO));
    }

    @Transactional(readOnly = true)
    public List<TurmaDTO> listarTodos() {
        return turmaRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public TurmaDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Turma buscarEntidade(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turma com id " + id + " nao encontrada"));
    }

    @Transactional
    public TurmaDTO criar(TurmaDTO dto) {
        if (turmaRepository.existsByCodigo(dto.getCodigo())) {
            throw new DuplicateException("Ja existe uma turma cadastrada com o codigo " + dto.getCodigo());
        }
        validarDatas(dto);

        Disciplina disciplina = disciplinaService.buscarEntidade(dto.getDisciplinaId());

        Turma turma = new Turma();
        turma.setCodigo(dto.getCodigo());
        turma.setDisciplina(disciplina);
        turma.setPeriodo(dto.getPeriodo());
        turma.setVagasTotais(dto.getVagasTotais());
        turma.setVagasOcupadas(0);
        turma.setDataInicio(dto.getDataInicio());
        turma.setDataFim(dto.getDataFim());
        turma.setStatus(dto.getStatus() == null ? StatusTurma.ABERTA : dto.getStatus());

        return toDTO(turmaRepository.save(turma));
    }

    @Transactional
    public TurmaDTO atualizar(Long id, TurmaDTO dto) {
        Turma turma = buscarEntidade(id);

        if (!turma.getCodigo().equals(dto.getCodigo()) && turmaRepository.existsByCodigo(dto.getCodigo())) {
            throw new DuplicateException("Ja existe uma turma cadastrada com o codigo " + dto.getCodigo());
        }
        validarDatas(dto);

        // O total de vagas nunca pode ser reduzido para um valor menor que as
        // vagas ja ocupadas, senao o contador de ocupacao ficaria inconsistente.
        if (dto.getVagasTotais() < turma.getVagasOcupadas()) {
            throw new RegraNegocioException(
                    "O total de vagas (" + dto.getVagasTotais() + ") nao pode ser menor que as vagas ja ocupadas (" + turma.getVagasOcupadas() + ")"
            );
        }

        Disciplina disciplina = disciplinaService.buscarEntidade(dto.getDisciplinaId());

        turma.setCodigo(dto.getCodigo());
        turma.setDisciplina(disciplina);
        turma.setPeriodo(dto.getPeriodo());
        turma.setVagasTotais(dto.getVagasTotais());
        turma.setDataInicio(dto.getDataInicio());
        turma.setDataFim(dto.getDataFim());
        if (dto.getStatus() != null) {
            turma.setStatus(dto.getStatus());
        }

        return toDTO(turmaRepository.save(turma));
    }

    @Transactional
    public void excluir(Long id) {
        buscarEntidade(id);

        if (matriculaRepository.existsByTurmaId(id)) {
            throw new RegraNegocioException("Esta turma não pode ser excluída pois possui alunos matriculados nela");
        }

        try {
            turmaRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("Esta turma não pode ser excluída pois possui alunos matriculados nela");
        }
    }

    @Transactional
    public void excluirEmLote(List<Long> ids) {
        for (Long id : ids) {
            excluir(id);
        }
    }

    private void validarDatas(TurmaDTO dto) {
        if (dto.getDataFim().isBefore(dto.getDataInicio())) {
            throw new IllegalArgumentException("A data de fim da turma nao pode ser anterior a data de inicio");
        }
    }

    private TurmaDTO toDTO(Turma turma) {
        TurmaDTO dto = new TurmaDTO();
        dto.setId(turma.getId());
        dto.setCodigo(turma.getCodigo());
        dto.setDisciplinaId(turma.getDisciplina().getId());
        dto.setNomeDisciplina(turma.getDisciplina().getNome());
        dto.setPeriodo(turma.getPeriodo());
        dto.setVagasTotais(turma.getVagasTotais());
        dto.setVagasOcupadas(turma.getVagasOcupadas());
        dto.setVagasDisponiveis(turma.getVagasTotais() - turma.getVagasOcupadas());
        dto.setDataInicio(turma.getDataInicio());
        dto.setDataFim(turma.getDataFim());
        dto.setStatus(turma.getStatus());
        return dto;
    }
}
