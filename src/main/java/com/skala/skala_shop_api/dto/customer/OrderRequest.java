package com.skala.skala_shop_api.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderRequest(
        @NotBlank(message = "고객 ID는 필수입니다.")
        String customerId,

        @NotNull(message = "상품 ID는 필수입니다.")
        @Positive(message = "상품 ID는 1 이상이어야 합니다.")
        Long productId,

        @Positive(message = "수량은 1개 이상이어야 합니다.")
        int quantity
) {
}
