package com.jrdm.Kando.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {

    private String id;
    private String name;
    private String description;
    private String ownerId;
    private String ownerDisplayName;
    private Instant createdAt;
    private Instant updatedAt;
}
