package com.skala.skala_shop_api.tools;

import com.skala.skala_shop_api.dto.product.ProductResponse;
import com.skala.skala_shop_api.service.ProductService;
import java.util.List;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class ProductTools {

    private final ProductService productService;

    public ProductTools(ProductService productService) {
        this.productService = productService;
    }

    @McpTool(
        name = "list_products",
        description = "쇼핑몰에 등록된 모든 상품의 ID, 이름, 가격을 조회합니다."
    )
    public List<ProductResponse> listProducts() {
        return productService.findAll();
    }

    @McpTool(
        name = "get_product",
        description = "상품 ID를 사용하여 상품 하나를 조회합니다."
    )
    public ProductResponse getProduct(
            @McpToolParam(
                description = "조회할 상품 ID",
                required = true
            )
            Long productId
    ) {
        return productService.findById(productId);
    }
}