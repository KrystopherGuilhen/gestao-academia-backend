package gestao.academico.exception;

/**
 * Usada quando uma regra de negocio critica e violada, por exemplo:
 * turma fechada para novas matriculas, ou vagas esgotadas.
 * Sempre resulta em HTTP 422 (Unprocessable Entity).
 */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String message) {
        super(message);
    }
}
