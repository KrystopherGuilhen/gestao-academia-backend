package gestao.academico.repository;

import gestao.academico.model.entidades.Matricula;
import gestao.academico.model.entidades.StatusMatricula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    boolean existsByAlunoIdAndTurmaId(Long alunoId, Long turmaId);

    /** Usado para bloquear a exclusao de um aluno que ja possui qualquer matricula (mesmo cancelada). */
    boolean existsByAlunoId(Long alunoId);

    /** Usado para bloquear a exclusao de uma turma que ja possui qualquer matricula (mesmo cancelada). */
    boolean existsByTurmaId(Long turmaId);

    Optional<Matricula> findByAlunoIdAndTurmaId(Long alunoId, Long turmaId);

    List<Matricula> findByAlunoId(Long alunoId);

    List<Matricula> findByTurmaId(Long turmaId);

    long countByTurmaIdAndStatus(Long turmaId, StatusMatricula status);

    @Query("""
            select m from Matricula m
            where (:filtro is null or :filtro = '')
               or lower(m.aluno.nome) like lower(concat('%', :filtro, '%'))
               or lower(m.turma.codigo) like lower(concat('%', :filtro, '%'))
               or lower(m.turma.disciplina.nome) like lower(concat('%', :filtro, '%'))
            """)
    Page<Matricula> buscarComFiltro(@Param("filtro") String filtro, Pageable pageable);
}
