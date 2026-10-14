package com.example.overviewbe.repository;

import com.example.overviewbe.entity.EconomicIndicator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EconomicIndicatorRepository extends JpaRepository<EconomicIndicator, Long> {
}
