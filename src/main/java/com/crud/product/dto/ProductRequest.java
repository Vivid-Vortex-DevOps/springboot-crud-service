package com.crud.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must not exceed 255 characters")
    String name,

    String description,

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.0", message = "price must be >= 0")
    BigDecimal price,

    @NotNull(message = "quantity is required")
    @Min(value = 0, message = "quantity must be >= 0")
    Integer quantity
) {}
