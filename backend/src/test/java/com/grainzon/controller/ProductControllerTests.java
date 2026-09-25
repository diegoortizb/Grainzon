package com.grainzon.controller;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.grainzon.dto.CreateProductRequest;
import com.grainzon.dto.ProductResponse;
import com.grainzon.service.ProductService;

@WebMvcTest(ProductController.class)
class ProductControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ProductService productService;

	@Test
	void listReturnsProducts() throws Exception {
		given(productService.list())
			.willReturn(List.of(new ProductResponse(1, "Hammer"), new ProductResponse(2, "Wrench")));

		mvc.perform(get("/api/products"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].name").value("Hammer"))
			.andExpect(jsonPath("$[1].name").value("Wrench"));
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
