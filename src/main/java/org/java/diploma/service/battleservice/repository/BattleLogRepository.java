package org.java.diploma.service.battleservice.repository;

import org.java.diploma.service.battleservice.entity.BattleLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleLogRepository extends JpaRepository<BattleLog, Integer> {}
