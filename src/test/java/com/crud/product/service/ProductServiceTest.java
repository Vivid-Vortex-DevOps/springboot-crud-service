package com.crud.product.service;

import com.crud.product.dto.ProductRequest;
import com.crud.product.entity.Product;
import com.crud.product.exception.ProductNotFoundException;
import com.crud.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository repo;
    @InjectMocks ProductService service;

    @Test
    void create_savesAndReturnsResponse() {
        var req = new ProductRequest("Widget", "A widget", BigDecimal.TEN, 5);
        var saved = productWithId(1L, "Widget");
        when(repo.save(any())).thenReturn(saved);

        var response = service.create(req);

        assertThat(response.name()).isEqualTo("Widget");
        assertThat(response.id()).isEqualTo(1L);
        verify(repo).save(any());
    }

    @Test
    void getById_returnsProduct_whenFound() {
        var p = productWithId(1L, "Widget");
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        var response = service.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void update_modifiesProduct_whenFound() {
        var existing = productWithId(1L, "Old");
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new ProductRequest("New", "Desc", BigDecimal.ONE, 3);
        var response = service.update(1L, req);

        assertThat(response.name()).isEqualTo("New");
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
            .isInstanceOf(ProductNotFoundException.class);
    }

    private Product productWithId(Long id, String name) {
        var p = new Product();
        p.setName(name);
        p.setPrice(BigDecimal.TEN);
        p.setQuantity(0);
        try {
            var field = Product.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(p, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return p;
    }
}
