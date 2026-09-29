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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.grainzon.dto.CreateProductRequest;
import com.grainzon.dto.PageResponse;
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
	void listReturnsRequestedPageSortedById() {
		PageRequest request = PageRequest.of(1, 2, Sort.by("id"));
		given(repository.findAll(request))
			.willReturn(new PageImpl<>(List.of(product(3, "Saw"), product(4, "Drill")), request, 5));

		PageResponse<ProductResponse> page = productService.list(1, 2);

		assertThat(page.content()).containsExactly(new ProductResponse(3, "Saw", 0.0), new ProductResponse(4, "Drill", 0.0));
		assertThat(page.page()).isEqualTo(1);
		assertThat(page.size()).isEqualTo(2);
		assertThat(page.totalElements()).isEqualTo(5);
		assertThat(page.totalPages()).isEqualTo(3);
	}

	@Test
	void createSavesProductAndReturnsIt() {
		given(repository.save(any(Product.class))).willReturn(product(7, "Drill", 19.99));

		ProductResponse created = productService.create(new CreateProductRequest("Drill", 19.99));

		ArgumentCaptor<Product> saved = ArgumentCaptor.forClass(Product.class);
		verify(repository).save(saved.capture());
		assertThat(saved.getValue().getName()).isEqualTo("Drill");
		assertThat(saved.getValue().getItemPrice()).isEqualTo(19.99);
		assertThat(created).isEqualTo(new ProductResponse(7, "Drill", 19.99));
	}

	private static Product product(int id, String name) {
		return product(id, name, 0.0);
	}

	private static Product product(int id, String name, double itemPrice) {
		Product product = new Product(name, itemPrice);
		ReflectionTestUtils.setField(product, "id", id);
		return product;
	}

}
