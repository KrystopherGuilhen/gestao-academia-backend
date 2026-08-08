package gestao.academico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita o preenchimento automatico de criadoEm/atualizadoEm
 * (definidos na classe Auditable) em todas as entidades.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
