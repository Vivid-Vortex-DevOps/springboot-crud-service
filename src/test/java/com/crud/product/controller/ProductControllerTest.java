package com.crud.product.controller;

import com.crud.product.dto.ProductRequest;
import com.crud.product.dto.ProductResponse;
import com.crud.product.exception.GlobalExceptionHandler;
import com.crud.product.exception.ProductNotFoundException;
import com.crud.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Uses standaloneSetup — no Spring context needed, no @WebMvcTest import issues.
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    ProductService productService;

    @InjectMocks
    ProductController controller;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private ProductResponse sampleResponse() {
        return new ProductResponse(
                1L, "Widget", "A widget",
                new BigDecimal("9.99"), 100,
                OffsetDateTime.now(), OffsetDateTime.now()
        );
    }

    @Test
    void createProduct_returns201() throws Exception {
        var request = new ProductRequest("Widget", "A widget", new BigDecimal("9.99"), 100);
        when(productService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Widget"));
    }

    @Test
    void createProduct_invalidRequest_returns400() throws Exception {
        // Empty name and negative price fail validation
        var bad = new ProductRequest("", null, new BigDecimal("-1"), -5);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listProducts_returns200() throws Exception {
        var page = new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 20), 1);
        when(productService.list(anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Widget"));
    }

    @Test
    void getProduct_found_returns200() throws Exception {
        when(productService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getProduct_notFound_returns404() throws Exception {
        when(productService.getById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_returns200() throws Exception {
        var request = new ProductRequest("Updated", "desc", new BigDecimal("19.99"), 50);
        when(productService.update(eq(1L), any())).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProduct_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_notFound_returns404() throws Exception {
        doThrow(new ProductNotFoundException(99L)).when(productService).delete(99L);

        mockMvc.perform(delete("/api/v1/products/99"))
                .andExpect(status().isNotFound());
    }
}
