package com.example.overviewbe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "economic_indicators")
@Getter
@NoArgsConstructor
public class EconomicIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "indicator_type", nullable = false)
    private String indicatorType; // 지표 종류

    @Column(name = "indicator_value", nullable = false)
    private Double value; // 실제 수치

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate; // 기준 일자

    // 데이터 저장용 생성자
    @Builder
    public EconomicIndicator(String indicatorType, Double value, LocalDate baseDate) {
        this.indicatorType = indicatorType;
        this.value = value;
        this.baseDate = baseDate;
    }
}
