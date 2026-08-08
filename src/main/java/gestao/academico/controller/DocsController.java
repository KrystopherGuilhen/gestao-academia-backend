package gestao.academico.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

/**
 * Encaminha /docs (sem barra final) e /docs/ para a pagina estatica de
 * documentacao (Swagger UI customizado com toggle de tema claro/escuro).
 * Usa um redirect HTTP simples via Servlet API, sem depender de ViewResolver.
 */
@Controller
public class DocsController {

    @GetMapping({"/docs", "/docs/"})
    public void docs(HttpServletResponse response) throws IOException {
        response.sendRedirect("/docs/index.html");
    }
}
