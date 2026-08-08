package gestao.academico.exception;

/**
 * Usada quando um registro unico (email, cpf, codigo, ou o par
 * aluno+turma de uma matricula) ja existe.
 */
public class DuplicateException extends RuntimeException {
    public DuplicateException(String message) {
        super(message);
    }
}
