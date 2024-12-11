package com.utndds.heladerasApi.services.Colaboraciones;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utndds.heladerasApi.DTOs.DistribucionViandasDTO;
import com.utndds.heladerasApi.models.Colaboraciones.DistribucionViandas;
import com.utndds.heladerasApi.models.Enums.MotivoApertura;
import com.utndds.heladerasApi.models.Heladera.Heladera;
import com.utndds.heladerasApi.models.Rol.Colaborador;
import com.utndds.heladerasApi.repositories.ColaboradorRepository;
import com.utndds.heladerasApi.repositories.HeladeraRepository;
import com.utndds.heladerasApi.repositories.ColaboracionesRepositories.DistribucionViandasRepository;
import com.utndds.heladerasApi.services.AperturaService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DistribucionViandasService {

        @Autowired
        private DistribucionViandasRepository distribucionViandasRepository;

        @Autowired
        private AperturaService solicitudAperturaService;

        @Autowired
        private ColaboradorRepository colaboradorRepository;

        @Autowired
        private HeladeraRepository heladeraRepository;

        public void guardarDistribucionViandas(String colaboradorUUID, DistribucionViandasDTO distribucionViandasDTO) {
                Colaborador colaborador = colaboradorRepository.findByUUID(colaboradorUUID)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Colaborador no encontrado con uuid " + colaboradorUUID));

                Heladera heladeraOrigen = heladeraRepository.findById(distribucionViandasDTO.getHeladeraOrigenId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Heladera no encontrada con id "
                                                                + distribucionViandasDTO.getHeladeraOrigenId()));
                Heladera heladeraDestino = heladeraRepository.findById(distribucionViandasDTO.getHeladeraDestinoId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Heladera no encontrada con id "
                                                                + distribucionViandasDTO.getHeladeraDestinoId()));
                if (heladeraOrigen.getId().equals(heladeraDestino.getId())) {
                        throw new IllegalArgumentException("La heladera de origen y destino no pueden ser la misma");
                }
                if (heladeraOrigen.getCantViandas() < distribucionViandasDTO.getCantidadViandasAMover()) {

                        throw new IllegalArgumentException("La heladera de origen no tiene suficientes viandas");
                } else {
                        heladeraOrigen.sacarViandas(distribucionViandasDTO.getCantidadViandasAMover());
                        heladeraDestino.agregarViandas(distribucionViandasDTO.getCantidadViandasAMover());
                        DistribucionViandas distribucionViandas = new DistribucionViandas();
                        distribucionViandas.setColaborador(colaborador);
                        distribucionViandas.setHeladeraOrigen(heladeraOrigen);
                        distribucionViandas.setHeladeraDestino(heladeraDestino);
                        distribucionViandas.setCantidadViandasAMover(distribucionViandasDTO.getCantidadViandasAMover());
                        distribucionViandas.setMotivo(distribucionViandasDTO.getMotivo());

                        distribucionViandasRepository.save(distribucionViandas);

                        // Crear la solicitud de apertura automáticamente
                        solicitudAperturaService.crearSolicitud(colaboradorUUID, heladeraDestino.getId(),
                                        MotivoApertura.DISTRIBUCION);
                }

        }
}