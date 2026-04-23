package com.messageschallenge.notifications.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMessageRequest(
    @NotBlank String category, @NotBlank @Size(min = 1, max = 4000) String body) {}
