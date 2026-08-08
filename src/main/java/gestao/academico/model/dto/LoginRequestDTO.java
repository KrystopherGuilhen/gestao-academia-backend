package gestao.academico.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "LoginRequestDTO",
        description = "DTO de entrada para autenticacao. Envie usuario e senha para receber um token JWT em POST /api/auth/login.",
        example = "{\n" +
                "  \"username\": \"admin\",\n" +
                "  \"password\": \"admin123\"\n" +
                "}"
)
public class LoginRequestDTO {

    @NotBlank(message = "O usuario e obrigatorio")
    @Schema(
            description = "Nome de usuario cadastrado",
            example = "admin",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(message = "A senha e obrigatoria")
    @Schema(
            description = "Senha do usuario, em texto plano (sera validada contra o hash BCrypt armazenado)",
            example = "admin123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    private String password;
}
