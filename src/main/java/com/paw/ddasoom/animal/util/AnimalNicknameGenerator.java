package com.paw.ddasoom.animal.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class AnimalNicknameGenerator {

  /**
   * 랜덤으로 동물의 닉네임 이름 리스트로 저장
   */
  private static final List<String> NOUNS = List.of(
    "몽이", "콩이", "보리", "두부", "초코", "별이", "구름이", "하늘이",
    "바둑이", "나비", "자두", "밤이", "마루", "방울이", "까망이", "하양이",
    "노랑이", "젤리", "라떼", "꿀떡이", "만두", "참깨", "콩콩이", "뭉치",
    "설이", "복이", "다롱이", "순두부", "감자", "도토리", "포도", "망고"
  );

  /**
   * 동물들의 이름을 생성
   * @return 이름
   */
  public String generate() {
    String noun = pick(NOUNS);
    return noun;
  }

  /**
   * 동물 이름 리스트 안의 랜덤 인덱스 값 뽑음
   * @param list 동물 이름 리스트
   * @return 리스트 중 랜덤 인덱스의 값
   */
  private String pick(List<String> list) {
    int index = ThreadLocalRandom.current().nextInt(list.size());
    return list.get(index);
  }
}
