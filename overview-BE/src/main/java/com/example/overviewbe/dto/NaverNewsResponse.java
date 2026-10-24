package com.example.overviewbe.dto;

import java.util.List;

/** 네이버 뉴스 검색 API의 원본 응답. 날짜와 HTML 정제는 서비스에서 수행한다. */
public record NaverNewsResponse(
        String lastBuildDate,
        int total,
        int start,
        int display,
        List<Item> items
) {
    public record Item(
            String title,
            String originallink,
            String link,
            String description,
            String pubDate
    ) {
    }
}
