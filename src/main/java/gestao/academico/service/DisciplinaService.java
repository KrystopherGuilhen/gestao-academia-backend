package gestao.academico.service;

import gestao.academico.exception.DuplicateException;
import gestao.academico.exception.RegraNegocioException;
import gestao.academico.exception.ResourceNotFoundException;
import gestao.academico.model.dto.DisciplinaDTO;
import gestao.academico.model.entidades.Curso;
import gestao.academico.model.entidades.Disciplina;
import gestao.academico.repository.DisciplinaRepository;
import gestao.academico.util.PaginaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final CursoService cursoService;

    @Transactional(readOnly = true)
    public PaginaResponse<DisciplinaDTO> consultaPaginada(Pageable pageable, String filtro) {
        return PaginaResponse.from(disciplinaRepository.buscarComFiltro(filtro, pageable).map(this::toDTO));
    }

    @Transactional(readOnly = true)
    public List<DisciplinaDTO> listarTodos() {
        return disciplinaRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public DisciplinaDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Disciplina buscarEntidade(Long id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina com id " + id + " nao encontrada"));
    }

    @Transactional
    public DisciplinaDTO criar(DisciplinaDTO dto) {
        if (disciplinaRepository.existsByCodigo(dto.getCodigo())) {
            throw new DuplicateException("Ja existe uma disciplina cadastrada com o codigo " + dto.getCodigo());
        }

        Disciplina disciplina = new Disciplina();
        aplicarDadosNaEntidade(dto, disciplina);
        disciplina.setAtivo(dto.getAtivo() == null ? Boolean.TRUE : dto.getAtivo());

        return toDTO(disciplinaRepository.save(disciplina));
    }

    @Transactional
    public DisciplinaDTO atualizar(Long id, DisciplinaDTO dto) {
        Disciplina disciplina = buscarEntidade(id);

        if (!disciplina.getCodigo().equals(dto.getCodigo()) && disciplinaRepository.existsByCodigo(dto.getCodigo())) {
            throw new DuplicateException("Ja existe uma disciplina cadastrada com o codigo " + dto.getCodigo());
        }

        aplicarDadosNaEntidade(dto, disciplina);
        if (dto.getAtivo() != null) {
            disciplina.setAtivo(dto.getAtivo());
        }

        return toDTO(disciplinaRepository.save(disciplina));
    }

    @Transactional
    public void excluir(Long id) {
        buscarEntidade(id);
        try {
            disciplinaRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("Nao e possivel excluir a disciplina pois existem turmas vinculadas a ela");
        }
    }

    @Transactional
    public void excluirEmLote(List<Long> ids) {
        for (Long id : ids) {
            excluir(id);
        }
    }

    private void aplicarDadosNaEntidade(DisciplinaDTO dto, Disciplina disciplina) {
        Curso curso = cursoService.buscarEntidade(dto.getCursoId());
        disciplina.setNome(dto.getNome());
        disciplina.setCodigo(dto.getCodigo());
        disciplina.setCargaHoraria(dto.getCargaHoraria());
        disciplina.setCurso(curso);
    }

    private DisciplinaDTO toDTO(Disciplina disciplina) {
        DisciplinaDTO dto = new DisciplinaDTO();
        dto.setId(disciplina.getId());
        dto.setNome(disciplina.getNome());
        dto.setCodigo(disciplina.getCodigo());
        dto.setCargaHoraria(disciplina.getCargaHoraria());
        dto.setCursoId(disciplina.getCurso().getId());
        dto.setNomeCurso(disciplina.getCurso().getNome());
        dto.setAtivo(disciplina.getAtivo());
        return dto;
    }
}
