package com.disasterHelp.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "desastres")
public class Desastre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private long id;

    @Column(nullable = false)
    @NotEmpty(message = "O tipo do desastre é obrigatório.")
    private String tipo; // Ex: "Enchente", "Terremoto", "Tempestade"

    @Column(nullable = false)
    @NotEmpty(message = "A descrição é obrigatória.")
    private String descricao;

    @Column(nullable = false)
    @NotEmpty(message = "A região afetada é obrigatória.")
    private String regiao;

    @Column(nullable = false)
    @NotEmpty(message = "A data prevista é obrigatória.")
    private String dataPrevista; // Ou use LocalDate se quiser como data mesmo

    // Relacionamento: cada desastre pode ter um usuário responsável pelo registro.
    // Gera a coluna FK "usuario_id" na tabela "desastres".
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    @ToString.Exclude
    private Usuario usuario;

    // Campos somente-leitura no JSON: mostram quem criou, sem expor a senha
    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getUsuarioId() {
        return usuario != null ? usuario.getId() : null;
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getResponsavel() {
        return usuario != null ? usuario.getNome() : null;
    }
}