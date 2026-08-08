package gestao.academico.controller;

import gestao.academico.model.dto.AlunoDTO;
import gestao.academico.service.AlunoService;
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
@RequestMapping("/api/alunos")
@RequiredArgsConstructor
@Tag(name = "Alunos", description = "Cadastro de alunos")
public class AlunoController {

    private final AlunoService alunoService;

    @Operation(summary = "Lista alunos de forma paginada, com filtro opcional")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginaResponse<AlunoDTO>>> listarPaginado(
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

        PaginaResponse<AlunoDTO> resultado = alunoService.consultaPaginada(pageable, filter);
        return ResponseEntity.ok(new ApiResponse<>(true, "Alunos recuperados com sucesso", resultado));
    }

    @Operation(summary = "Lista todos os alunos, sem paginacao")
    @GetMapping("/todos")
    public ResponseEntity<ApiResponse<List<AlunoDTO>>> listarTodos() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Alunos recuperados com sucesso", alunoService.listarTodos()));
    }

    @Operation(summary = "Busca um aluno pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlunoDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Aluno encontrado", alunoService.buscarPorId(id)));
    }

    @Operation(summary = "Cadastra um novo aluno")
    @PostMapping
    public ResponseEntity<ApiResponse<AlunoDTO>> criar(@RequestBody @Valid AlunoDTO dto) {
        AlunoDTO criado = alunoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Aluno cadastrado com sucesso", criado));
    }

    @Operation(summary = "Atualiza um aluno existente")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AlunoDTO>> atualizar(@PathVariable Long id, @RequestBody @Valid AlunoDTO dto) {
        AlunoDTO atualizado = alunoService.atualizar(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Aluno atualizado com sucesso", atualizado));
    }

    @Operation(summary = "Exclui um aluno")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        alunoService.excluir(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Aluno excluido com sucesso", null));
    }

    @Operation(summary = "Exclui multiplos alunos")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> excluirEmLote(@RequestBody List<Long> ids) {
        alunoService.excluirEmLote(ids);
        return ResponseEntity.ok(new ApiResponse<>(true, "Alunos excluidos com sucesso", null));
    }
}
