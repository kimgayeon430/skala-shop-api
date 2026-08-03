package com.skala.skala_shop_api.dto.customer;

import jakarta.validation.constraints.NotBlank;

public record CustomerDeleteRequest(
        @NotBlank(message = "고객 ID는 필수입니다.")
        String customerId
) {
}
