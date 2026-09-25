package com.grainzon.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grainzon.dto.CreateProductRequest;
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
	public List<ProductResponse> list() {
		return repository.findAll(Sort.by("id")).stream().map(ProductResponse::from).toList();
	}

	@Transactional
	public ProductResponse create(CreateProductRequest request) {
		Product saved = repository.save(new Product(request.name().trim()));
		return ProductResponse.from(saved);
	}

}
