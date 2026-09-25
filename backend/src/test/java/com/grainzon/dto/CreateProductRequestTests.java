package com.grainzon.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CreateProductRequestTests {

	@Test
	void stripsSurroundingWhitespace() {
		assertThat(new CreateProductRequest("  Drill \t").name()).isEqualTo("Drill");
	}

	@Test
	void keepsInnerWhitespace() {
		assertThat(new CreateProductRequest(" Cordless  drill ").name()).isEqualTo("Cordless  drill");
	}

	@Test
	void allowsNullSoValidationCanRejectIt() {
		assertThat(new CreateProductRequest(null).name()).isNull();
	}

}
