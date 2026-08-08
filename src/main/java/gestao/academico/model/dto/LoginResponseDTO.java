package gestao.academico.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "LoginResponseDTO",
        description = "DTO de saida do login bem-sucedido, com o token JWT a ser usado no cabecalho Authorization das demais requisicoes.",
        example = "{\n" +
                "  \"token\": \"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcyMzAwMDAwMH0.assinatura\",\n" +
                "  \"username\": \"admin\",\n" +
                "  \"nome\": \"Administrador do Sistema\"\n" +
                "}"
)
public class LoginResponseDTO {

    @Schema(
            description = "Token JWT a ser enviado no cabecalho 'Authorization: Bearer {token}' nas demais requisicoes. Valido pelo tempo configurado em JWT_EXPIRATION",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.assinatura",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String token;

    @Schema(description = "Nome de usuario autenticado", example = "admin", accessMode = Schema.AccessMode.READ_ONLY)
    private String username;

    @Schema(description = "Nome completo do usuario autenticado", example = "Administrador do Sistema", accessMode = Schema.AccessMode.READ_ONLY)
    private String nome;
}
