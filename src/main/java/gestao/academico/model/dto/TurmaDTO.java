package gestao.academico.model.dto;

import gestao.academico.model.entidades.StatusTurma;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "TurmaDTO",
        description = "DTO para representar uma turma de uma disciplina, com controle de vagas e periodo letivo.",
        example = "{\n" +
                "  \"id\": 1,\n" +
                "  \"codigo\": \"ED-2025-2-A\",\n" +
                "  \"disciplinaId\": 1,\n" +
                "  \"nomeDisciplina\": \"Estrutura de Dados\",\n" +
                "  \"periodo\": \"2025.2\",\n" +
                "  \"vagasTotais\": 30,\n" +
                "  \"vagasOcupadas\": 12,\n" +
                "  \"vagasDisponiveis\": 18,\n" +
                "  \"dataInicio\": \"2025-08-04\",\n" +
                "  \"dataFim\": \"2025-12-12\",\n" +
                "  \"status\": \"ABERTA\"\n" +
                "}"
)
public class TurmaDTO {

    @Schema(
            description = "Identificador unico da turma, gerado automaticamente pelo banco de dados",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "O codigo da turma e obrigatorio")
    @Size(max = 40, message = "O codigo deve ter no maximo 40 caracteres")
    @Schema(
            description = "Codigo unico da turma",
            example = "ED-2025-2-A",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 40
    )
    private String codigo;

    @NotNull(message = "A disciplina e obrigatoria")
    @Schema(
            description = "Identificador da disciplina a qual esta turma pertence",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long disciplinaId;

    @Schema(
            description = "Nome da disciplina desta turma (preenchido automaticamente pela API a partir do disciplinaId)",
            example = "Estrutura de Dados",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String nomeDisciplina;

    @NotBlank(message = "O periodo e obrigatorio")
    @Size(max = 20, message = "O periodo deve ter no maximo 20 caracteres")
    @Schema(
            description = "Periodo letivo da turma (ex: ano.semestre)",
            example = "2025.2",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 20
    )
    private String periodo;

    @NotNull(message = "O total de vagas e obrigatorio")
    @Positive(message = "O total de vagas deve ser maior que zero")
    @Schema(
            description = "Numero total de vagas da turma",
            example = "30",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1"
    )
    private Integer vagasTotais;

    @Schema(
            description = "Numero de vagas ja consumidas por matriculas CONFIRMADAS. Calculado e controlado exclusivamente pelo backend — nunca aceito do cliente",
            example = "12",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Integer vagasOcupadas;

    @Schema(
            description = "Numero de vagas ainda disponiveis (vagasTotais - vagasOcupadas). Calculado automaticamente pelo backend",
            example = "18",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Integer vagasDisponiveis;

    @NotNull(message = "A data de inicio e obrigatoria")
    @Schema(
            description = "Data de inicio da turma (formato ISO: yyyy-MM-dd)",
            example = "2025-08-04",
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = "yyyy-MM-dd"
    )
    private LocalDate dataInicio;

    @NotNull(message = "A data de fim e obrigatoria")
    @Schema(
            description = "Data de encerramento da turma (formato ISO: yyyy-MM-dd). Deve ser posterior a dataInicio",
            example = "2025-12-12",
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = "yyyy-MM-dd"
    )
    private LocalDate dataFim;

    @Schema(
            description = "Situacao da turma. Somente turmas ABERTA aceitam novas matriculas. Quando nao informado na criacao, assume-se ABERTA",
            example = "ABERTA",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            allowableValues = {"ABERTA", "FECHADA", "CANCELADA"},
            defaultValue = "ABERTA"
    )
    private StatusTurma status;
}
