package com.paw.ddasoom.member.domain;

import java.time.LocalDateTime;

import com.paw.ddasoom.common.util.BaseTimeEntity;
import com.paw.ddasoom.member.exception.MemberErrorCode;
import com.paw.ddasoom.member.exception.MemberException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity{

  @Id 
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  private String password; // 소셜 유저는 null

  @Column(length = 50)
  private String name;

  @Column(length = 20, unique = true)
  private String nickname;

  @Column(length = 20)
  private String tel;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(nullable = false)
  private boolean isDeleted;

  private LocalDateTime deletedAt;

  @Builder
  public Member(String email, String password, String name, String nickname, String tel, Role role) {
      this.email = email;
      this.password = password;
      this.name = name;
      this.nickname = nickname;
      this.tel = tel;
      this.role = role != null ? role : Role.GUEST;
      this.isDeleted = false; // 기본값
  }

  // 리치도메인 메서드 -> SNS 회원가입 추가 정보 수용 + Update
  public void updateExtraInfo(String name, String nickname, String tel) {
      this.name = name;
      this.nickname = nickname;
      this.tel = tel;
      this.role = Role.USER; // 추가 정보 입력 시 USER로 권한 승급
  }

  // 리치도메인 메서드 -> Soft delete 
  public void softDelete() {
        if (this.isDeleted) {
            throw new MemberException(MemberErrorCode.ALREADY_DELETED_MEMBER);
        }
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

}
