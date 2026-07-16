package com.paw.ddasoom.animal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paw.ddasoom.animal.domain.Animal;
import com.paw.ddasoom.animal.dto.response.AnimalMyPageResponse;
import com.paw.ddasoom.animal.repository.AnimalLikeRepository;
import com.paw.ddasoom.common.dto.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnimalMyPageService {

  private final AnimalLikeRepository animalLikeRepository;

  // 내가 좋아요한 동물 목록(최근 좋아요 순). 정의상 전부 isLiked=true.
  @Transactional(readOnly = true)
  public PageResponse<AnimalMyPageResponse> getMyLikedAnimals(Long memberId, Pageable pageable) {
    Page<Animal> page = animalLikeRepository.findLikedAnimals(memberId, pageable);
    return PageResponse.of(page, AnimalMyPageResponse::from);
  }
}