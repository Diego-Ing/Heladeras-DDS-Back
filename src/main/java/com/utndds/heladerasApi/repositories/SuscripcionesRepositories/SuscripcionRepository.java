package com.utndds.heladerasApi.repositories.SuscripcionesRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.utndds.heladerasApi.models.Heladera.Heladera;
import com.utndds.heladerasApi.models.Rol.Colaborador;
import com.utndds.heladerasApi.models.Suscripciones.Suscripcion;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    Suscripcion findByColaboradorAndHeladera(Colaborador colaborador, Heladera heladera);
    
}