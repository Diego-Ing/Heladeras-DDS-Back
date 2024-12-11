package com.utndds.heladerasApi.models.Heladera;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.utndds.heladerasApi.models.Heladera.Incidentes.Incidente.Incidente;
import com.utndds.heladerasApi.models.Heladera.Sensores.Sensor;
import com.utndds.heladerasApi.models.Observer.ObservadorHeladera;
import com.utndds.heladerasApi.models.Suscripciones.Suscripcion;

import lombok.Setter;
import lombok.Getter;

import jakarta.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "Heladera")
public class Heladera implements ObservadorHeladera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "punto")
    private Punto punto;

    @Column(name = "capacidad")
    int capacidad;

    @Column(name = "fecha_inicio_funcionamiento")
    LocalDate fechaInicioFuncionamiento;

    @Column(name = "funcionando")
    boolean funcionando;

    @Column(name = "cant_viandas")
    int cantViandas;

    @Column(name = "temp_min")
    Double tempMin;

    @Column(name = "temp_max")
    Double tempMax;

    @OneToMany(mappedBy = "heladera", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Suscripcion> suscriptores = new ArrayList<>();

    @OneToMany(mappedBy = "heladera", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Incidente> incidentes = new ArrayList<>();

    @OneToMany(mappedBy = "heladera", cascade = CascadeType.ALL, fetch = FetchType.LAZY) // Nueva relación para sensores
    private List<Sensor> sensores = new ArrayList<>(); // Se agrega la lista de sensores

    public Heladera() {
    };

    public Heladera(Punto punto, int capacidad, Double tempMax, Double tempMin, boolean funcionando,
            boolean abierta, LocalDate fechaInicioFuncionamiento) {
        this.punto = punto;
        this.capacidad = capacidad;
        this.funcionando = funcionando;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.fechaInicioFuncionamiento = fechaInicioFuncionamiento;
        this.cantViandas = 0;
    }

    @Override
    public void actualizarTemperatura(double temperatura) {
        if (temperatura < this.tempMin || temperatura > this.tempMax) {
            this.funcionando = false;
        }
    }

    public int cantMesesActiva() {
        LocalDate hoy = LocalDate.now();
        Period periodo = Period.between(fechaInicioFuncionamiento, hoy);
        int cantMeses = periodo.getYears() * 12 + periodo.getMonths();
        return cantMeses;
    }

    public void agregarVianda() {
        if (cantViandas < capacidad) {
            this.cantViandas += 1;
            this.verificarSuscripciones();
        }
    }

    public void extraerVianda() {
        if (cantViandas > 0) {
            this.cantViandas -= 1;
            this.verificarSuscripciones();
        }
    }

    public void sacarViandas(int cantidad) {
        this.cantViandas -= cantidad;
        this.verificarSuscripciones();
    }

    public void agregarViandas(int cantidad) {
        this.cantViandas += cantidad;
        this.verificarSuscripciones();
    }

    public void verificarSuscripciones() {

        for (Suscripcion suscripcion : suscriptores) {

            suscripcion.verificarNotificaciones();
        }
    }

    /// Esto es xq no me detecta los getters de ciertos atributos

    public int cantFallas() {
        return this.incidentes.size();
    }

    public int cantViandas() {
        return this.cantViandas;
    }

    public boolean estaFuncionando() {
        return this.funcionando;
    }

}
