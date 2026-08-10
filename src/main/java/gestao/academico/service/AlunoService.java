package gestao.academico.service;

import gestao.academico.exception.DuplicateException;
import gestao.academico.exception.RegraNegocioException;
import gestao.academico.exception.ResourceNotFoundException;
import gestao.academico.model.dto.AlunoDTO;
import gestao.academico.model.entidades.Aluno;
import gestao.academico.repository.AlunoRepository;
import gestao.academico.repository.MatriculaRepository;
import gestao.academico.util.PaginaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final MatriculaRepository matriculaRepository;

    @Transactional(readOnly = true)
    public PaginaResponse<AlunoDTO> consultaPaginada(Pageable pageable, String filtro) {
        return PaginaResponse.from(alunoRepository.buscarComFiltro(filtro, pageable).map(this::toDTO));
    }

    @Transactional(readOnly = true)
    public List<AlunoDTO> listarTodos() {
        return alunoRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public AlunoDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Aluno buscarEntidade(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno com id " + id + " nao encontrado"));
    }

    @Transactional
    public AlunoDTO criar(AlunoDTO dto) {
        validarDuplicidade(dto, null);

        Aluno aluno = new Aluno();
        aplicarDadosNaEntidade(dto, aluno);
        aluno.setAtivo(dto.getAtivo() == null ? Boolean.TRUE : dto.getAtivo());

        return toDTO(alunoRepository.save(aluno));
    }

    @Transactional
    public AlunoDTO atualizar(Long id, AlunoDTO dto) {
        Aluno aluno = buscarEntidade(id);
        validarDuplicidade(dto, id);

        aplicarDadosNaEntidade(dto, aluno);
        if (dto.getAtivo() != null) {
            aluno.setAtivo(dto.getAtivo());
        }

        return toDTO(alunoRepository.save(aluno));
    }

    @Transactional
    public void excluir(Long id) {
        buscarEntidade(id);

        if (matriculaRepository.existsByAlunoId(id)) {
            throw new RegraNegocioException("Este aluno não pode ser excluído pois está relacionado a uma turma");
        }

        try {
            alunoRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("Este aluno não pode ser excluído pois está relacionado a uma turma");
        }
    }

    @Transactional
    public void excluirEmLote(List<Long> ids) {
        for (Long id : ids) {
            excluir(id);
        }
    }

    private void validarDuplicidade(AlunoDTO dto, Long idAtual) {
        alunoRepository.findByEmail(dto.getEmail()).ifPresent(existente -> {
            if (idAtual == null || !existente.getId().equals(idAtual)) {
                throw new DuplicateException("Ja existe um aluno cadastrado com o e-mail " + dto.getEmail());
            }
        });

        alunoRepository.findByCpf(dto.getCpf()).ifPresent(existente -> {
            if (idAtual == null || !existente.getId().equals(idAtual)) {
                throw new DuplicateException("Ja existe um aluno cadastrado com o CPF " + dto.getCpf());
            }
        });
    }

    private void aplicarDadosNaEntidade(AlunoDTO dto, Aluno aluno) {
        aluno.setNome(dto.getNome());
        aluno.setEmail(dto.getEmail());
        aluno.setCpf(dto.getCpf());
        aluno.setDataNascimento(dto.getDataNascimento());
    }

    private AlunoDTO toDTO(Aluno aluno) {
        AlunoDTO dto = new AlunoDTO();
        dto.setId(aluno.getId());
        dto.setNome(aluno.getNome());
        dto.setEmail(aluno.getEmail());
        dto.setCpf(aluno.getCpf());
        dto.setDataNascimento(aluno.getDataNascimento());
        dto.setAtivo(aluno.getAtivo());
        return dto;
    }
}
