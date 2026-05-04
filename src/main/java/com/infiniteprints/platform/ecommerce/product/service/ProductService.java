package com.infiniteprints.platform.ecommerce.product.service;

import com.infiniteprints.platform.ecommerce.common.exception.ResourceNotFoundException;
import com.infiniteprints.platform.ecommerce.media.service.SupabaseStorageService;
import com.infiniteprints.platform.ecommerce.product.dto.ProductRequest;
import com.infiniteprints.platform.ecommerce.product.dto.ProductResponse;
import com.infiniteprints.platform.ecommerce.product.entity.Product;
import com.infiniteprints.platform.ecommerce.product.entity.ProductImage;
import com.infiniteprints.platform.ecommerce.product.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final SupabaseStorageService storageService;

    public ProductService(ProductRepository productRepository, SupabaseStorageService storageService) {
        this.productRepository = productRepository;
        this.storageService = storageService;
    }

    public ProductResponse create(ProductRequest req) {
        Product p = new Product();
        mapRequest(p, req);
        return toResponse(productRepository.save(p));
    }

    public ProductResponse update(UUID id, ProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        mapRequest(p, req);
        return toResponse(productRepository.save(p));
    }

    public void delete(UUID id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        p.getImages().forEach(img -> storageService.deleteFileByUrl(img.getPublicUrl()));

        productRepository.delete(p);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 5MB");
        }
    }

    public ProductResponse addImage(UUID productId, MultipartFile file, String imageType, boolean isPrimary) throws Exception {
        validateFile(file);

        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        String url = storageService.uploadProductImage(productId, file, imageType);

        ProductImage img = new ProductImage();
        img.setProductId(productId);
        img.setPublicUrl(url);
        img.setType(imageType);
        img.setStoragePath(url); // keep same for now (don’t overcomplicate)
        img.setMimeType(file.getContentType());
        img.setFileSizeBytes(file.getSize());
        img.setIsPrimary(isPrimary);
        img.setSortOrder(p.getImages().size());

        if (isPrimary) {
            p.getImages().forEach(i -> i.setIsPrimary(false));
        }

        p.getImages().add(img);

        return toResponse(productRepository.save(p));
    }

    public Page<ProductResponse> list(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable).map(this::toResponse);
    }

    public Page<ProductResponse> listAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<ProductResponse> search(String query, Pageable pageable) {
        return productRepository.search(query, pageable).map(this::toResponse);
    }

    public ProductResponse getById(UUID id) {
        return toResponse(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id)));
    }

    public ProductResponse removeImage(UUID productId, UUID imageId) throws Exception {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        ProductImage imageToRemove = p.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        storageService.deleteFileByUrl(imageToRemove.getPublicUrl());
        p.getImages().remove(imageToRemove);

        return toResponse(productRepository.save(p));
    }

    public ProductResponse updateImageMetadata(UUID productId, UUID imageId, String altText, Integer sortOrder) throws Exception {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        ProductImage image = p.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        if (altText != null) {
            image.setAltText(altText);
        }
        if (sortOrder != null) {
            image.setSortOrder(sortOrder);
        }

        return toResponse(productRepository.save(p));
    }

    public ProductResponse setPrimaryImage(UUID productId, UUID imageId) throws Exception {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        ProductImage imageToSetPrimary = p.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        p.getImages().forEach(img -> img.setIsPrimary(false));
        imageToSetPrimary.setIsPrimary(true);

        return toResponse(productRepository.save(p));
    }

    private void mapRequest(Product p, ProductRequest req) {
        p.setName(req.name);
        p.setSlug(req.slug);
        p.setDescription(req.description);
        p.setShortDescription(req.shortDescription);
        p.setSku(req.sku);
        p.setPrice(req.price);
        p.setCompareAtPrice(req.compareAtPrice);
        p.setStockQuantity(req.stockQuantity == null ? 0 : req.stockQuantity);
        p.setCategoryId(req.categoryId);
        p.setIsActive(req.isActive == null ? true : req.isActive);
        p.setIsFeatured(req.isFeatured == null ? false : req.isFeatured);
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse r = new ProductResponse();
        r.id = p.getId();
        r.name = p.getName();
        r.slug = p.getSlug();
        r.description = p.getDescription();
        r.shortDescription = p.getShortDescription();
        r.sku = p.getSku();
        r.price = p.getPrice();
        r.compareAtPrice = p.getCompareAtPrice();
        r.stockQuantity = p.getStockQuantity();
        r.categoryId = p.getCategoryId();
        r.isActive = p.getIsActive();
        r.isFeatured = p.getIsFeatured();
        r.createdAt = p.getCreatedAt();
        r.updatedAt = p.getUpdatedAt();

        r.images = p.getImages().stream().map(img -> {
            ProductResponse.ImageResponse ir = new ProductResponse.ImageResponse();
            ir.id = img.getId();
            ir.publicUrl = img.getPublicUrl();
            ir.altText = img.getAltText();
            ir.isPrimary = img.getIsPrimary();
            ir.sortOrder = img.getSortOrder();
            return ir;
        }).collect(Collectors.toList());

        return r;
    }
}