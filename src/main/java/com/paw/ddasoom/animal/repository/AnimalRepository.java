package com.paw.ddasoom.animal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paw.ddasoom.animal.domain.Animal;

public interface AnimalRepository extends JpaRepository<Animal, Long>, AnimalRepositoryCustom {
  Optional<Animal> findByAbandonmentId(String abandonmentId);
  // 메인 미리보기 — 최근 등록(PK 내림차순) 4건
  List<Animal> findTop4ByOrderByIdDesc();
}
