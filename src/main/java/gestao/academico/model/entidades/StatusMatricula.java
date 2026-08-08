package gestao.academico.model.entidades;

/**
 * Ciclo de vida de uma matricula.
 * PENDENTE  -> criada, ainda nao consome vaga da turma.
 * CONFIRMADA -> vaga da turma foi consumida.
 * CANCELADA -> matricula encerrada; se estava CONFIRMADA, a vaga e liberada.
 */
public enum StatusMatricula {
    PENDENTE,
    CONFIRMADA,
    CANCELADA
}
