package com.infiniteprints.platform.ecommerce.product.controller;

import com.infiniteprints.platform.ecommerce.product.dto.ProductRequest;
import com.infiniteprints.platform.ecommerce.product.dto.ProductResponse;
import com.infiniteprints.platform.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Public: list products
    @GetMapping
    public Page<ProductResponse> list(Pageable pageable) {
        return productService.list(pageable);
    }

    // Public: search
    @GetMapping("/search")
    public Page<ProductResponse> search(@RequestParam String q, Pageable pageable) {
        return productService.search(q, pageable);
    }

    // Public: get by ID
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    // Admin only: create
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest req) {
        return ResponseEntity.status(201).body(productService.create(req));
    }

    // Admin only: update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest req) {
        return productService.update(id, req);
    }

    // Admin only: delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Admin only: upload image
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "primary", defaultValue = "false") boolean isPrimary) throws Exception {
        return productService.addImage(id, file, "main", isPrimary);
    }
}