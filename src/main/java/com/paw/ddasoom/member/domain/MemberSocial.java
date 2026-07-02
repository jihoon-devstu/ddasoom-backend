package com.paw.ddasoom.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_social", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_id"}) // 복합 유니크 키
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSocial {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_social_id")
  private Long id;

  @Column(nullable = false, length = 20)
  private String provider; // KAKAO, NAVER, GOOGLE

  @Column(nullable = false)
  private String providerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Builder
  public MemberSocial(String provider, String providerId, Member member) {
      this.provider = provider;
      this.providerId = providerId;
      this.member = member;
  }
}
