package com.example.overviewbe.service;

import com.example.overviewbe.client.NaverApiClient;
import com.example.overviewbe.dto.NaverNewsResponse;
import com.example.overviewbe.entity.EconomicNews;
import com.example.overviewbe.repository.EconomicNewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class EconomicNewsService {

    private static final ZoneId NEWS_ZONE = ZoneId.of("Asia/Seoul");

    private final NaverApiClient naverApiClient;
    private final EconomicNewsRepository economicNewsRepository;
    private final TransactionTemplate transactionTemplate;

    public EconomicNewsService(NaverApiClient naverApiClient,
                               EconomicNewsRepository economicNewsRepository,
                               PlatformTransactionManager transactionManager) {
        this.naverApiClient = naverApiClient;
        this.economicNewsRepository = economicNewsRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /** 최신 경제 뉴스 최대 100건을 수집하고 신규 저장 건수를 반환한다. */
    public int fetchAndSaveEconomicNews() {
        return fetchAndSaveEconomicNews("경제");
    }

    public int fetchAndSaveEconomicNews(String query) {
        NaverNewsResponse response = naverApiClient.searchNews(query);
        List<EconomicNews> news = new ArrayList<>();
        Set<String> originalLinks = new HashSet<>();
        int skipped = 0;

        for (NaverNewsResponse.Item item : response.items()) {
            try {
                EconomicNews article = toEntity(item);
                if (originalLinks.add(article.getOriginalLink())) {
                    news.add(article);
                }
            } catch (IllegalArgumentException | DateTimeParseException e) {
                skipped++;
                log.warn("네이버 뉴스 항목 제외: {}", e.getMessage());
            }
        }

        // 외부 HTTP 호출과 정제가 끝난 후에만 DB 트랜잭션을 시작한다.
        Integer saved = transactionTemplate.execute(status -> {
            List<EconomicNews> newArticles = news.stream()
                    .filter(article -> !economicNewsRepository.existsByOriginalLink(article.getOriginalLink()))
                    .toList();
            return economicNewsRepository.saveAll(newArticles).size();
        });
        if (saved == null) {
            throw new IllegalStateException("경제 뉴스 저장 결과가 없습니다.");
        }
        log.info("경제 뉴스 수집 완료: 수신={}, 신규 저장={}, 유효하지 않은 항목={}",
                response.items().size(), saved, skipped);
        return saved;
    }

    private EconomicNews toEntity(NaverNewsResponse.Item item) {
        if (item == null) {
            throw new IllegalArgumentException("뉴스 항목이 null입니다.");
        }
        String title = cleanText(item.title());
        String link = item.link() == null ? "" : item.link().strip();
        String originalLink = item.originallink() == null ? "" : item.originallink().strip();
        if (originalLink.isBlank()) {
            originalLink = link;
        }
        if (link.isBlank()) {
            link = originalLink;
        }
        if (title.isBlank() || originalLink.isBlank()
                || item.pubDate() == null || item.pubDate().isBlank()) {
            throw new IllegalArgumentException("뉴스 제목, URL 또는 발행 시각이 없습니다.");
        }
        if (originalLink.length() > 2048) {
            throw new IllegalArgumentException("뉴스 원문 URL이 저장 길이를 초과했습니다.");
        }

        return EconomicNews.builder()
                .title(title)
                .description(cleanText(item.description()))
                .link(link)
                .originalLink(originalLink)
                .pubDate(ZonedDateTime.parse(item.pubDate().strip(), DateTimeFormatter.RFC_1123_DATE_TIME)
                        .withZoneSameInstant(NEWS_ZONE)
                        .toLocalDateTime())
                .build();
    }

    private String cleanText(String html) {
        // text()는 태그 제거와 함께 &quot;, &amp;, 숫자 엔티티도 디코딩한다.
        return html == null ? "" : Jsoup.parseBodyFragment(html).text();
    }
}
