package colegio.api.repository;

import colegio.api.model.SolicitudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudRepository extends JpaRepository<SolicitudEntity, Integer> {}