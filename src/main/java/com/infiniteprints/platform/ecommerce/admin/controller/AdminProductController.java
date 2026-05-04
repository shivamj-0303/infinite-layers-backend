package com.infiniteprints.platform.ecommerce.admin.controller;

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

/**
 * Admin Product Management Controller
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get all products (admin view - includes inactive)
     */
    @GetMapping
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productService.listAll(pageable);
    }

    /**
     * Search products (admin view)
     */
    @GetMapping("/search")
    public Page<ProductResponse> searchProducts(@RequestParam String q, Pageable pageable) {
        return productService.search(q, pageable);
    }

    /**
     * Get single product by ID
     */
    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable UUID id) {
        return productService.getById(id);
    }

    /**
     * Create new product
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest req) {
        return ResponseEntity.status(201).body(productService.create(req));
    }

    /**
     * Update product
     */
    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductRequest req) {
        return productService.update(id, req);
    }

    /**
     * Delete product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload single image for product
     * 
     * @param id Product ID
     * @param file Image file (max 5MB, must be image)
     * @param isPrimary Whether this is the primary/featured image
     */
    @PostMapping("/{id}/images")
    public ProductResponse uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "primary", defaultValue = "false") boolean isPrimary) throws Exception {
        return productService.addImage(id, file, "main", isPrimary);
    }

    /**
     * Upload multiple images for product (max 7 total)
     * 
     * @param id Product ID
     * @param files Array of image files
     */
    @PostMapping("/{id}/images/batch")
    public ProductResponse uploadMultipleImages(
            @PathVariable UUID id,
            @RequestParam("files") MultipartFile[] files) throws Exception {
        if (files.length > 7) {
            throw new RuntimeException("Maximum 7 images allowed per product");
        }
        
        ProductResponse response = productService.getById(id);
        if (response.images.size() + files.length > 7) {
            throw new RuntimeException("Total images cannot exceed 7. Current: " + response.images.size());
        }

        for (int i = 0; i < files.length; i++) {
            response = productService.addImage(id, files[i], "main", i == 0);
        }
        
        return response;
    }

    /**
     * Delete image from product
     */
    @DeleteMapping("/{id}/images/{imageId}")
    public ProductResponse deleteImage(
            @PathVariable UUID id,
            @PathVariable UUID imageId) throws Exception {
        return productService.removeImage(id, imageId);
    }

    /**
     * Update image metadata (alt text, sort order)
     */
    @PutMapping("/{id}/images/{imageId}")
    public ProductResponse updateImageMetadata(
            @PathVariable UUID id,
            @PathVariable UUID imageId,
            @RequestParam(required = false) String altText,
            @RequestParam(required = false) Integer sortOrder) throws Exception {
        return productService.updateImageMetadata(id, imageId, altText, sortOrder);
    }

    /**
     * Set product image as primary
     */
    @PutMapping("/{id}/images/{imageId}/set-primary")
    public ProductResponse setPrimaryImage(
            @PathVariable UUID id,
            @PathVariable UUID imageId) throws Exception {
        return productService.setPrimaryImage(id, imageId);
    }
}
