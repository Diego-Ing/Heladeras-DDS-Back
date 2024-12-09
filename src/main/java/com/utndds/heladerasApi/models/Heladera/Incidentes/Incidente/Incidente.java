package com.utndds.heladerasApi.models.Heladera.Incidentes.Incidente;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

import com.utndds.heladerasApi.models.Heladera.Heladera;
import com.utndds.heladerasApi.models.Heladera.Incidentes.VisitaTecnico;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha")
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heladera") // Nombre de la columna de la FK en la tabla Incidente
    protected Heladera heladera;

    @OneToMany(mappedBy = "incidente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VisitaTecnico> visitas = new ArrayList<>();

    // Constructor vacío para JPA
    public Incidente() {
    }

    public Incidente(Heladera heladera) {
        this.fecha = LocalDate.now();
        this.heladera = heladera;

        this.desactivarHeladera();
    }

    private void desactivarHeladera() {
        this.heladera.setFuncionando(false);
        this.heladera.verificarSuscripciones();
    }
}
