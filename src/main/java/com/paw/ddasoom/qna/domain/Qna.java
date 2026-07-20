package com.paw.ddasoom.qna.domain;

import java.time.LocalDateTime;

import com.paw.ddasoom.common.util.BaseTimeEntity;
import com.paw.ddasoom.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "qna")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// ==========================
// [대화형 스레드 구조]
// ==========================
public class Qna extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questioner_id", nullable = false)
    private Member questioner; // 질문자(유저)

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /* [QnA 생성] - 기본 상태: PENDING, 관리자 답변 시 ANSWERED로 업데이트 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QnaStatus status = QnaStatus.PENDING;

    /* [유저가 비밀글로 설정하지 않은 채 욕설/비방을 올린 경우 관리자가 숨김 처리] */
    @Column(nullable = false)
    private Boolean isVisible=true;

    /* [최초 응대 시각] */
    @Column(columnDefinition = "DATETIME(6)")
    private LocalDateTime answeredAt;

    /* [삭제된 시각 기록] - Soft Delete 사용, 데이터 복구&이력 관리 가능 */
    @Column(columnDefinition = "DATETIME(6)")
    private LocalDateTime deletedAt;

    @Builder
    public Qna(Member questioner, String title, String content) {
        this.questioner = questioner;
        this.title = title;
        this.content = content;
    }

    // =========================================================================
    // 비즈니스 메서드 (의도가 명확한 상태 변경 행위)
    // 무분별한 Setter를 배제하고, 객체 스스로가 자신의 상태를 변경하도록 도메인 로직 응집
    // =========================================================================

    /* [QnA 답변 작성 시 상태 변경 + 답변 일시 저장] */
    public void markAnswered() {
        this.status = QnaStatus.ANSWERED;
        if (this.answeredAt == null) {
            this.answeredAt = LocalDateTime.now(); // 최초 답변 시각 고정 (재답변 시 갱신하지 않음 — 최초 응대 지표 보존)
        }
    }

    /* [유저 재질문 코멘트 추가 → 답변 대기] */
    public void markPending() {
        this.status = QnaStatus.PENDING;
    }

    /* [QnA 노출 여부 변경] */
    public void changeVisibility(boolean isVisible) {
        this.isVisible = isVisible;
    }

    /* [QnA 논리 삭제 처리] */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /* [QnA 삭제 상태 확인] */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
  }