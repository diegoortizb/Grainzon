package com.grainzon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grainzon.dto.CreateProductRequest;
import com.grainzon.dto.PageResponse;
import com.grainzon.dto.ProductResponse;
import com.grainzon.entity.Product;
import com.grainzon.repository.ProductRepository;

@Service
public class ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductService.class);

	private final ProductRepository repository;

	public ProductService(ProductRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public PageResponse<ProductResponse> list(int page, int size) {
		Page<Product> result = repository.findAll(PageRequest.of(page, size, Sort.by("id")));
		log.debug("Listed products page={} size={}: {} returned, {} total", page, size, result.getNumberOfElements(),
				result.getTotalElements());
		return PageResponse.from(result, ProductResponse::from);
	}

	@Transactional
	public ProductResponse create(CreateProductRequest request) {
		log.debug("Creating product name='{}'", request.name());
		Product saved = repository.save(new Product(request.name(), request.itemPrice()));
		log.info("Created product id={}", saved.getId());
		return ProductResponse.from(saved);
	}

}
