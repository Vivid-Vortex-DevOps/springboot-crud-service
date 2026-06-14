package com.crud.product.service;

import com.crud.product.dto.ProductRequest;
import com.crud.product.dto.ProductResponse;
import com.crud.product.entity.Product;
import com.crud.product.exception.ProductNotFoundException;
import com.crud.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public ProductResponse create(ProductRequest req) {
        Product p = new Product();
        p.setName(req.name());
        p.setDescription(req.description() != null ? req.description() : "");
        p.setPrice(req.price());
        p.setQuantity(req.quantity());
        Product saved = repo.save(p);
        log.atInfo().addKeyValue("productId", saved.getId()).setMessage("product created").log();
        return ProductResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return repo.findById(id)
            .map(ProductResponse::from)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repo.findAll(pageable).map(ProductResponse::from);
    }

    public ProductResponse update(Long id, ProductRequest req) {
        Product p = repo.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        p.setName(req.name());
        p.setDescription(req.description() != null ? req.description() : "");
        p.setPrice(req.price());
        p.setQuantity(req.quantity());
        log.atInfo().addKeyValue("productId", id).setMessage("product updated").log();
        return ProductResponse.from(repo.save(p));
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        repo.deleteById(id);
        log.atInfo().addKeyValue("productId", id).setMessage("product deleted").log();
    }
}
