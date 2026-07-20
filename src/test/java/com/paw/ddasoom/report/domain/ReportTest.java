package com.paw.ddasoom.report.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.paw.ddasoom.report.exception.ReportException;

class ReportTest {

    private Report newReport() {
        // 처리자/신고자 연관은 검증 대상 아님 → null
        return Report.builder()
                .reporter(null)
                .targetType(ReportTargetType.POST)
                .targetId(1L)
                .reason(ReportReason.SPAM)
                .content(null)
                .build();
    }

    @Test
    @DisplayName("생성 직후 상태는 PENDING")
    void 생성_초기상태() {
        assertThat(newReport().getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("approve는 APPROVED로 전이하고 processedAt을 채운다")
    void 승인_상태전이() {
        Report report = newReport();
        report.approve(null);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.APPROVED);
        assertThat(report.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("reject는 REJECTED로 전이한다")
    void 반려_상태전이() {
        Report report = newReport();
        report.reject(null);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
    }

    @Test
    @DisplayName("이미 처리된 신고 재처리 시 REPORT_ALREADY_PROCESSED (멱등 보호)")
    void 중복처리_방지() {
        Report report = newReport();
        report.approve(null);
        assertThatThrownBy(() -> report.reject(null))
                .isInstanceOf(ReportException.class);
    }
}