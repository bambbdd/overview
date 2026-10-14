package com.example.demo;

import com.example.overviewbe.entity.EconomicIndicator;
import com.example.overviewbe.repository.EconomicIndicatorRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.example.demo", "com.example.overviewbe"})
@EntityScan(basePackageClasses = EconomicIndicator.class)
@EnableJpaRepositories(basePackageClasses = EconomicIndicatorRepository.class)
public class OverviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(OverviewApplication.class, args);
	}

}
