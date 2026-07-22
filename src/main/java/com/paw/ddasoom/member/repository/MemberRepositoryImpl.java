package com.paw.ddasoom.member.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import com.paw.ddasoom.member.domain.Member;
import com.paw.ddasoom.member.domain.MemberStatus;
import com.paw.ddasoom.member.domain.QMember;
import com.paw.ddasoom.member.domain.Role;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 회원 검색 QueryDSL 구현.
 *
 * <p><b>왜 JPQL에서 옮겼나</b> — 화면의 "상태" 정렬(활성/숨김/탈퇴)은 컬럼 하나가 아니라
 * {@code deletedAt} 유무와 {@code status}를 조합한 <b>파생값</b>이다. Spring Data의 {@code Sort}는
 * "컬럼명 + 방향"만 표현할 수 있어 CASE 식을 담지 못한다. QueryDSL은 CASE를 정렬식으로 쓸 수 있어
 * 파생 정렬과 서버 페이징을 동시에 만족시킨다.
 *
 * <p>또한 기존 JPQL에는 {@code ORDER BY m.createdAt DESC}가 하드코딩돼 있어, Pageable로 정렬을 넘겨도
 * 뒤에 덧붙는 2차 정렬이 되어 사실상 무시됐다. 여기서는 정렬을 전적으로 Pageable이 결정한다.
 *
 * <p>⚠️ 클래스명은 반드시 {@code MemberRepositoryCustom} + "Impl" 이어야 한다 —
 * Spring Data가 이 규칙으로 구현체를 찾는다. 이름이 다르면 기동 시 빈을 찾지 못해 실패한다.
 */
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Member> searchForAdmin(String keyword, Role role, String status, Pageable pageable) {
        QMember member = QMember.member;
        BooleanBuilder where = buildCondition(keyword, role, status);

        List<Member> content = queryFactory
                .selectFrom(member)
                .where(where)
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트는 별도 쿼리 — 목록과 조건은 공유하되 정렬·페이징은 붙이지 않는다.
        // (조건을 빠뜨리면 "검색 결과 3건인데 페이지가 12개"인 상태가 된다)
        Long total = queryFactory
                .select(member.count())
                .from(member)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    /** 검색·필터 조건 조립 — 값이 없는 조건은 아예 붙이지 않아 "미지정 = 전체"가 되게 한다 */
    private BooleanBuilder buildCondition(String keyword, Role role, String status) {
        QMember member = QMember.member;
        BooleanBuilder where = new BooleanBuilder();

        // 빈 문자열도 "조건 없음"으로 취급 — 검색창을 비웠을 때 전체가 나와야 한다
        if (StringUtils.hasText(keyword)) {
            where.and(member.email.containsIgnoreCase(keyword)
                    .or(member.nickname.containsIgnoreCase(keyword)));
        }
        if (role != null) {
            where.and(member.role.eq(role));
        }
        // 상태 3분류 — 탈퇴(deletedAt)가 status 컬럼보다 우선한다.
        // 프론트의 파생 규칙(deletedAt ? 탈퇴 : status==HIDDEN ? 숨김 : 활성)과 동일하게 맞춘 것.
        if (StringUtils.hasText(status)) {
            switch (status) {
                case "ACTIVE" -> where.and(member.deletedAt.isNull())
                                      .and(member.status.eq(MemberStatus.ACTIVE));
                case "HIDDEN" -> where.and(member.deletedAt.isNull())
                                      .and(member.status.eq(MemberStatus.HIDDEN));
                case "DELETED" -> where.and(member.deletedAt.isNotNull());
                default -> { }   // 정의되지 않은 값은 조건 미적용(전체) — 400을 던지지 않고 안전하게 무시
            }
        }
        return where;
    }

    /**
     * Pageable의 Sort를 QueryDSL 정렬식으로 변환.
     * 도착하는 프로퍼티는 PageableSanitizer가 이미 화이트리스트로 걸렀지만,
     * 여기서도 매핑되지 않은 값은 조용히 무시해 이중으로 방어한다.
     */
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        QMember member = QMember.member;
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order o : sort) {
            Order direction = o.isAscending() ? Order.ASC : Order.DESC;
            switch (o.getProperty()) {
                case "id" -> orders.add(new OrderSpecifier<>(direction, member.id));
                case "email" -> orders.add(new OrderSpecifier<>(direction, member.email));
                case "nickname" -> orders.add(new OrderSpecifier<>(direction, member.nickname));
                case "role" -> orders.add(new OrderSpecifier<>(direction, member.role));
                case "createdAt" -> orders.add(new OrderSpecifier<>(direction, member.createdAt));
                case "status" -> orders.add(new OrderSpecifier<>(direction, statusOrder()));
                default -> { }
            }
        }
        // 정렬 지정이 없으면 가입일 최신순 — 기존 JPQL의 기본 동작을 유지한다
        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier<>(Order.DESC, member.createdAt));
        }
        return orders.toArray(new OrderSpecifier[0]);
    }

    /**
     * 화면의 "상태" 정렬용 파생 표현식 — 활성(0) → 숨김(1) → 탈퇴(2).
     * 오름차순일 때 문제 있는 계정이 뒤로 모이도록 순번을 잡았다.
     */
    private NumberExpression<Integer> statusOrder() {
        QMember member = QMember.member;
        return new CaseBuilder()
                .when(member.deletedAt.isNotNull()).then(2)
                .when(member.status.eq(MemberStatus.HIDDEN)).then(1)
                .otherwise(0);
    }
}