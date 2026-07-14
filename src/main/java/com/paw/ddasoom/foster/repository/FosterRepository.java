package com.paw.ddasoom.foster.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paw.ddasoom.foster.domain.Foster;
import com.paw.ddasoom.foster.domain.FosterStatus;

public interface FosterRepository extends JpaRepository<Foster, Long> {

  /** 유저 수정 조회(유저 본인 + fosterId 조회)*/
  Optional<Foster> findByFosterIdAndUser_IdAndDeletedAtIsNull(Long fosterId, Long userId);
  
  /** 유저 리스트 조회( 유저ID.상태값 필터링, 삭제 미포함, 신청일 정렬 )
   * ex)
   * status = null //전체 조회
   * status = 'PENDING' //PENDING만 조회
   */
  @Query("""
      select f
      from Foster f
      where f.user.id = :memberId
        and f.deletedAt is null
        and (:status is null or f.status = :status)
      order by f.createdAt desc
      """)
  Page<Foster> findAlForUser(
    @Param("memberId") Long memberId,
    @Param("status") FosterStatus status,
    Pageable pageable
  );

  /** 관리자 리스트 조회( 상태값.삭제 필터링, 신청일 정렬)
   * ex)
   * status = null //전체 조회
   * status = 'PENDING' //PENDING만 조회
   * includeDeleted = true //softDelete 포함 조회
   * includeDeleted = false //softDelete 미포함 조회
   */
  @Query("""
      select f
      from Foster f
      where (:status is null or f.status = :status)
      and (:includeDeleted = true or f.deletedAt is null)
      order by f.createdAt desc
      """)
    Page<Foster> findAllForAdmin(
      @Param("status") FosterStatus status,
      @Param("includeDeleted") boolean includeDeleted,
      Pageable pageable
    );

  /**  관리자 수정 - 해당 동물이 임시보호 상태(FOSTERING/EXTENDED)가 있는지 확인(삭제된 데이터 제외) */
  boolean existsByAnimal_IdAndStatusInAndDeletedAtIsNull(
    Long animalId,
    Collection<FosterStatus> statuses //여러 임시보호 상태를 한번에 IN 조회하기위해 Collection사용
  );

}
