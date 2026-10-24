package com.example.overviewbe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "economic_news", indexes = {
        @Index(name = "idx_economic_news_pub_date", columnList = "pub_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EconomicNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false, columnDefinition = "text")
    private String link;

    // 원문 URL이 없는 응답은 link를 대체 키로 사용한다.
    @Column(name = "original_link", nullable = false, unique = true, length = 2048)
    private String originalLink;

    /** Asia/Seoul 기준 발행 시각. */
    @Column(name = "pub_date", nullable = false)
    private LocalDateTime pubDate;

    @Builder
    public EconomicNews(String title, String description, String link,
                        String originalLink, LocalDateTime pubDate) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.originalLink = originalLink;
        this.pubDate = pubDate;
    }
}
