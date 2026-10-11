package com.studymatch.repository;

import com.studymatch.model.PersistenciaTeste;
import org.springframework.data.jpa.repository.JpaRepository;

// Disponibiliza operações de base de dados como save() e findAll().
// O Spring Data JPA implementa automaticamente esta interface.
public interface PersistenciaTesteRepository
        extends JpaRepository<PersistenciaTeste, Long> {
}
