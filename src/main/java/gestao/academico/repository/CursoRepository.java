package gestao.academico.repository;

import gestao.academico.model.entidades.Curso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    boolean existsByCodigo(String codigo);

    @Query("""
            select c from Curso c
            where (:filtro is null or :filtro = '')
               or lower(c.nome) like lower(concat('%', :filtro, '%'))
               or lower(c.codigo) like lower(concat('%', :filtro, '%'))
            """)
    Page<Curso> buscarComFiltro(@Param("filtro") String filtro, Pageable pageable);
}
