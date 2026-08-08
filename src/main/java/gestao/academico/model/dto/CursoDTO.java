package gestao.academico.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "CursoDTO",
        description = "DTO para representar um curso de graduacao/formacao oferecido pela instituicao.",
        example = "{\n" +
                "  \"id\": 1,\n" +
                "  \"nome\": \"Engenharia de Software\",\n" +
                "  \"codigo\": \"ENG-SW\",\n" +
                "  \"cargaHorariaTotal\": 3600,\n" +
                "  \"ativo\": true\n" +
                "}"
)
public class CursoDTO {

    @Schema(
            description = "Identificador unico do curso, gerado automaticamente pelo banco de dados",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "O nome do curso e obrigatorio")
    @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
    @Schema(
            description = "Nome do curso",
            example = "Engenharia de Software",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 150
    )
    private String nome;

    @NotBlank(message = "O codigo do curso e obrigatorio")
    @Size(max = 30, message = "O codigo deve ter no maximo 30 caracteres")
    @Schema(
            description = "Codigo curto e unico que identifica o curso (usado em relatorios e nos codigos de turma/disciplina)",
            example = "ENG-SW",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 30
    )
    private String codigo;

    @NotNull(message = "A carga horaria total e obrigatoria")
    @Positive(message = "A carga horaria total deve ser maior que zero")
    @Schema(
            description = "Carga horaria total do curso, em horas",
            example = "3600",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1"
    )
    private Integer cargaHorariaTotal;

    @Schema(
            description = "Indica se o curso esta ativo (aceitando novas disciplinas/turmas). Quando nao informado na criacao, assume-se \"true\"",
            example = "true",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            defaultValue = "true"
    )
    private Boolean ativo;
}
