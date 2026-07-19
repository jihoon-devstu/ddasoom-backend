package com.paw.ddasoom.report.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.ddasoom.report.service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  // 로그인 필수 — PUBLIC_URIS 미등록 = anyRequest authenticated가 커버 (기본 잠금)

}
