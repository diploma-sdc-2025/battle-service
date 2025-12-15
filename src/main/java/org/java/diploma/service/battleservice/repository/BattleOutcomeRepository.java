package org.java.diploma.service.battleservice.repository;

import org.java.diploma.service.battleservice.entity.BattleOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleOutcomeRepository extends JpaRepository<BattleOutcome, Integer> {}
