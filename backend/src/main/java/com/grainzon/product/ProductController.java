package com.grainzon.product;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductRepository repository;

	public ProductController(ProductRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<ProductResponse> list() {
		return repository.findAll(Sort.by("id")).stream().map(ProductResponse::from).toList();
	}

	@PostMapping
	public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
		Product saved = repository.save(new Product(request.name().trim()));
		return ResponseEntity.created(URI.create("/api/products/" + saved.getId()))
				.body(ProductResponse.from(saved));
	}

}
