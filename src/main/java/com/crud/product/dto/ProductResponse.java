package com.crud.product.dto;

import com.crud.product.entity.Product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer quantity,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
            p.getId(), p.getName(), p.getDescription(),
            p.getPrice(), p.getQuantity(), p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
