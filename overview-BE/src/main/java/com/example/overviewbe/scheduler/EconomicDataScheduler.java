package com.example.overviewbe.scheduler;

import com.example.overviewbe.service.EconomicNewsService;
import com.example.overviewbe.service.EcosApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EconomicDataScheduler {

    private final EcosApiService ecosApiService;
    private final EconomicNewsService economicNewsService;

    // 초·분·시·일·월·요일 순서
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void collectDailyEconomicData() {
        log.info("자정 경제 데이터 수집 시작");

        boolean baseRateSucceeded = false;
        try {
            ecosApiService.fetchAndSaveBaseRate();
            baseRateSucceeded = true;
        } catch (Exception e) {
            log.error("자정 기준금리 수집 실패", e);
        }

        boolean newsSucceeded = false;
        try {
            int savedCount = economicNewsService.fetchAndSaveEconomicNews();
            newsSucceeded = true;
            log.info("자정 경제 뉴스 신규 저장: {}건", savedCount);
        } catch (Exception e) {
            log.error("자정 경제 뉴스 수집 실패", e);
        }

        log.info("자정 경제 데이터 수집 종료: 기준금리 성공={}, 뉴스 성공={}",
                baseRateSucceeded, newsSucceeded);
    }
}
