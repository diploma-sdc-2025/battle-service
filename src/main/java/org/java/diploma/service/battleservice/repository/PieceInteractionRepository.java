package org.java.diploma.service.battleservice.repository;

import org.java.diploma.service.battleservice.entity.PieceInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PieceInteractionRepository extends JpaRepository<PieceInteraction, Integer> {}
