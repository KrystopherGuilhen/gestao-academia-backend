package gestao.academico.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Envelope padrao de resposta da API: toda rota devolve o mesmo
 * formato (sucesso, mensagem e dado), o que facilita o tratamento
 * generico no frontend.
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean sucesso;
    private String mensagem;
    private T dados;
}
