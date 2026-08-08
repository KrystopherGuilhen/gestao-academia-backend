package gestao.academico.model.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "turma")
public class Turma extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 40)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;

    @Column(name = "periodo", nullable = false, length = 20)
    private String periodo;

    @Column(name = "vagas_totais", nullable = false)
    private Integer vagasTotais;

    /**
     * Contador de vagas ja consumidas por matriculas CONFIRMADAS.
     * Nunca e enviado pelo cliente: e calculado e atualizado somente
     * pelo MatriculaService, dentro de uma transacao com lock, para
     * proteger a regra de limite de vagas contra concorrencia.
     */
    @Column(name = "vagas_ocupadas", nullable = false)
    private Integer vagasOcupadas = 0;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusTurma status = StatusTurma.ABERTA;

    @Transient
    public int getVagasDisponiveis() {
        return vagasTotais - vagasOcupadas;
    }
}
