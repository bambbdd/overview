package com.example.overviewbe.client;

import com.example.overviewbe.dto.NaverNewsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class NaverApiClient {

    private final RestClient restClient;
    private final String newsUrl;

    public NaverApiClient(
            @Value("${naver.api.client-id}") String clientId,
            @Value("${naver.api.client-secret}") String clientSecret,
            @Value("${naver.api.news-url}") String newsUrl
    ) {
        this.newsUrl = newsUrl;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(10_000);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("X-Naver-Client-Id", clientId)
                .defaultHeader("X-Naver-Client-Secret", clientSecret)
                .build();
    }

    public NaverNewsResponse searchNews(String query) {
        return searchNews(query, 100, 1, "date");
    }

    public NaverNewsResponse searchNews(String query, int display, int start, String sort) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("뉴스 검색어는 비어 있을 수 없습니다.");
        }
        if (display < 1 || display > 100 || start < 1 || start > 1000) {
            throw new IllegalArgumentException("display는 1~100, start는 1~1000이어야 합니다.");
        }
        if (!"date".equals(sort) && !"sim".equals(sort)) {
            throw new IllegalArgumentException("sort는 date 또는 sim이어야 합니다.");
        }

        // URI 변수를 확장하면서 한글 및 &, + 등의 검색어 문자를 한 번만 인코딩한다.
        URI uri = UriComponentsBuilder.fromUriString(newsUrl)
                .queryParam("query", "{query}")
                .queryParam("display", display)
                .queryParam("start", start)
                .queryParam("sort", sort)
                .encode()
                .buildAndExpand(query.strip())
                .toUri();

        try {
            NaverNewsResponse response = restClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(NaverNewsResponse.class);
            if (response == null || response.items() == null) {
                throw new IllegalStateException("네이버 뉴스 API 응답 또는 items가 비어 있습니다.");
            }
            return response;
        } catch (RestClientResponseException e) {
            // 인증 헤더나 응답 본문은 오류 메시지에 포함하지 않는다.
            throw new IllegalStateException("네이버 뉴스 API 호출 실패: HTTP " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new IllegalStateException("네이버 뉴스 API 통신 또는 응답 변환에 실패했습니다.", e);
        }
    }
}
