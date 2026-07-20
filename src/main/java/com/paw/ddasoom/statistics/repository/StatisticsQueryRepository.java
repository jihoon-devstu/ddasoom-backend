package com.paw.ddasoom.statistics.repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.paw.ddasoom.foster.domain.FosterStatus;
import com.paw.ddasoom.foster.domain.QFoster;
import com.paw.ddasoom.qna.domain.QQna;
import com.paw.ddasoom.qna.domain.QnaStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * 대시보드·통계 전용 집계 쿼리의 단일 창구.
 * 타 도메인(foster/qna/animal) Repository에 집계 메서드를 흩뿌리지 않기 위해 한 곳에 모은다 — 팀 결정.
 * 읽기 전용 집계만 담당하며, 타 도메인 엔티티에 대한 쓰기는 절대 하지 않는다 (단방향 읽기 의존만 허용).
 * dashboard 도메인도 이 저장소를 공유한다 (두 도메인 모두 집계 소비자 — 담당: 지훈).
 */
@Repository
@RequiredArgsConstructor
public class StatisticsQueryRepository {

  private final JPAQueryFactory queryFactory;

    /** 임보 신청 상태별 현재 분포 — 심사대기 카운트와 상태 분포 차트가 공유하는 단일 쿼리 */
    public Map<FosterStatus, Long> countFostersByStatus() {
        QFoster foster = QFoster.foster;
        return queryFactory
                .select(foster.status, foster.count())
                .from(foster)
                .where(foster.deletedAt.isNull())   // 활성 신청만 (soft delete 제외)
                .groupBy(foster.status)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(foster.status),
                        tuple -> Optional.ofNullable(tuple.get(foster.count())).orElse(0L)));
    }

    /**
     * 만료 임박 임보 건수 — [from, toExclusive) 반개구간이라 호출부가 구간을 이어 붙여도 이중 카운트가 없다.
     * 진행 중(FOSTERING/EXTENDED) 신청만 대상 — 종료/거절/대기 건은 "만료"라는 개념이 없음.
     */
    public long countExpiringFosters(LocalDateTime from, LocalDateTime toExclusive) {
        QFoster foster = QFoster.foster;
        Long count = queryFactory
                .select(foster.count())
                .from(foster)
                .where(foster.deletedAt.isNull(),
                        foster.status.in(FosterStatus.FOSTERING, FosterStatus.EXTENDED),
                        foster.fosterEndAt.goe(from),
                        foster.fosterEndAt.lt(toExclusive))
                .fetchOne();
        return count != null ? count : 0L;
    }

    /** 답변 대기 QnA 건수 (Qna는 soft delete 미적용 도메인이라 deletedAt 조건 없음) */
    public long countPendingQnas() {
        QQna qna = QQna.qna;
        Long count = queryFactory
                .select(qna.count())
                .from(qna)
                .where(qna.status.eq(QnaStatus.PENDING))
                .fetchOne();
        return count != null ? count : 0L;
    }
}
