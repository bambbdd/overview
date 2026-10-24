package com.example.overviewbe.repository;

import com.example.overviewbe.entity.EconomicNews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EconomicNewsRepository extends JpaRepository<EconomicNews, Long> {

    boolean existsByOriginalLink(String originalLink);
}
