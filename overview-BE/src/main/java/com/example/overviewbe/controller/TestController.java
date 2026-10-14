package com.example.overviewbe.controller;

import com.example.overviewbe.service.EcosApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final EcosApiService ecosApiService;

    @GetMapping("/test/ecos")
    public String testEcos() {
        ecosApiService.fetchAndSaveBaseRate();
        return "ECOS 데이터 적재 완료!";
    }
}
