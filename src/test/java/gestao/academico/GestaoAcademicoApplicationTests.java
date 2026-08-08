package gestao.academico;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Garante que o contexto Spring sobe corretamente (todas as configuracoes,
 * beans e migrations Flyway sao aplicadas sem erro) usando o perfil de teste
 * (H2 em memoria).
 */
@SpringBootTest
@ActiveProfiles("test")
class GestaoAcademicoApplicationTests {

    @Test
    void contextLoads() {
    }
}
