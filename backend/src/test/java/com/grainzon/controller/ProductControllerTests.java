package com.grainzon.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.grainzon.dto.CreateProductRequest;
import com.grainzon.dto.PageResponse;
import com.grainzon.dto.ProductResponse;
import com.grainzon.service.ProductService;

@WebMvcTest(ProductController.class)
class ProductControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ProductService productService;

	@Test
	void listDefaultsToFirstPageOfTen() throws Exception {
		given(productService.list(0, 10)).willReturn(
				new PageResponse<>(List.of(new ProductResponse(1, "Hammer"), new ProductResponse(2, "Wrench")), 0, 10, 2, 1));

		mvc.perform(get("/api/products"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].id").value(1))
			.andExpect(jsonPath("$.content[0].name").value("Hammer"))
			.andExpect(jsonPath("$.content[1].name").value("Wrench"))
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.size").value(10))
			.andExpect(jsonPath("$.totalElements").value(2))
			.andExpect(jsonPath("$.totalPages").value(1));
	}

	@Test
	void listPassesRequestedPage() throws Exception {
		given(productService.list(2, 5)).willReturn(new PageResponse<>(List.of(), 2, 5, 11, 3));

		mvc.perform(get("/api/products").param("page", "2").param("size", "5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page").value(2))
			.andExpect(jsonPath("$.totalPages").value(3));
	}

	@Test
	void listRejectsInvalidPaging() throws Exception {
		mvc.perform(get("/api/products").param("page", "-1")).andExpect(status().isBadRequest());
		mvc.perform(get("/api/products").param("size", "0")).andExpect(status().isBadRequest());
		mvc.perform(get("/api/products").param("size", "101")).andExpect(status().isBadRequest());

		verify(productService, never()).list(anyInt(), anyInt());
	}

	@Test
	void createReturnsCreatedProduct() throws Exception {
		given(productService.create(any(CreateProductRequest.class))).willReturn(new ProductResponse(7, "Drill"));

		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Drill\"}"))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", "/api/products/7"))
			.andExpect(jsonPath("$.id").value(7))
			.andExpect(jsonPath("$.name").value("Drill"));
	}

	@Test
	void createRejectsBlankName() throws Exception {
		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
			.andExpect(status().isBadRequest());

		verify(productService, never()).create(any());
	}

}
