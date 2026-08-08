package gestao.academico.controller;

import gestao.academico.model.dto.TurmaDTO;
import gestao.academico.service.TurmaService;
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
@RequestMapping("/api/turmas")
@RequiredArgsConstructor
@Tag(name = "Turmas", description = "Cadastro de turmas")
public class TurmaController {

    private final TurmaService turmaService;

    @Operation(summary = "Lista turmas de forma paginada, com filtro opcional")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginaResponse<TurmaDTO>>> listarPaginado(
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

        PaginaResponse<TurmaDTO> resultado = turmaService.consultaPaginada(pageable, filter);
        return ResponseEntity.ok(new ApiResponse<>(true, "Turmas recuperadas com sucesso", resultado));
    }

    @Operation(summary = "Lista todas as turmas, sem paginacao")
    @GetMapping("/todos")
    public ResponseEntity<ApiResponse<List<TurmaDTO>>> listarTodos() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Turmas recuperadas com sucesso", turmaService.listarTodos()));
    }

    @Operation(summary = "Busca uma turma pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TurmaDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Turma encontrada", turmaService.buscarPorId(id)));
    }

    @Operation(summary = "Cadastra uma nova turma")
    @PostMapping
    public ResponseEntity<ApiResponse<TurmaDTO>> criar(@RequestBody @Valid TurmaDTO dto) {
        TurmaDTO criado = turmaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Turma cadastrada com sucesso", criado));
    }

    @Operation(summary = "Atualiza uma turma existente")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TurmaDTO>> atualizar(@PathVariable Long id, @RequestBody @Valid TurmaDTO dto) {
        TurmaDTO atualizado = turmaService.atualizar(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Turma atualizada com sucesso", atualizado));
    }

    @Operation(summary = "Exclui uma turma")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        turmaService.excluir(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Turma excluida com sucesso", null));
    }

    @Operation(summary = "Exclui multiplas turmas")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> excluirEmLote(@RequestBody List<Long> ids) {
        turmaService.excluirEmLote(ids);
        return ResponseEntity.ok(new ApiResponse<>(true, "Turmas excluidas com sucesso", null));
    }
}
