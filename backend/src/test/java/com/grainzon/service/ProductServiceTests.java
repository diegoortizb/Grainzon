package com.grainzon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.grainzon.dto.CreateProductRequest;
import com.grainzon.dto.ProductResponse;
import com.grainzon.entity.Product;
import com.grainzon.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

	@Mock
	private ProductRepository repository;

	@InjectMocks
	private ProductService productService;

	@Test
	void listReturnsProductsSortedById() {
		given(repository.findAll(Sort.by("id"))).willReturn(List.of(product(1, "Hammer"), product(2, "Wrench")));

		assertThat(productService.list())
			.containsExactly(new ProductResponse(1, "Hammer"), new ProductResponse(2, "Wrench"));
	}

	@Test
	void createTrimsNameAndSaves() {
		given(repository.save(any(Product.class))).willReturn(product(7, "Drill"));

		ProductResponse created = productService.create(new CreateProductRequest("  Drill "));

		ArgumentCaptor<Product> saved = ArgumentCaptor.forClass(Product.class);
		verify(repository).save(saved.capture());
		assertThat(saved.getValue().getName()).isEqualTo("Drill");
		assertThat(created).isEqualTo(new ProductResponse(7, "Drill"));
	}

	private static Product product(int id, String name) {
		Product product = new Product(name);
		ReflectionTestUtils.setField(product, "id", id);
		return product;
	}

}
