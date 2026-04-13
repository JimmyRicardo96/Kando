package com.jrdm.Kando.service.dto;

import com.jrdm.Kando.domain.enums.BoardRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardMemberResponse {

    private String membershipId;
    private String userId;
    private String displayName;
    private String email;
    private BoardRole role;
    private Instant joinedAt;
}
