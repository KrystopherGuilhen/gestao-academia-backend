package gestao.academico.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "DisciplinaDTO",
        description = "DTO para representar uma disciplina, sempre vinculada a um curso.",
        example = "{\n" +
                "  \"id\": 1,\n" +
                "  \"nome\": \"Estrutura de Dados\",\n" +
                "  \"codigo\": \"ENG-SW-101\",\n" +
                "  \"cargaHoraria\": 80,\n" +
                "  \"cursoId\": 1,\n" +
                "  \"nomeCurso\": \"Engenharia de Software\",\n" +
                "  \"ativo\": true\n" +
                "}"
)
public class DisciplinaDTO {

    @Schema(
            description = "Identificador unico da disciplina, gerado automaticamente pelo banco de dados",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "O nome da disciplina e obrigatorio")
    @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
    @Schema(
            description = "Nome da disciplina",
            example = "Estrutura de Dados",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 150
    )
    private String nome;

    @NotBlank(message = "O codigo da disciplina e obrigatorio")
    @Size(max = 30, message = "O codigo deve ter no maximo 30 caracteres")
    @Schema(
            description = "Codigo curto e unico que identifica a disciplina",
            example = "ENG-SW-101",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 30
    )
    private String codigo;

    @NotNull(message = "A carga horaria e obrigatoria")
    @Positive(message = "A carga horaria deve ser maior que zero")
    @Schema(
            description = "Carga horaria da disciplina, em horas",
            example = "80",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1"
    )
    private Integer cargaHoraria;

    @NotNull(message = "O curso e obrigatorio")
    @Schema(
            description = "Identificador do curso ao qual esta disciplina pertence",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long cursoId;

    @Schema(
            description = "Nome do curso ao qual a disciplina pertence (preenchido automaticamente pela API a partir do cursoId)",
            example = "Engenharia de Software",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String nomeCurso;

    @Schema(
            description = "Indica se a disciplina esta ativa (podendo ter novas turmas abertas). Quando nao informado na criacao, assume-se \"true\"",
            example = "true",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            defaultValue = "true"
    )
    private Boolean ativo;
}
