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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
public class EcosApiService {

    private static final String ITEM_URL =
            "https://ecos.bok.or.kr/api/StatisticItemList/{apiKey}/json/kr/1/1000/722Y001";
    private static final String API_URL =
            "https://ecos.bok.or.kr/api/StatisticSearch/{apiKey}/json/kr/1/1/722Y001/D/{startDate}/{endDate}/0101000";
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
        EconomicIndicator indicator;
        try {
            String metadataResponse = restTemplate.getForObject(ITEM_URL, String.class, apiKey);
            JsonNode metadataRows = parseResponse(metadataResponse).path("StatisticItemList").path("row");
            if (!metadataRows.isArray() || metadataRows.isEmpty()) {
                throw new IllegalStateException("ECOS 응답에 통계 항목 배열이 없습니다.");
            }

            LocalDate latestDate = null;
            for (JsonNode item : metadataRows) {
                if ("0101000".equals(item.path("ITEM_CODE").asString(""))
                        && "D".equals(item.path("CYCLE").asString(""))) {
                    LocalDate endDate = LocalDate.parse(item.path("END_TIME").asString(""), DATE_FORMAT);
                    if (latestDate == null || endDate.isAfter(latestDate)) {
                        latestDate = endDate;
                    }
                }
            }
            if (latestDate == null) {
                throw new IllegalStateException("ECOS 기준금리의 최신 일자를 찾을 수 없습니다.");
            }

            String date = latestDate.format(DATE_FORMAT);
            String response = restTemplate.getForObject(API_URL, String.class, apiKey, date, date);
            JsonNode root = parseResponse(response);

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
            if (!baseDate.equals(latestDate)
                    || !"0101000".equals(row.path("ITEM_CODE1").asString(""))) {
                throw new IllegalStateException("ECOS 응답의 기준 일자 또는 항목 코드가 조회 조건과 다릅니다.");
            }

            indicator = EconomicIndicator.builder()
                    .indicatorType("BASE_RATE")
                    .value(value)
                    .baseDate(baseDate)
                    .build();
        } catch (JacksonException | IllegalArgumentException | DateTimeParseException e) {
            throw new IllegalStateException("ECOS 기준금리 응답 파싱에 실패했습니다.", e);
        }

        EconomicIndicator saved = economicIndicatorRepository.save(indicator);
        log.info("ECOS 기준금리 저장 완료: id={}, 기준일={}, 금리={}",
                saved.getId(), saved.getBaseDate(), saved.getValue());
    }

    private JsonNode parseResponse(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("ECOS API 응답이 비어 있습니다.");
        }
        JsonNode root = objectMapper.readTree(response);
        JsonNode result = root.path("RESULT");
        if (!result.isMissingNode()) {
            String code = result.path("CODE").asString("");
            String message = result.path("MESSAGE").asString("");
            throw new IllegalStateException("ECOS API 조회 실패: " + code + " - " + message);
        }
        return root;
    }
}
