package gestao.academico.repository;

import gestao.academico.model.entidades.Disciplina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

    boolean existsByCodigo(String codigo);

    List<Disciplina> findByCursoId(Long cursoId);

    @Query("""
            select d from Disciplina d
            where (:filtro is null or :filtro = '')
               or lower(d.nome) like lower(concat('%', :filtro, '%'))
               or lower(d.codigo) like lower(concat('%', :filtro, '%'))
            """)
    Page<Disciplina> buscarComFiltro(@Param("filtro") String filtro, Pageable pageable);
}
