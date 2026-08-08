package gestao.academico.service;

import gestao.academico.config.JwtService;
import gestao.academico.model.dto.LoginRequestDTO;
import gestao.academico.model.dto.LoginResponseDTO;
import gestao.academico.model.entidades.Usuario;
import gestao.academico.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .filter(Usuario::getAtivo)
                .orElseThrow(() -> new BadCredentialsException("Usuario ou senha invalidos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Usuario ou senha invalidos");
        }

        String token = jwtService.gerarToken(usuario.getUsername());
        return new LoginResponseDTO(token, usuario.getUsername(), usuario.getNome());
    }
}
