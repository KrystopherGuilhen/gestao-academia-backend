package gestao.academico.controller;

import gestao.academico.model.dto.MatriculaRequestDTO;
import gestao.academico.model.dto.MatriculaResponseDTO;
import gestao.academico.service.MatriculaService;
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
@RequestMapping("/api/matriculas")
@RequiredArgsConstructor
@Tag(name = "Matriculas", description = "Fluxo de matricula de alunos em turmas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    @Operation(summary = "Lista matriculas de forma paginada, com filtro opcional")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginaResponse<MatriculaResponseDTO>>> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "1") int sortOrder,
            @RequestParam(required = false) String filter
    ) {
        Sort sort = (sortField == null || sortField.isBlank())
                ? Sort.by(Sort.Direction.DESC, "dataMatricula")
                : Sort.by(sortOrder == 1 ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(new ApiResponse<>(true, "Matriculas recuperadas com sucesso",
                matriculaService.consultaPaginada(pageable, filter)));
    }

    @Operation(
            summary = "Matricula um aluno em uma turma",
            description = "Cria a matricula com status PENDENTE. A vaga da turma so e consumida quando a matricula for confirmada."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<MatriculaResponseDTO>> matricular(@RequestBody @Valid MatriculaRequestDTO request) {
        MatriculaResponseDTO criada = matriculaService.matricular(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Matricula criada com sucesso (status PENDENTE)", criada));
    }

    @Operation(
            summary = "Confirma uma matricula pendente",
            description = "Consome uma vaga da turma. Falha se nao houver vagas disponiveis."
    )
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<ApiResponse<MatriculaResponseDTO>> confirmar(@PathVariable Long id) {
        MatriculaResponseDTO confirmada = matriculaService.confirmar(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Matricula confirmada com sucesso", confirmada));
    }

    @Operation(
            summary = "Cancela uma matricula",
            description = "Se a matricula estava CONFIRMADA, a vaga da turma e liberada automaticamente."
    )
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<MatriculaResponseDTO>> cancelar(@PathVariable Long id) {
        MatriculaResponseDTO cancelada = matriculaService.cancelar(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Matricula cancelada com sucesso", cancelada));
    }

    @Operation(summary = "Lista as matriculas de um aluno")
    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<ApiResponse<List<MatriculaResponseDTO>>> consultarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Matriculas do aluno recuperadas com sucesso",
                matriculaService.consultarPorAluno(alunoId)));
    }

    @Operation(summary = "Lista as matriculas de uma turma")
    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<ApiResponse<List<MatriculaResponseDTO>>> consultarPorTurma(@PathVariable Long turmaId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Matriculas da turma recuperadas com sucesso",
                matriculaService.consultarPorTurma(turmaId)));
    }
}
