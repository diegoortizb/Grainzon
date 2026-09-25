package com.grainzon.service;

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

	private final ProductRepository repository;

	public ProductService(ProductRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public PageResponse<ProductResponse> list(int page, int size) {
		return PageResponse.from(repository.findAll(PageRequest.of(page, size, Sort.by("id"))), ProductResponse::from);
	}

	@Transactional
	public ProductResponse create(CreateProductRequest request) {
		Product saved = repository.save(new Product(request.name()));
		return ProductResponse.from(saved);
	}

}
