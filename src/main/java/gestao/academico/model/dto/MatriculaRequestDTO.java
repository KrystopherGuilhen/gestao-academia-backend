package gestao.academico.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload minimo para criar uma matricula: o cliente informa apenas
 * quem esta se matriculando e em qual turma.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "MatriculaRequestDTO",
        description = "DTO de entrada para matricular um aluno em uma turma. A matricula e criada com status PENDENTE " +
                "e nao consome vaga da turma ate ser confirmada (ver POST /api/matriculas/{id}/confirmar).",
        example = "{\n" +
                "  \"alunoId\": 1,\n" +
                "  \"turmaId\": 3\n" +
                "}"
)
public class MatriculaRequestDTO {

    @NotNull(message = "O aluno e obrigatorio")
    @Schema(
            description = "Identificador do aluno a ser matriculado",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long alunoId;

    @NotNull(message = "A turma e obrigatoria")
    @Schema(
            description = "Identificador da turma na qual o aluno sera matriculado. A turma precisa estar com status ABERTA",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long turmaId;
}
