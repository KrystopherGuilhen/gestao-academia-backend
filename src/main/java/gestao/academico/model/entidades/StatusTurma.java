package gestao.academico.model.entidades;

/**
 * Situacao de uma turma.
 * Somente turmas ABERTA aceitam novas matriculas.
 */
public enum StatusTurma {
    ABERTA,
    FECHADA,
    CANCELADA
}
