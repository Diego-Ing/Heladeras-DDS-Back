package com.utndds.heladerasApi.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.utndds.heladerasApi.models.Heladera.Heladera;

import jakarta.transaction.Transactional;

@Repository
public interface HeladeraRepository extends JpaRepository<Heladera, Long> {
    @SuppressWarnings("null")
    Optional<Heladera> findById(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Heladera h SET h.funcionando = true WHERE h.id > 0")
    void activarTodas();
}