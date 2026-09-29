package com.grainzon.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CreateProductRequestTests {

	@Test
	void stripsSurroundingWhitespace() {
		assertThat(new CreateProductRequest("  Drill \t", null).name()).isEqualTo("Drill");
	}

	@Test
	void keepsInnerWhitespace() {
		assertThat(new CreateProductRequest(" Cordless  drill ", null).name()).isEqualTo("Cordless  drill");
	}

	@Test
	void allowsNullSoValidationCanRejectIt() {
		assertThat(new CreateProductRequest(null, null).name()).isNull();
	}

	@Test
	void defaultsMissingPriceToZero() {
		assertThat(new CreateProductRequest("Drill", null).itemPrice()).isZero();
	}

	@Test
	void keepsGivenPrice() {
		assertThat(new CreateProductRequest("Drill", 19.99).itemPrice()).isEqualTo(19.99);
	}

}
