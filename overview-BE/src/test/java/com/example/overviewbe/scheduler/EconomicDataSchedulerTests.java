package com.example.overviewbe.scheduler;

import com.example.overviewbe.service.EconomicNewsService;
import com.example.overviewbe.service.EcosApiService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EconomicDataSchedulerTests {

    private final EcosApiService ecosApiService = mock(EcosApiService.class);
    private final EconomicNewsService economicNewsService = mock(EconomicNewsService.class);
    private final EconomicDataScheduler scheduler =
            new EconomicDataScheduler(ecosApiService, economicNewsService);

    @Test
    void collectsBothSources() {
        when(economicNewsService.fetchAndSaveEconomicNews()).thenReturn(3);

        scheduler.collectDailyEconomicData();

        verify(ecosApiService).fetchAndSaveBaseRate();
        verify(economicNewsService).fetchAndSaveEconomicNews();
    }

    @Test
    void collectsNewsEvenWhenBaseRateFails() {
        doThrow(new IllegalStateException("ECOS unavailable"))
                .when(ecosApiService).fetchAndSaveBaseRate();

        assertDoesNotThrow(scheduler::collectDailyEconomicData);

        verify(economicNewsService).fetchAndSaveEconomicNews();
    }

    @Test
    void handlesNewsFailureAfterCollectingBaseRate() {
        when(economicNewsService.fetchAndSaveEconomicNews())
                .thenThrow(new IllegalStateException("Naver unavailable"));

        assertDoesNotThrow(scheduler::collectDailyEconomicData);

        verify(ecosApiService).fetchAndSaveBaseRate();
    }
}
