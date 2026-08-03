package com.skala.skala_shop_api.dto.product;

import com.skala.skala_shop_api.domain.product.Product;

public record ProductResponse(Long id, String name, long price) {

    public static ProductResponse from(Product product){
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getPrice()
        );
    }
}

// record : 값을 전달하는 DTO를 간결하게 작성하는 java문법
// 생성자와 접근 메서드, equals(), hashCode(), toString()이 자동으로 만들어진다.