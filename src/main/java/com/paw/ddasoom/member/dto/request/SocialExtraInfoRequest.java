package com.paw.ddasoom.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialExtraInfoRequest {
  @NotBlank(message = "이름은 필수입니다.")
  private String name;

  @NotBlank(message = "닉네임은 필수입니다.")
  private String nickname;

  @NotBlank(message = "전화번호는 필수입니다.")
  private String tel;
}
