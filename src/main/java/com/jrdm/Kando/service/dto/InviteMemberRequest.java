package com.jrdm.Kando.service.dto;

import com.jrdm.Kando.domain.enums.BoardRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteMemberRequest {

    @NotBlank
    private String userId;

    @NotNull
    private BoardRole role;
}
