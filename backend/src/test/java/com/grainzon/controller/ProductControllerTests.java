package com.grainzon.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.grainzon.dto.CreateProductRequest;
import com.grainzon.dto.PageResponse;
import com.grainzon.dto.ProductResponse;
import com.grainzon.service.ProductService;

@WebMvcTest(ProductController.class)
@ExtendWith(OutputCaptureExtension.class)
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

	@Test
	void createRejectsMissingName() throws Exception {
		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest());
		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\":null}"))
			.andExpect(status().isBadRequest());

		verify(productService, never()).create(any());
	}

	@Test
	void createAcceptsNameAtMaxLength() throws Exception {
		String name = "a".repeat(256);
		given(productService.create(any(CreateProductRequest.class))).willReturn(new ProductResponse(1, name));

		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(json(name)))
			.andExpect(status().isCreated());

		verify(productService).create(new CreateProductRequest(name));
	}

	@Test
	void createRejectsNameOverMaxLength() throws Exception {
		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(json("a".repeat(257))))
			.andExpect(status().isBadRequest());

		verify(productService, never()).create(any());
	}

	@Test
	void createAppliesMaxLengthAfterTrimming() throws Exception {
		String name = "a".repeat(256);
		given(productService.create(any(CreateProductRequest.class))).willReturn(new ProductResponse(1, name));

		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(json("  " + name + "  ")))
			.andExpect(status().isCreated());

		verify(productService).create(new CreateProductRequest(name));
	}

	@Test
	void createRejectsControlCharacters() throws Exception {
		// JSON escapes: NUL (which Postgres cannot store), newline, tab and escape inside the name.
		for (String name : new String[] { "bad\\u0000name", "two\\nlines", "tab\\there", "esc\\u001bape" }) {
			mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(json(name)))
				.andExpect(status().isBadRequest());
		}

		verify(productService, never()).create(any());
	}

	@Test
	void createAcceptsPunctuationAndNonAsciiLetters() throws Exception {
		String name = "3/4\" Hex Bolt & O'Brien Café M8×1.25";
		given(productService.create(any(CreateProductRequest.class))).willReturn(new ProductResponse(1, name));

		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"3/4\\\" Hex Bolt & O'Brien Café M8×1.25\"}"))
			.andExpect(status().isCreated());

		verify(productService).create(new CreateProductRequest(name));
	}

	@Test
	void validationErrorsExplainWhichFieldFailedAndWhy() throws Exception {
		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(json("a".repeat(257))))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.detail").value("Invalid request."))
			.andExpect(jsonPath("$.errors.name").value("must be at most 256 characters"));
	}

	@Test
	void pagingErrorsExplainWhichParameterFailed() throws Exception {
		mvc.perform(get("/api/products").param("size", "101"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.errors.size").value("must be less than or equal to 100"));
	}

	@Test
	void malformedJsonIsABadRequest() throws Exception {
		mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{\"name\":"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
	}

	@Test
	void unexpectedErrorsAreLoggedButNotExposed(CapturedOutput output) throws Exception {
		given(productService.list(anyInt(), anyInt())).willThrow(new IllegalStateException("database password is hunter2"));

		mvc.perform(get("/api/products"))
			.andExpect(status().isInternalServerError())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.detail").value("Something went wrong."))
			.andExpect(content().string(not(containsString("hunter2"))));

		assertThat(output).contains("Unhandled error on GET /api/products").contains("database password is hunter2");
	}

	private static String json(String name) {
		return "{\"name\":\"" + name + "\"}";
	}

}
