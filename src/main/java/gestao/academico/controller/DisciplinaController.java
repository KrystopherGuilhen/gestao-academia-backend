package gestao.academico.controller;

import gestao.academico.model.dto.DisciplinaDTO;
import gestao.academico.service.DisciplinaService;
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
@RequestMapping("/api/disciplinas")
@RequiredArgsConstructor
@Tag(name = "Disciplinas", description = "Cadastro de disciplinas")
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    @Operation(summary = "Lista disciplinas de forma paginada, com filtro opcional")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginaResponse<DisciplinaDTO>>> listarPaginado(
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

        PaginaResponse<DisciplinaDTO> resultado = disciplinaService.consultaPaginada(pageable, filter);
        return ResponseEntity.ok(new ApiResponse<>(true, "Disciplinas recuperadas com sucesso", resultado));
    }

    @Operation(summary = "Lista todas as disciplinas, sem paginacao")
    @GetMapping("/todos")
    public ResponseEntity<ApiResponse<List<DisciplinaDTO>>> listarTodos() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Disciplinas recuperadas com sucesso", disciplinaService.listarTodos()));
    }

    @Operation(summary = "Busca uma disciplina pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DisciplinaDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Disciplina encontrada", disciplinaService.buscarPorId(id)));
    }

    @Operation(summary = "Cadastra uma nova disciplina")
    @PostMapping
    public ResponseEntity<ApiResponse<DisciplinaDTO>> criar(@RequestBody @Valid DisciplinaDTO dto) {
        DisciplinaDTO criado = disciplinaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Disciplina cadastrada com sucesso", criado));
    }

    @Operation(summary = "Atualiza uma disciplina existente")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DisciplinaDTO>> atualizar(@PathVariable Long id, @RequestBody @Valid DisciplinaDTO dto) {
        DisciplinaDTO atualizado = disciplinaService.atualizar(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Disciplina atualizada com sucesso", atualizado));
    }

    @Operation(summary = "Exclui uma disciplina")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        disciplinaService.excluir(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Disciplina excluida com sucesso", null));
    }

    @Operation(summary = "Exclui multiplas disciplinas")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> excluirEmLote(@RequestBody List<Long> ids) {
        disciplinaService.excluirEmLote(ids);
        return ResponseEntity.ok(new ApiResponse<>(true, "Disciplinas excluidas com sucesso", null));
    }
}
