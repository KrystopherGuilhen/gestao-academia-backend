package gestao.academico.model.dto;

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
        name = "AlunoDTO",
        description = "DTO para representar um aluno do sistema academico.",
        example = "{\n" +
                "  \"id\": 1,\n" +
                "  \"nome\": \"Ana Beatriz Souza\",\n" +
                "  \"email\": \"ana.souza@email.com\",\n" +
                "  \"cpf\": \"11122233344\",\n" +
                "  \"dataNascimento\": \"2001-03-15\",\n" +
                "  \"ativo\": true\n" +
                "}"
)
public class AlunoDTO {

    @Schema(
            description = "Identificador unico do aluno, gerado automaticamente pelo banco de dados",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "O nome do aluno e obrigatorio")
    @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres")
    @Schema(
            description = "Nome completo do aluno",
            example = "Ana Beatriz Souza",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 150
    )
    private String nome;

    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "Informe um e-mail valido")
    @Schema(
            description = "E-mail do aluno. Deve ser unico no sistema — usado tambem como referencia de contato",
            example = "ana.souza@email.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "O CPF e obrigatorio")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 digitos numericos")
    @Schema(
            description = "CPF do aluno, somente digitos (sem pontos ou traco). Deve ser unico no sistema",
            example = "11122233344",
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = "^\\d{11}$",
            minLength = 11,
            maxLength = 11
    )
    private String cpf;

    @NotNull(message = "A data de nascimento e obrigatoria")
    @Past(message = "A data de nascimento deve estar no passado")
    @Schema(
            description = "Data de nascimento do aluno (formato ISO: yyyy-MM-dd). Deve ser uma data no passado",
            example = "2001-03-15",
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = "yyyy-MM-dd"
    )
    private LocalDate dataNascimento;

    @Schema(
            description = "Indica se o aluno esta ativo no sistema. Quando nao informado na criacao, assume-se \"true\"",
            example = "true",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            defaultValue = "true"
    )
    private Boolean ativo;
}
