package com.disasterHelp.repository;

import com.disasterHelp.model.Desastre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesastreRepository extends JpaRepository<Desastre, Long> {
}