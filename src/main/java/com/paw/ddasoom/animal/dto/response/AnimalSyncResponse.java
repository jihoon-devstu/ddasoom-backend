package com.paw.ddasoom.animal.dto.response;

import com.paw.ddasoom.animal.domain.Animal;

/**
 * 프론트로 보내줄 dto
 * AnimalSyncResponse
 * @param id
 * @param abandonmentId
 * @param kind
 * @param nickname
 * @param gender
 * @param typeName
 * @param age
 * @param imageUrl
 * @param likeCount
 * @param isFostered
 */
public record AnimalSyncResponse(
        Long id,
        String abandonmentId,
        String kind,
        String nickname,
        String gender,
        String typeName,
        String age,
        String imageUrl,
        String specialMark,
        String vaccinationChk,
        int likeCount,
        boolean isFostered
) {
    public static AnimalSyncResponse from(Animal animal) {
        return new AnimalSyncResponse(
                animal.getId(),
                animal.getAbandonmentId(),
                animal.getKind().name(),
                animal.getNickname(),
                animal.getGender().name(),
                animal.getTypeName(),
                animal.getAge(),
                animal.getImageUrl(),
                animal.getSpecialMark(),
                animal.getVaccinationChk(),
                animal.getLikeCount(),
                animal.isFostered()
        );
    }
}