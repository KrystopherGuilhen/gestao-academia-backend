package gestao.academico.service;

import gestao.academico.exception.DuplicateException;
import gestao.academico.exception.RegraNegocioException;
import gestao.academico.exception.ResourceNotFoundException;
import gestao.academico.model.dto.CursoDTO;
import gestao.academico.model.entidades.Curso;
import gestao.academico.repository.CursoRepository;
import gestao.academico.util.PaginaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    @Transactional(readOnly = true)
    public PaginaResponse<CursoDTO> consultaPaginada(Pageable pageable, String filtro) {
        return PaginaResponse.from(cursoRepository.buscarComFiltro(filtro, pageable).map(this::toDTO));
    }

    @Transactional(readOnly = true)
    public List<CursoDTO> listarTodos() {
        return cursoRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public CursoDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Curso buscarEntidade(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso com id " + id + " nao encontrado"));
    }

    @Transactional
    public CursoDTO criar(CursoDTO dto) {
        if (cursoRepository.existsByCodigo(dto.getCodigo())) {
            throw new DuplicateException("Ja existe um curso cadastrado com o codigo " + dto.getCodigo());
        }

        Curso curso = new Curso();
        aplicarDadosNaEntidade(dto, curso);
        curso.setAtivo(dto.getAtivo() == null ? Boolean.TRUE : dto.getAtivo());

        return toDTO(cursoRepository.save(curso));
    }

    @Transactional
    public CursoDTO atualizar(Long id, CursoDTO dto) {
        Curso curso = buscarEntidade(id);

        if (!curso.getCodigo().equals(dto.getCodigo()) && cursoRepository.existsByCodigo(dto.getCodigo())) {
            throw new DuplicateException("Ja existe um curso cadastrado com o codigo " + dto.getCodigo());
        }

        aplicarDadosNaEntidade(dto, curso);
        if (dto.getAtivo() != null) {
            curso.setAtivo(dto.getAtivo());
        }

        return toDTO(cursoRepository.save(curso));
    }

    @Transactional
    public void excluir(Long id) {
        buscarEntidade(id);
        try {
            cursoRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("Nao e possivel excluir o curso pois existem disciplinas vinculadas a ele");
        }
    }

    @Transactional
    public void excluirEmLote(List<Long> ids) {
        for (Long id : ids) {
            excluir(id);
        }
    }

    private void aplicarDadosNaEntidade(CursoDTO dto, Curso curso) {
        curso.setNome(dto.getNome());
        curso.setCodigo(dto.getCodigo());
        curso.setCargaHorariaTotal(dto.getCargaHorariaTotal());
    }

    private CursoDTO toDTO(Curso curso) {
        CursoDTO dto = new CursoDTO();
        dto.setId(curso.getId());
        dto.setNome(curso.getNome());
        dto.setCodigo(curso.getCodigo());
        dto.setCargaHorariaTotal(curso.getCargaHorariaTotal());
        dto.setAtivo(curso.getAtivo());
        return dto;
    }
}
