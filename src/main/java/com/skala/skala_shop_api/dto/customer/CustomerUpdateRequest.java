package com.skala.skala_shop_api.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CustomerUpdateRequest(
        @NotBlank(message = "고객 ID는 필수입니다.")
        String customerId,

        @NotBlank(message = "고객 이름은 필수입니다.")
        @Size(max = 50, message = "고객 이름은 50자 이하여야 합니다.")
        String name,

        @PositiveOrZero(message = "포인트는 0 이상이어야 합니다.")
        long point
) {
}
