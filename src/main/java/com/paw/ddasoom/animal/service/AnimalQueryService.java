package com.paw.ddasoom.animal.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paw.ddasoom.animal.domain.Animal;
import com.paw.ddasoom.animal.dto.request.AnimalListPageRequest;
import com.paw.ddasoom.animal.dto.response.AnimalDetailPageResponse;
import com.paw.ddasoom.animal.dto.response.AnimalListPageResponse;
import com.paw.ddasoom.animal.dto.response.AnimalMainPageResponse;
import com.paw.ddasoom.animal.dto.response.AnimalMyPageResponse;
import com.paw.ddasoom.animal.exception.AnimalErrorCode;
import com.paw.ddasoom.animal.exception.AnimalException;
import com.paw.ddasoom.animal.repository.AnimalLikeRepository;
import com.paw.ddasoom.animal.repository.AnimalRepository;
import com.paw.ddasoom.common.dto.PageResponse;

import lombok.RequiredArgsConstructor;

/**
 * 유기동물 조회 통합 서비스 - 목록(동적검색) / 상세 / 메인 미리보기 / 마이페이지 좋아요 목록.
 * 전부 읽기 전용이라 클래스 단위로 readOnly 트랜잭션을 건다.
 * (쓰기 성격인 Sync/Nickname/Like는 별도 서비스로 분리 - 읽기/쓰기 혼합 방지)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnimalQueryService {

  private final AnimalRepository animalRepository;
  private final AnimalLikeRepository animalLikeRepository;

  // 목록 (동적 검색 + 페이징). memberId 있으면 이번 페이지의 isLiked를 배치 조회로 채운다.
  public PageResponse<AnimalListPageResponse> search(
    AnimalListPageRequest request, Long memberId, Pageable pageable) {

    Page<Animal> page = animalRepository.search(request, memberId, pageable);

    // 이번 페이지 동물들 중 내가 좋아요한 id 집합을 한 번에 조회(비로그인이면 빈 집합)
    Set<Long> likedIds = resolveLikedIds(page.getContent(), memberId);

    return PageResponse.of(page,
      animal -> AnimalListPageResponse.from(animal, likedIds.contains(animal.getId())));
  }

  // 상세. memberId 있으면 단건 좋아요 여부(isLiked)를 계산한다.
  public AnimalDetailPageResponse getDetail(Long animalId, Long memberId) {
    
    Animal animal = animalRepository.findById(animalId)
      .orElseThrow(() -> new AnimalException(AnimalErrorCode.ANIMAL_NOT_FOUND));

    boolean isLiked = memberId != null
      && animalLikeRepository.existsByAnimal_IdAndMember_Id(animalId, memberId);

    return AnimalDetailPageResponse.from(animal, isLiked);
  }

  // 메인 미리보기 - 최근 등록 4건. 공개(비로그인) 노출이라 isLiked는 계산하지 않는다.
  public List<AnimalMainPageResponse> getMainPreview() {

    return animalRepository.findTop4ByOrderByIdDesc().stream()
      .map(AnimalMainPageResponse::from)
      .toList();
  }

  // 마이페이지 - 내가 좋아요한 동물 목록(최근순). 정의상 전부 isLiked=true.
  public PageResponse<AnimalMyPageResponse> getMyLikedAnimals(Long memberId, Pageable pageable) {

    Page<Animal> page = animalLikeRepository.findLikedAnimals(memberId, pageable);

    return PageResponse.of(page, AnimalMyPageResponse::from);
  }

  // 이번 페이지 동물들 중 내가 좋아요 한 id 집합 (비로그인/빈 페이지면 빈 집함, N+1 방지)
  private Set<Long> resolveLikedIds(List<Animal> animals, Long memberId) {

    if (memberId == null || animals.isEmpty()) {
      return Set.of();
    }

    List<Long> animalIds = animals.stream().map(Animal::getId).toList();

    return Set.copyOf(animalLikeRepository.findLikedAnimalIds(memberId, animalIds));
  }
}
