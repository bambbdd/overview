package com.example.overviewbe.service;

import com.example.overviewbe.entity.EconomicIndicator;
import com.example.overviewbe.repository.EconomicIndicatorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
public class EcosApiService {

    private static final String API_URL =
            "https://ecos.bok.or.kr/api/StatisticSearch/{apiKey}/json/kr/1/1/731Y001/D/{startDate}/{endDate}/0101000";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final EconomicIndicatorRepository economicIndicatorRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${ecos.api.key}")
    private String apiKey;

    public EcosApiService(EconomicIndicatorRepository economicIndicatorRepository, ObjectMapper objectMapper) {
        this.economicIndicatorRepository = economicIndicatorRepository;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(10_000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public void fetchAndSaveBaseRate() {
        String date = LocalDate.now(ZoneId.of("Asia/Seoul")).format(DATE_FORMAT);
        String response = restTemplate.getForObject(API_URL, String.class, apiKey, date, date);
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("ECOS API 응답이 비어 있습니다.");
        }

        EconomicIndicator indicator;
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode result = root.path("RESULT");
            if (!result.isMissingNode()) {
                String code = result.path("CODE").asString("");
                if ("INFO-200".equals(code)) {
                    log.info("ECOS 기준금리 조회 결과가 없습니다. 기준일: {}", date);
                    return;
                }
                throw new IllegalStateException("ECOS API 오류 코드: " + code);
            }

            JsonNode rows = root.path("StatisticSearch").path("row");
            if (!rows.isArray() || rows.isEmpty()) {
                throw new IllegalStateException("ECOS 응답에 통계 데이터 배열이 없습니다.");
            }

            JsonNode row = rows.get(0);
            double value = Double.parseDouble(row.path("DATA_VALUE").asString(""));
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("금리 수치가 유효하지 않습니다.");
            }
            LocalDate baseDate = LocalDate.parse(row.path("TIME").asString(""), DATE_FORMAT);

            indicator = EconomicIndicator.builder()
                    .indicatorType("BASE_RATE")
                    .value(value)
                    .baseDate(baseDate)
                    .build();
        } catch (JacksonException | IllegalArgumentException | DateTimeParseException e) {
            throw new IllegalStateException("ECOS 기준금리 응답 파싱에 실패했습니다.", e);
        }

        economicIndicatorRepository.save(indicator);
    }
}
