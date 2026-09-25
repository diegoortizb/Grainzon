package com.grainzon.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ProductRepository repository;

	@Test
	void listReturnsProducts() throws Exception {
		given(repository.findAll(any(Sort.class))).willReturn(List.of(product(1, "Hammer"), product(2, "Wrench")));

		mvc.perform(get("/api/products"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].name").value("Hammer"))
			.andExpect(jsonPath("$[1].name").value("Wrench"));
	}

	@Test
	void createReturnsCreatedProduct() throws Exception {
		given(repository.save(any(Product.class))).willReturn(product(7, "Drill"));

		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  Drill \"}"))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", "/api/products/7"))
			.andExpect(jsonPath("$.id").value(7))
			.andExpect(jsonPath("$.name").value("Drill"));
	}

	@Test
	void createRejectsBlankName() throws Exception {
		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
			.andExpect(status().isBadRequest());

		verify(repository, never()).save(any());
	}

	private static Product product(int id, String name) {
		Product product = new Product(name);
		ReflectionTestUtils.setField(product, "id", id);
		return product;
	}

}
