package gestao.academico.repository;

import gestao.academico.model.entidades.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    Optional<Aluno> findByEmail(String email);

    Optional<Aluno> findByCpf(String cpf);

    @Query("""
            select a from Aluno a
            where (:filtro is null or :filtro = '')
               or lower(a.nome) like lower(concat('%', :filtro, '%'))
               or a.cpf like concat('%', :filtro, '%')
               or lower(a.email) like lower(concat('%', :filtro, '%'))
            """)
    Page<Aluno> buscarComFiltro(@Param("filtro") String filtro, Pageable pageable);
}
