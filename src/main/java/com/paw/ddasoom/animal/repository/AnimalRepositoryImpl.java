package com.paw.ddasoom.animal.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.paw.ddasoom.animal.domain.Animal;
import com.paw.ddasoom.animal.domain.AnimalGender;
import com.paw.ddasoom.animal.domain.AnimalKind;
import com.paw.ddasoom.animal.domain.QAnimal;
import com.paw.ddasoom.animal.dto.request.AnimalListPageRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AnimalRepositoryImpl implements AnimalRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  private static final QAnimal animal = QAnimal.animal;

  @Override
  public Page<Animal> search(AnimalListPageRequest request, Pageable pageable) {
    
    // 쿼리문의 조건(WHERE)을 한번에 처리함
    List<Animal> animals = queryFactory
      .selectFrom(animal)
      .where(
        kindEq(request.kind()),
        locationEq(request.location()),
        isFosteredEq(request.isFostered()),
        genderEq(request.gender())
      )
      .offset(pageable.getOffset())
      .limit(pageable.getPageSize())
      .fetch();

    long total = queryFactory
      .select(animal.count())
      .from(animal)
      .where(
        kindEq(request.kind()),
        locationEq(request.location()),
        isFosteredEq(request.isFostered()),
        genderEq(request.gender())
      )
      .fetchOne();

      return new PageImpl<>(animals, pageable, total);
  }

  // 조건 메서드 — null 반환 시 where절에서 자동 제거
  private BooleanExpression kindEq(AnimalKind kind) {
      return kind != null ? animal.kind.eq(kind) : null;
  }

  private BooleanExpression locationEq(String location) {
      return location != null ? animal.location.contains(location) : null;
  }

  private BooleanExpression isFosteredEq(Boolean isFostered) {
    return isFostered != null ? animal.isFostered.eq(isFostered) : null;
}

  private BooleanExpression genderEq(AnimalGender gender) {
      return gender != null ? animal.gender.eq(gender) : null;
  }
}
