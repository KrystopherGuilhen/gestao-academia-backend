package gestao.academico.controller;

import gestao.academico.model.dto.CursoDTO;
import gestao.academico.service.CursoService;
import gestao.academico.util.ApiResponse;
import gestao.academico.util.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
@Tag(name = "Cursos", description = "Cadastro de cursos")
public class CursoController {

    private final CursoService cursoService;

    @Operation(summary = "Lista cursos de forma paginada, com filtro opcional")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginaResponse<CursoDTO>>> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "1") int sortOrder,
            @RequestParam(required = false) String filter
    ) {
        Sort sort = (sortField == null || sortField.isBlank())
                ? Sort.unsorted()
                : Sort.by(sortOrder == 1 ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        PaginaResponse<CursoDTO> resultado = cursoService.consultaPaginada(pageable, filter);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cursos recuperados com sucesso", resultado));
    }

    @Operation(summary = "Lista todos os cursos, sem paginacao")
    @GetMapping("/todos")
    public ResponseEntity<ApiResponse<List<CursoDTO>>> listarTodos() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cursos recuperados com sucesso", cursoService.listarTodos()));
    }

    @Operation(summary = "Busca um curso pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CursoDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Curso encontrado", cursoService.buscarPorId(id)));
    }

    @Operation(summary = "Cadastra um novo curso")
    @PostMapping
    public ResponseEntity<ApiResponse<CursoDTO>> criar(@RequestBody @Valid CursoDTO dto) {
        CursoDTO criado = cursoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Curso cadastrado com sucesso", criado));
    }

    @Operation(summary = "Atualiza um curso existente")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CursoDTO>> atualizar(@PathVariable Long id, @RequestBody @Valid CursoDTO dto) {
        CursoDTO atualizado = cursoService.atualizar(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Curso atualizado com sucesso", atualizado));
    }

    @Operation(summary = "Exclui um curso")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        cursoService.excluir(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Curso excluido com sucesso", null));
    }

    @Operation(summary = "Exclui multiplos cursos")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> excluirEmLote(@RequestBody List<Long> ids) {
        cursoService.excluirEmLote(ids);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cursos excluidos com sucesso", null));
    }
}
