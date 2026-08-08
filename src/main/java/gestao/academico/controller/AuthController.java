package gestao.academico.controller;

import gestao.academico.model.dto.LoginRequestDTO;
import gestao.academico.model.dto.LoginResponseDTO;
import gestao.academico.service.AuthService;
import gestao.academico.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacao", description = "Login e emissao de token JWT")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Autentica um usuario e retorna um token JWT")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody @Valid LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login realizado com sucesso", response));
    }
}
