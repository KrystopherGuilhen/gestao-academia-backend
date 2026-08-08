package gestao.academico.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper de paginacao no formato { data, total }, que e exatamente o
 * contrato consumido pelo motor generico de tabelas do frontend
 * (Paginado&lt;T&gt; / CrudAbstractComponent.retornaDadosPaginados),
 * reaproveitado do projeto anterior.
 */
@Data
@AllArgsConstructor
public class PaginaResponse<T> {
    private List<T> data;
    private long total;

    public static <T> PaginaResponse<T> from(Page<T> page) {
        return new PaginaResponse<>(page.getContent(), page.getTotalElements());
    }
}
