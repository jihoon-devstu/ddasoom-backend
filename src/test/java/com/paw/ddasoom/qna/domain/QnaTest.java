package com.paw.ddasoom.qna.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QnaTest {

    private Qna newQna() {
        // 상태 전이만 검증 → questioner 연관은 불필요
        return Qna.builder().questioner(null).title("문의 제목").content("문의 내용").build();
    }

    @Test
    @DisplayName("생성 직후 상태는 PENDING, answeredAt은 null")
    void 생성_초기상태() {
        Qna qna = newQna();
        assertThat(qna.getStatus()).isEqualTo(QnaStatus.PENDING);
        assertThat(qna.getAnsweredAt()).isNull();
    }

    @Test
    @DisplayName("markAnswered는 ANSWERED로 전이하고 answeredAt을 채운다")
    void 답변_상태전이() {
        Qna qna = newQna();
        qna.markAnswered();
        assertThat(qna.getStatus()).isEqualTo(QnaStatus.ANSWERED);
        assertThat(qna.getAnsweredAt()).isNotNull();
    }

    @Test
    @DisplayName("markAnswered 반복 호출해도 answeredAt은 최초 시각으로 고정 (A-1 회귀 방지)")
    void answeredAt_최초고정() {
        Qna qna = newQna();
        qna.markAnswered();
        LocalDateTime first = qna.getAnsweredAt();
        qna.markAnswered();
        assertThat(qna.getAnsweredAt()).isEqualTo(first);
    }

    @Test
    @DisplayName("유저 재질문(markPending)은 PENDING으로 되돌리되 answeredAt은 유지 (A-1 회귀 방지)")
    void 재질문_대기복귀() {
        Qna qna = newQna();
        qna.markAnswered();
        LocalDateTime answeredAt = qna.getAnsweredAt();

        qna.markPending();

        assertThat(qna.getStatus()).isEqualTo(QnaStatus.PENDING);
        assertThat(qna.getAnsweredAt()).isEqualTo(answeredAt);
    }
}