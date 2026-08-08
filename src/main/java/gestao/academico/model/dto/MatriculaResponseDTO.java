package gestao.academico.model.dto;

import gestao.academico.model.entidades.StatusMatricula;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "MatriculaResponseDTO",
        description = "DTO de saida com os dados de uma matricula, incluindo informacoes ja resolvidas do aluno e da turma " +
                "para facilitar a exibicao no cliente sem precisar de consultas adicionais.",
        example = "{\n" +
                "  \"id\": 10,\n" +
                "  \"alunoId\": 1,\n" +
                "  \"nomeAluno\": \"Ana Beatriz Souza\",\n" +
                "  \"turmaId\": 3,\n" +
                "  \"codigoTurma\": \"PW-2025-2-A\",\n" +
                "  \"nomeDisciplina\": \"Programacao Web\",\n" +
                "  \"status\": \"CONFIRMADA\",\n" +
                "  \"dataMatricula\": \"2025-08-06T14:32:10\",\n" +
                "  \"dataConfirmacao\": \"2025-08-06T14:35:02\",\n" +
                "  \"dataCancelamento\": null\n" +
                "}"
)
public class MatriculaResponseDTO {

    @Schema(description = "Identificador unico da matricula", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Identificador do aluno matriculado", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long alunoId;

    @Schema(description = "Nome do aluno matriculado (resolvido automaticamente pela API)", example = "Ana Beatriz Souza", accessMode = Schema.AccessMode.READ_ONLY)
    private String nomeAluno;

    @Schema(description = "Identificador da turma da matricula", example = "3", accessMode = Schema.AccessMode.READ_ONLY)
    private Long turmaId;

    @Schema(description = "Codigo da turma da matricula (resolvido automaticamente pela API)", example = "PW-2025-2-A", accessMode = Schema.AccessMode.READ_ONLY)
    private String codigoTurma;

    @Schema(description = "Nome da disciplina da turma (resolvido automaticamente pela API)", example = "Programacao Web", accessMode = Schema.AccessMode.READ_ONLY)
    private String nomeDisciplina;

    @Schema(
            description = "Situacao atual da matricula. PENDENTE nao consome vaga; CONFIRMADA consome vaga da turma; CANCELADA libera a vaga se estava CONFIRMADA",
            example = "CONFIRMADA",
            allowableValues = {"PENDENTE", "CONFIRMADA", "CANCELADA"},
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private StatusMatricula status;

    @Schema(description = "Data e hora em que a matricula foi criada (status PENDENTE)", example = "2025-08-06T14:32:10", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataMatricula;

    @Schema(description = "Data e hora em que a matricula foi confirmada. Nulo se a matricula nunca foi confirmada", example = "2025-08-06T14:35:02", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataConfirmacao;

    @Schema(description = "Data e hora em que a matricula foi cancelada. Nulo se a matricula nao foi cancelada", example = "null", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataCancelamento;
}
