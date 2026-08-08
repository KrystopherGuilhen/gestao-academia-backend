package gestao.academico.repository;

import gestao.academico.model.entidades.Turma;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {

    boolean existsByCodigo(String codigo);

    /**
     * Busca a turma com um lock pessimista de escrita (SELECT ... FOR UPDATE).
     * Usada exclusivamente pelo MatriculaService ao confirmar ou cancelar uma
     * matricula: enquanto a transacao estiver aberta, nenhuma outra transacao
     * consegue ler/alterar a mesma turma para vaga, o que impede que duas
     * confirmacoes concorrentes furem o limite de vagas (overbooking).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Turma t where t.id = :id")
    Optional<Turma> buscarParaAtualizacaoDeVagas(@Param("id") Long id);

    @Query("""
            select t from Turma t
            where (:filtro is null or :filtro = '')
               or lower(t.codigo) like lower(concat('%', :filtro, '%'))
               or lower(t.periodo) like lower(concat('%', :filtro, '%'))
            """)
    Page<Turma> buscarComFiltro(@Param("filtro") String filtro, Pageable pageable);
}
