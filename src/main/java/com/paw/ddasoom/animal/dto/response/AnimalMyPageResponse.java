package com.paw.ddasoom.animal.dto.response;

import com.paw.ddasoom.animal.domain.Animal;
import com.paw.ddasoom.animal.domain.AnimalGender;
import com.paw.ddasoom.animal.domain.AnimalKind;

import lombok.Builder;

@Builder
public record AnimalMyPageResponse(
  Long animalId,
  AnimalKind kind,
  String nickname,
  AnimalGender gender,
  String typeName,
  String age,
  String location,
  String imageUrl,
  boolean isFostered,
  int likeCount,
  boolean isLiked
) {
  public static AnimalMyPageResponse from(Animal animal) {
    return AnimalMyPageResponse.builder()
      .animalId(animal.getId())
      .kind(animal.getKind())
      .nickname(animal.getNickname())
      .gender(animal.getGender())
      .typeName(animal.getTypeName())
      .age(animal.getAge())
      .location(animal.getLocation())
      .imageUrl(animal.getImageUrl())
      .isFostered(animal.isFostered())
      .likeCount(animal.getLikeCount())
      .isLiked(true)
      .build();
  }
}
